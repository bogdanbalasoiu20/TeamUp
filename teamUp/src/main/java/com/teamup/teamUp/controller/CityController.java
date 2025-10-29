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
}
