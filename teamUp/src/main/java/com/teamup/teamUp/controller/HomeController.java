package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.dashboard.HomeUpcomingResponse;
import com.teamup.teamUp.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/upcoming")
    public ResponseEntity<ResponseApi<HomeUpcomingResponse>> getUpcoming(Authentication auth) {
        return ResponseEntity.ok(new ResponseApi<>("Upcoming data fetched successfully", homeService.getUpcomingForCurrentUser(auth.getName()),true));
    }
}
