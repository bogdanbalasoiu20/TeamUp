package com.teamup.teamUp.service;


import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.user.UserProfileResponseDto;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Transactional(readOnly=true)
    public User findByUsername(String username){
        return userRepository.findByUsernameIgnoreCase(username).orElseThrow(()-> new NotFoundException("User not found with username: "+username));
    }
}
