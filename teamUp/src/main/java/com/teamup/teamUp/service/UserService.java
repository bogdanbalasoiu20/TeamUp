package com.teamup.teamUp.service;


import com.teamup.teamUp.exceptions.BusinessConflictException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.user.ChangePasswordRequestDto;
import com.teamup.teamUp.model.dto.user.UpdateProfileRequestDto;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RatingUpdateService ratingUpdateService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RatingUpdateService ratingUpdateService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ratingUpdateService = ratingUpdateService;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username){
        return userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
    }

    @Transactional
    public User updateMyProfile(String username, UpdateProfileRequestDto request){
        User me = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        if (request.birthday() != null) me.setBirthday(request.birthday());
        if (request.phoneNumber() != null) me.setPhoneNumber(request.phoneNumber().trim());
        if (request.position() != null && request.position() != me.getPosition()) {
            me.setPosition(request.position());

            ratingUpdateService.recalculateOverallForUser(
                    me.getId(),
                    request.position()
            );
        }
        if (request.city() != null) me.setCity(request.city().trim());
        if (request.description() != null) me.setDescription(request.description().trim());

        return me;
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto request, String username){
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect current password");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessConflictException("New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        Integer tv = user.getTokenVersion();
        user.setTokenVersion(tv == null ? 1 : tv + 1);

        userRepository.save(user);
    }

    @Transactional
    public void deleteProfile(String username){
        User user = findByUsername(username);
        user.setDeleted(true);
        // safe increment
        Integer tv = user.getTokenVersion();
        user.setTokenVersion(tv == null ? 1 : tv + 1);

        String shortId = user.getId().toString().replace("-", "").substring(0, 8);
        String suffix = "del" + shortId;

        String base = user.getUsername().toLowerCase(Locale.ROOT);
        int MAX = 20;
        int keep = Math.max(1, MAX - suffix.length());
        user.setUsername((base.length() > keep ? base.substring(0, keep) : base) + suffix);

        String email = user.getEmail();
        int at = email.lastIndexOf('@');
        if (at > 0) {
            String local = email.substring(0, at);
            String domain = email.substring(at + 1);
            user.setEmail(local + "+del" + shortId + "@" + domain);
        } else {
            user.setEmail(email + "+del" + shortId);
        }
        userRepository.save(user);
    }

    @Transactional
    public void logout(String username){
        User user = findByUsername(username);
        Integer tv = user.getTokenVersion();
        user.setTokenVersion(tv == null ? 1 : tv + 1);
        userRepository.save(user);
    }
}

