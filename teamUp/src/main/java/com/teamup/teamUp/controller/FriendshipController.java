package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.friend.FriendshipResponseDto;
import com.teamup.teamUp.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<ResponseApi<Page<FriendshipResponseDto>>> getFriends(Authentication auth, @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        var result = friendshipService.listFriends(auth.getName(), pageable);
        return ResponseEntity.ok(new ResponseApi<>("Friends fetched successfully", result, true));
    }


    @DeleteMapping("/{friendId}")
    public ResponseEntity<ResponseApi<Void>> deleteFriendship(@PathVariable UUID friendId, Authentication auth) {
        friendshipService.removeFriend(auth.getName(), friendId);
        return ResponseEntity.ok(new ResponseApi<>("Friend removed successfully", null, true));
    }

    @GetMapping("/relation/{targetUsername}")
    public ResponseEntity<ResponseApi<?>> relation(Authentication auth, @PathVariable String targetUsername) {

        var dto = friendshipService.getRelationStatus(auth.getName(), targetUsername);
        return ResponseEntity.ok(new ResponseApi<>("relation status", dto, true));
    }

}
