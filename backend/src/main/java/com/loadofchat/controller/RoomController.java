package com.loadofchat.controller;

import com.loadofchat.dto.RoomCodeResponse;
import com.loadofchat.model.Room;
import com.loadofchat.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for room creation and validation.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /** Create a new room and return its 6-character code. */
    @PostMapping
    public RoomCodeResponse createRoom() {
        Room room = roomService.createRoom();
        return new RoomCodeResponse(room.getCode());
    }

    /** Check whether a room code exists (200) or not (404). */
    @GetMapping("/{code}")
    public ResponseEntity<Void> getRoom(@PathVariable String code) {
        if (roomService.roomExists(code)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
