package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.FriendMapper;
import com.teamup.teamUp.model.dto.friend.FriendRequestCreateDto;
import com.teamup.teamUp.model.dto.friend.FriendRequestResponseDto;
import com.teamup.teamUp.model.dto.friend.FriendshipResponseDto;
import com.teamup.teamUp.model.entity.FriendRequest;
import com.teamup.teamUp.model.entity.Friendship;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.FriendRequestStatus;
import com.teamup.teamUp.repository.FriendRequestRepository;
import com.teamup.teamUp.repository.FriendshipRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, FriendRequestRepository friendRequestRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FriendRequestResponseDto sendRequest(String requesterUsername, FriendRequestCreateDto dto) {
        User requester = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(requesterUsername)
                .orElseThrow(() -> new NotFoundException("Requester not found"));
        User addressee = userRepository.findById(dto.addresseeId())
                .orElseThrow(() -> new NotFoundException("Addressee not found"));

        if (requester.getId().equals(addressee.getId()))
            throw new BadRequestException("You cannot send a friend request to yourself");

        if (friendshipRepository.existsByUserAIdAndUserBId(requester.getId(), addressee.getId()))
            throw new BadRequestException("You are already friends");

        boolean pendingExists = friendRequestRepository.findBetweenUsers(requester.getId(), addressee.getId())
                .filter(fr -> fr.getStatus() == FriendRequestStatus.PENDING)
                .isPresent();
        if (pendingExists)
            throw new BadRequestException("Pending request already exists between users");

        FriendRequest request = FriendRequest.builder()
                .requester(requester)
                .addressee(addressee)
                .message(dto.message())
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(request);
        return FriendMapper.toFriendRequestResponseDto(request);
    }

    @Transactional
    public void respondToRequest(UUID requestId, String responderUsername, boolean accept) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getAddressee().getUsername().equalsIgnoreCase(responderUsername))
            throw new BadRequestException("You cannot respond to a request not addressed to you");

        request.setStatus(accept ? FriendRequestStatus.ACCEPTED : FriendRequestStatus.DECLINED);
        request.setRespondedAt(Instant.now());

        if (accept) {
            friendshipRepository.save(Friendship.builder()
                    .userA(request.getRequester())
                    .userB(request.getAddressee())
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public Page<FriendshipResponseDto> listFriends(String username, Pageable pageable) {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<FriendshipResponseDto> friends = friendshipRepository.findAllByUser(user.getId())
                .stream()
                .map(f -> FriendMapper.toFriendshipResponseDto(f, username))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), friends.size());
        List<FriendshipResponseDto> paged = friends.subList(start, end);

        return new PageImpl<>(paged, pageable, friends.size());
    }


    @Transactional(readOnly = true)
    public Page<FriendRequestResponseDto> getIncomingRequests(String username, Pageable pageable) {
        UUID userId = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getId();

        List<FriendRequestResponseDto> list = friendRequestRepository
                .findAllByAddresseeIdAndStatus(userId, FriendRequestStatus.PENDING)
                .stream().map(FriendMapper::toFriendRequestResponseDto).toList();

        return new PageImpl<>(list, pageable, list.size());
    }


    @Transactional(readOnly = true)
    public Page<FriendRequestResponseDto> getOutgoingRequests(String username, Pageable pageable) {
        UUID userId = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getId();

        List<FriendRequestResponseDto> list = friendRequestRepository
                .findAllByRequesterIdAndStatus(userId, FriendRequestStatus.PENDING)
                .stream().map(FriendMapper::toFriendRequestResponseDto).toList();

        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional
    public void removeFriend(String username, UUID friendId) {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        UUID userId = user.getId();

        friendshipRepository.findAllByUser(userId).stream()
                .filter(f -> f.getUserA().getId().equals(friendId) || f.getUserB().getId().equals(friendId))
                .findFirst()
                .ifPresentOrElse(
                        friendshipRepository::delete,
                        () -> { throw new NotFoundException("Friendship not found"); }
                );
    }
}
