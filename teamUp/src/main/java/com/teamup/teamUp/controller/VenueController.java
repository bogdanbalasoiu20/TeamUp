package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.venue.VenueResponseDto;
import com.teamup.teamUp.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ResponseApi<VenueResponseDto>> getVenue(@PathVariable UUID id){
//
//    }
}
