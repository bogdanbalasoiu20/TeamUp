package com.teamup.teamUp.controller;


import com.teamup.teamUp.model.dto.user.UserProfileResponseDto;
import com.teamup.teamUp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<ResponseApi<UserProfileResponseDto>> getUserProfile(@PathVariable String username, Authentication auth){
        String requester = (auth!=null)?auth.getName():null;
        UserProfileResponseDto response = userService.getUserProfile(username,requester);
        return ResponseEntity.ok(new ResponseApi<>("User profile retrieved successfully",response,true));
    }


}
