package com.teamup.teamUp.controller;

import com.teamup.teamUp.mapper.MatchMapper;
import com.teamup.teamUp.model.dto.match.MatchCreateRequestDto;
import com.teamup.teamUp.model.dto.match.MatchMapPinDto;
import com.teamup.teamUp.model.dto.match.MatchResponseDto;
import com.teamup.teamUp.model.dto.match.MatchUpdateRequestDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.service.MatchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/matches")
public class MatchController {
    private final MatchService matchService;
    private final MatchMapper matchMapper;

    @Autowired
    public MatchController(MatchService matchService, MatchMapper matchMapper) {
        this.matchService = matchService;
        this.matchMapper = matchMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<MatchResponseDto>> getById(@PathVariable UUID id){
        Match match = matchService.findById(id);
        return ResponseEntity.ok(new ResponseApi<>("Match found successfully",matchMapper.toDto(match),true));
    }

    @PostMapping
    public ResponseEntity<ResponseApi<MatchResponseDto>> create(@Valid @RequestBody MatchCreateRequestDto request, Authentication auth){
        Match match = matchService.create(request,auth.getName());
        return ResponseEntity.created(URI.create("/api/matches/" + match.getId())).body(new ResponseApi<>("Match created successfully",matchMapper.toDto(match),true));
    }

    @PreAuthorize("@matchSecurity.canEdit(#id, authentication)")
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseApi<MatchResponseDto>> update(@PathVariable UUID id, @RequestBody MatchUpdateRequestDto request){
        Match matchUpdated = matchService.update(id, request);
        return ResponseEntity.ok(new ResponseApi<>("Match updated successfully",matchMapper.toDto(matchUpdated),true));
    }

    @PreAuthorize("@matchSecurity.canEdit(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable UUID id){
        matchService.delete(id);
        return  ResponseEntity.ok(new  ResponseApi<>("Match deleted successfully",null,true));
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseApi<Page<MatchResponseDto>>> search(@RequestParam(required = false) String city,
                                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
                                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
                                                                      @PageableDefault(size = 20, sort="startsAt", direction = Sort.Direction.ASC) Pageable pageable){
        var page = matchService.search(city, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(new ResponseApi<>("Matches found successfully",page,true));


    }

    public ResponseEntity<ResponseApi<List<MatchMapPinDto>>> nearbyBBox(@RequestParam double minLat,
                                                                        @RequestParam double minLng,
                                                                        @RequestParam double maxLat,
                                                                        @RequestParam double maxLng,
                                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
                                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
                                                                        @RequestParam(defaultValue = "300") @Max(500) int limit) {


        List<MatchMapPinDto> pins = matchService.nearbyPins(minLat, minLng, maxLat, maxLng, dateFrom, dateTo, limit);
        return ResponseEntity.ok(new ResponseApi<>("pins", pins, true));
    }
}
