package com.loadofchat.websocket;

import com.loadofchat.model.ChatMessage;
import com.loadofchat.service.RoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.Instant;

/**
 * Handles inbound STOMP messages from clients and broadcasts to room topics.
 */
@Controller
public class ChatController {

    private final RoomService roomService;

    public ChatController(RoomService roomService) {
        this.roomService = roomService;
    }

    @MessageMapping("/chat/{code}")
    public void handleMessage(
            @DestinationVariable String code,
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null) {
            return;
        }

        switch (message.getType()) {
            case JOIN -> roomService.joinRoom(code, sessionId, message.getSender());
            case CHAT -> {
                message.setTimestamp(Instant.now().toString());
                roomService.sendChatMessage(code, message);
            }
            case LEAVE -> roomService.leaveRoom(sessionId);
            default -> { /* PARTICIPANT_COUNT is server-generated only */ }
        }
    }
}
