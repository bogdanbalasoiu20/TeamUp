package com.teamup.teamUp.service;


import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.user.UpdateProfileRequestDto;
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

    @Transactional
    public User updateMyProfile(String username, UpdateProfileRequestDto request){
        User me = userRepository.findByUsernameIgnoreCase(username).orElseThrow(()-> new NotFoundException("User not found with username: "+username));

        if(request.birthday()!=null)
            me.setBirthday(request.birthday());
        if(request.phoneNumber()!=null)
            me.setPhoneNumber(request.phoneNumber().trim());
        if(request.position()!=null)
            me.setPosition(request.position().trim());
        if(request.city()!=null)
            me.setCity(request.city().trim());
        if(request.description()!=null)
            me.setDescription(request.description().trim());

        return me;
    }
}
