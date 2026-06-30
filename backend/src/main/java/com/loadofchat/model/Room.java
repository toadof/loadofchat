package com.loadofchat.model;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory representation of a chat room.
 * Participant count is derived from active WebSocket session IDs.
 */
public class Room {

    private final String code;
    private final Instant createdAt;
    private Instant lastEmptyAt;
    private final Set<String> sessionIds = ConcurrentHashMap.newKeySet();

    public Room(String code) {
        this.code = code;
        this.createdAt = Instant.now();
        this.lastEmptyAt = null;
    }

    public String getCode() {
        return code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastEmptyAt() {
        return lastEmptyAt;
    }

    public void setLastEmptyAt(Instant lastEmptyAt) {
        this.lastEmptyAt = lastEmptyAt;
    }

    public int getParticipantCount() {
        return sessionIds.size();
    }

    public boolean addSession(String sessionId) {
        boolean added = sessionIds.add(sessionId);
        if (added && !sessionIds.isEmpty()) {
            lastEmptyAt = null;
        }
        return added;
    }

    public boolean removeSession(String sessionId) {
        boolean removed = sessionIds.remove(sessionId);
        if (removed && sessionIds.isEmpty()) {
            lastEmptyAt = Instant.now();
        }
        return removed;
    }

    public boolean isEmpty() {
        return sessionIds.isEmpty();
    }
}
