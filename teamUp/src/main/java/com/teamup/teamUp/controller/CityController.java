package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.city.CityDto;
import com.teamup.teamUp.model.dto.city.CityUpsertRequestDto;
import com.teamup.teamUp.service.CityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cities")
public class CityController {
    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ResponseApi<CityDto>> getBySlug(@Valid  @PathVariable String slug){
        CityDto response = cityService.getBySlug(slug);
        return ResponseEntity.ok(new ResponseApi<>("City found successfully",response,true));
    }

    @GetMapping("/suggest")
    public ResponseEntity<ResponseApi<List<CityDto>>> suggest(@RequestParam String q,
                                                              @RequestParam(defaultValue = "10") @Max(20)int limit){
        return ResponseEntity.ok(new ResponseApi<>("Cities suggested",cityService.suggest(q,limit),true));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upsert")
    public ResponseEntity<ResponseApi<CityDto>> upsert(@Valid @RequestBody CityUpsertRequestDto request) {
        return ResponseEntity.ok(new ResponseApi<>("saved", cityService.upsert(request), true));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{slug}/import-geom")
    public ResponseEntity<ResponseApi<Void>> importGeom(@PathVariable String slug){
        cityService.importGeometryByCitySlug(slug);
        return ResponseEntity.ok(new ResponseApi<>("City geometry imported", null, true));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign-venues-by-geom")
    public ResponseEntity<ResponseApi<Integer>> assignVenues(){
        int updated = cityService.assignCitiesToVenuesByGeometry();
        return ResponseEntity.ok(new ResponseApi<>("Venues assigned to cities", updated, true));
    }
}
