package com.loadofchat.service;

import com.loadofchat.model.Room;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory room store backed by ConcurrentHashMap.
 */
@Repository
public class InMemoryRoomStorage implements RoomStorage {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Override
    public Room createRoom() {
        String code;
        do {
            code = generateCode();
        } while (rooms.containsKey(code));

        Room room = new Room(code);
        rooms.put(code, room);
        return room;
    }

    @Override
    public Optional<Room> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rooms.get(code.toUpperCase()));
    }

    @Override
    public void removeRoom(String code) {
        rooms.remove(code);
    }

    /** Used by scheduled cleanup to iterate all rooms. Not part of the storage interface. */
    public ConcurrentHashMap<String, Room> getAllRooms() {
        return rooms;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
