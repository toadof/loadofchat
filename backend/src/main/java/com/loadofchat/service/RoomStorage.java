package com.loadofchat.service;

import com.loadofchat.model.Room;

import java.util.Optional;

/**
 * Abstraction over room persistence. In-memory today; swap for Redis/Postgres later.
 */
public interface RoomStorage {

    Room createRoom();

    Optional<Room> findByCode(String code);

    void removeRoom(String code);
}
