package com.teamup.teamUp.controller;


import com.teamup.teamUp.mapper.UserMapper;
import com.teamup.teamUp.model.dto.user.UserProfileResponseDto;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/{username}")
    public ResponseEntity<ResponseApi<UserProfileResponseDto>> getProfile(@PathVariable String username, Authentication auth){

        User user = userService.findByUsername(username.trim());
        boolean isMyProfile = (auth !=null) && user.getUsername().equalsIgnoreCase(auth.getName());
        UserProfileResponseDto response = userMapper.toProfileDto(user,isMyProfile);

        return ResponseEntity.ok(new ResponseApi<>("User profile retrieved successfully",response,true));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseApi<UserProfileResponseDto>> me(Authentication auth){
        User user =  userService.findByUsername(auth.getName().trim());
        UserProfileResponseDto response = userMapper.toProfileDto(user,true);
        return ResponseEntity.ok(new ResponseApi<>("User profile retrieved successfully",response,true));
    }
}
