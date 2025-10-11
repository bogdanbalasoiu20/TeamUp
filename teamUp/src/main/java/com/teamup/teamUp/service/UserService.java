package com.teamup.teamUp.service;


import com.teamup.teamUp.exceptions.BusinessConflictException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.user.ChangePasswordRequestDto;
import com.teamup.teamUp.model.dto.user.UpdateProfileRequestDto;
import com.teamup.teamUp.model.dto.user.UserProfileResponseDto;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly=true)
    public User findByUsername(String username){
        return userRepository.findByUsernameIgnoreCase(username).orElseThrow(()-> new NotFoundException("User not found with username: "+username));
    }

    @Transactional
    public User updateMyProfile(String username, UpdateProfileRequestDto request){
        User me = userRepository.findByUsernameIgnoreCase(username).orElseThrow(()-> new NotFoundException("User not found with username: "+username));

        if(request.birthday()!=null)
            me.setBirthday(request.birthday());
        if(request.phoneNumber()!=null)
            me.setPhoneNumber(request.phoneNumber().trim());
        if(request.position()!=null)
            me.setPosition(request.position());
        if(request.city()!=null)
            me.setCity(request.city().trim());
        if(request.description()!=null)
            me.setDescription(request.description().trim());

        return me;
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto request, String username){
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow(()-> new NotFoundException("User not found with username: "+username));

        if(!passwordEncoder.matches(request.currentPassword(),user.getPasswordHash())){
            throw new BadCredentialsException("Incorrect current password");
        }

        if(passwordEncoder.matches(request.newPassword(),user.getPasswordHash())){
            throw new BusinessConflictException("New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);

        userRepository.save(user);
    }
}
