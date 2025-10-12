package com.teamup.teamUp.controller;


import com.teamup.teamUp.mapper.UserMapper;
import com.teamup.teamUp.model.dto.user.ChangePasswordRequestDto;
import com.teamup.teamUp.model.dto.user.UpdateProfileRequestDto;
import com.teamup.teamUp.model.dto.user.UserProfileResponseDto;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/me")
    public ResponseEntity<ResponseApi<UserProfileResponseDto>> updateMyProfile(@Valid @RequestBody UpdateProfileRequestDto request, Authentication auth){
        User updatedUser = userService.updateMyProfile(auth.getName(),request);
        UserProfileResponseDto response = userMapper.toProfileDto(updatedUser,true);
        return ResponseEntity.ok(new ResponseApi<>("User profile updated successfully",response,true));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ResponseApi<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDto request, Authentication auth){
        userService.changePassword(request, auth.getName());
        return ResponseEntity.ok(new ResponseApi<>("Password changed successfully",null,true));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ResponseApi<Void>> deleteMyProfile(Authentication auth){
        String username = auth.getName();
        userService.deleteProfile(username);
        return ResponseEntity.ok(new ResponseApi<>("User deleted successfully",null,true));
    }
}
