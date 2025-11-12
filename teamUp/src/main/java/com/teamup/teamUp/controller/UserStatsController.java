package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.entity.UserStatsView;
import com.teamup.teamUp.service.PlayerStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserStatsController {

    private final PlayerStatsService playerStatsService;

    //intoarce stats-urile pentru un User pentru a fi folosite in cardul de fifa
    @GetMapping("/{username}/stats")
    public ResponseEntity<ResponseApi<UserStatsView>> getUserStats(@PathVariable String username) {
        var stats = playerStatsService.getUserStats(username);
        return ResponseEntity.ok(new ResponseApi<>("User stats fetched successfully", stats, true));
    }
}
