package com.teamup.teamUp.controller;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.model.dto.friend.FriendRequestActionDto;
import com.teamup.teamUp.model.dto.friend.FriendRequestCreateDto;
import com.teamup.teamUp.model.dto.friend.FriendRequestResponseDto;
import com.teamup.teamUp.model.dto.friend.UserSearchResponseDto;
import com.teamup.teamUp.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/friends/requests")
public class FriendRequestController {
    private final FriendshipService friendshipService;

    @Autowired
    public FriendRequestController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping
    public ResponseEntity<ResponseApi<FriendRequestResponseDto>> send(@Valid @RequestBody FriendRequestCreateDto request, Authentication auth){
        var response = friendshipService.sendRequest(auth.getName(),request);
        return ResponseEntity.ok(new ResponseApi<>("Friend request sent successfully",response,true));
    }

    @GetMapping("/incoming")
    public ResponseEntity<ResponseApi<Page<FriendRequestResponseDto>>> getIncoming(Authentication auth, @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var page = friendshipService.getIncomingRequests(auth.getName(), pageable);
        return ResponseEntity.ok(new ResponseApi<>(
                "Incoming friend requests fetched successfully", page, true));
    }

    @GetMapping("/outgoing")
    public ResponseEntity<ResponseApi<Page<FriendRequestResponseDto>>> getOutgoing(Authentication auth, @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var page = friendshipService.getOutgoingRequests(auth.getName(), pageable);
        return ResponseEntity.ok(new ResponseApi<>(
                "Outgoing friend requests fetched successfully", page, true));
    }

    @PatchMapping(value = "/{id}/respond", consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseApi<Void>> respond(@PathVariable UUID id, @RequestBody FriendRequestActionDto action, Authentication auth) {
        if (action == null || action.getAccept() == null)
            throw new BadRequestException("Missing accept field");

        friendshipService.respondToRequest(id, auth.getName(), action.getAccept());
        return ResponseEntity.ok(new ResponseApi<>("OK", null, true));
    }


    @GetMapping("/search")
    public ResponseEntity<ResponseApi<Page<UserSearchResponseDto>>> search(
            @RequestParam String query,
            Authentication auth,
            @PageableDefault(size = 20) Pageable pageable) {

        var page = friendshipService.searchUsers(auth.getName(), query, pageable);
        return ResponseEntity.ok(new ResponseApi<>("Users fetched", page, true));
    }


}
