package com.loadofchat.service;

import com.loadofchat.model.ChatMessage;
import com.loadofchat.model.Room;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Business logic for room lifecycle and participant tracking.
 */
@Service
public class RoomService {

    private final RoomStorage roomStorage;
    private final SimpMessagingTemplate messagingTemplate;

    /** Maps WebSocket session ID → room code for disconnect cleanup. */
    private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

    /** Maps WebSocket session ID → display name for leave messages. */
    private final Map<String, String> sessionToSender = new ConcurrentHashMap<>();

    @Value("${app.room-cleanup-minutes:10}")
    private int roomCleanupMinutes;

    public RoomService(RoomStorage roomStorage, SimpMessagingTemplate messagingTemplate) {
        this.roomStorage = roomStorage;
        this.messagingTemplate = messagingTemplate;
    }

    public Room createRoom() {
        return roomStorage.createRoom();
    }

    public boolean roomExists(String code) {
        return roomStorage.findByCode(normalizeCode(code)).isPresent();
    }

    public Optional<Room> getRoom(String code) {
        return roomStorage.findByCode(normalizeCode(code));
    }

    /**
     * Register a participant joining a room via WebSocket.
     */
    public void joinRoom(String code, String sessionId, String sender) {
        String normalized = normalizeCode(code);
        Room room = roomStorage.findByCode(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + code));

        room.addSession(sessionId);
        sessionToRoom.put(sessionId, normalized);
        sessionToSender.put(sessionId, sender);

        broadcastJoin(normalized, sender);
        broadcastParticipantCount(normalized, room.getParticipantCount());
    }

    /**
     * Broadcast a chat message to all subscribers in the room.
     */
    public void sendChatMessage(String code, ChatMessage message) {
        String normalized = normalizeCode(code);
        if (roomStorage.findByCode(normalized).isEmpty()) {
            throw new IllegalArgumentException("Room not found: " + code);
        }
        messagingTemplate.convertAndSend("/topic/room/" + normalized, message);
    }

    /**
     * Handle explicit leave or WebSocket disconnect.
     */
    public void leaveRoom(String sessionId) {
        String code = sessionToRoom.remove(sessionId);
        String sender = sessionToSender.remove(sessionId);
        if (code == null) {
            return;
        }

        roomStorage.findByCode(code).ifPresent(room -> {
            room.removeSession(sessionId);
            if (sender != null) {
                broadcastLeave(code, sender);
            }
            broadcastParticipantCount(code, room.getParticipantCount());
        });
    }

    /**
     * Scheduled cleanup: remove rooms with zero participants for longer than the threshold.
     */
    @Scheduled(fixedRate = 60_000)
    public void cleanupEmptyRooms() {
        Duration threshold = Duration.ofMinutes(roomCleanupMinutes);
        Instant cutoff = Instant.now().minus(threshold);

        if (roomStorage instanceof InMemoryRoomStorage inMemory) {
            inMemory.getAllRooms().forEach((code, room) -> {
                if (room.isEmpty() && room.getLastEmptyAt() != null
                        && room.getLastEmptyAt().isBefore(cutoff)) {
                    roomStorage.removeRoom(code);
                }
            });
        }
    }

    private void broadcastJoin(String code, String sender) {
        ChatMessage msg = new ChatMessage(
                ChatMessage.MessageType.JOIN,
                sender,
                sender + " joined the room",
                Instant.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/room/" + code, msg);
    }

    private void broadcastLeave(String code, String sender) {
        ChatMessage msg = new ChatMessage(
                ChatMessage.MessageType.LEAVE,
                sender,
                sender + " left the room",
                Instant.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/room/" + code, msg);
    }

    private void broadcastParticipantCount(String code, int count) {
        messagingTemplate.convertAndSend("/topic/room/" + code, ChatMessage.participantCount(count));
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }
}
