package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.venue.VenueResponseDto;
import com.teamup.teamUp.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public ResponseEntity<Page<VenueResponseDto>> search(@RequestParam(required = false) String city,
                                                         @RequestParam(required = false,name = "q") String query,
                                                         @RequestParam(defaultValue = "true") boolean activeOnly,
                                                         @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(venueService.search(city,query,activeOnly,pageable));
    }
}
