package com.teamup.teamUp.controller;

import com.teamup.teamUp.client.nominatim.NominatimClient;
import com.teamup.teamUp.mapper.VenueMapper;
import com.teamup.teamUp.model.dto.venue.VenueAdminUpdateRequestDto;
import com.teamup.teamUp.model.dto.venue.VenueResponseDto;
import com.teamup.teamUp.model.dto.venue.VenueUpsertRequestDto;
import com.teamup.teamUp.model.entity.Venue;
import com.teamup.teamUp.service.VenueService;
import com.teamup.teamUp.service.importers.VenueImportService;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService venueService;
    private final VenueMapper venueMapper;
    private final VenueImportService venueImportService;
    private final NominatimClient nominatimClient;

    @Autowired
    public VenueController(VenueService venueService, VenueMapper venueMapper, VenueImportService venueImportService,NominatimClient nominatimClient) {
        this.venueService = venueService;
        this.venueMapper = venueMapper;
        this.venueImportService = venueImportService;
        this.nominatimClient = nominatimClient;
    }

    @GetMapping
    public ResponseEntity<ResponseApi<Page<VenueResponseDto>>> search(@RequestParam(required = false) String city,
                                                         @RequestParam(required = false,name = "q") String query,
                                                         @RequestParam(defaultValue = "true") boolean activeOnly,
                                                         @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(new ResponseApi<>("venues pagination generated",venueService.search(city,query,activeOnly,pageable),true));
    }

    @GetMapping("/search-map")
    public ResponseEntity<ResponseApi<List<VenueResponseDto>>> searchForMap(@RequestParam(required = false) String city,
                                                                            @RequestParam(required = false,name = "q") String query,
                                                                            @RequestParam(defaultValue = "true") boolean activeOnly,
                                                                            @RequestParam(defaultValue = "50") int limit){
        var page = venueService.search(city,query,activeOnly, PageRequest.of(0, Math.min(limit, 100), Sort.by("name").ascending()));//cer doar primele N rezultate potrivite pentru harta(nu paginez)
        return ResponseEntity.ok(new ResponseApi<>("venues generated",page.getContent(),true));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ResponseApi<List<VenueResponseDto>>> nearby(@RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
                                                                      @RequestParam @DecimalMin("-180") @DecimalMax("180") double lng,
                                                                      @RequestParam(defaultValue = "2000") @Positive double radiusMeters,
                                                                      @RequestParam(defaultValue = "300") @Positive @Max(500) int limit){
        var data = venueService.nearby(lat,lng,radiusMeters,limit);
        var dataToDto = data.stream().map(venueMapper::toDto).toList();
        return ResponseEntity.ok(new ResponseApi<>("venues nearby",dataToDto,true));
    }

    @PostMapping("/upsert")
    public ResponseEntity<ResponseApi<VenueResponseDto>> upsert(@Valid @RequestBody VenueUpsertRequestDto request){
        Venue venueUpserted = venueService.upsert(request);
        return ResponseEntity.ok(new ResponseApi<>("Upserted venue",venueMapper.toDto(venueUpserted),true));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import/osm")
    public ResponseEntity<ResponseApi<VenueImportService.ImportResult>> importOsm(
            @RequestParam double minLat,
            @RequestParam double minLng,
            @RequestParam double maxLat,
            @RequestParam double maxLng
    ){
        var result = venueImportService.importFromBBox(minLat, minLng, maxLat, maxLng);
        return ResponseEntity.ok(new ResponseApi<>("OSM import done", result, true));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<VenueResponseDto>> getById(@PathVariable UUID id){
        Venue venue = venueService.findById(id);
        return ResponseEntity.ok(new ResponseApi<>("Venue found",venueMapper.toDto(venue),true));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseApi<VenueResponseDto>> update(@PathVariable UUID id, @Valid @RequestBody VenueAdminUpdateRequestDto request){
        Venue venue = venueService.update(id,request);
        return ResponseEntity.ok(new ResponseApi<>("Venue updated by admin", venueMapper.toDto(venue),true));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/active")
    public ResponseEntity<ResponseApi<Void>> setActive(@PathVariable UUID id, @RequestParam Boolean isActive){
        venueService.setActive(id,isActive);
        return ResponseEntity.ok(new ResponseApi<>(isActive?"Venue activated":"Venue deactivated",null,true));
    }

    @GetMapping("/nearby-bbox")
    public ResponseEntity<ResponseApi<List<VenueResponseDto>>> inBBox(@RequestParam @DecimalMin("-90") @DecimalMax("90") double minLat,
                                                                      @RequestParam @DecimalMin("-180") @DecimalMax("180") double minLng,
                                                                      @RequestParam @DecimalMin("-90") @DecimalMax("90") double maxLat,
                                                                      @RequestParam @DecimalMin("-180") @DecimalMax("180") double maxLng,
                                                                      @RequestParam(defaultValue="300") @Positive @Max(500) int limit){
        var list = venueService.inBBox(minLat, minLng, maxLat, maxLng, limit)
                .stream().map(venueMapper::toDto).toList();
        return ResponseEntity.ok(new ResponseApi<>("ok",list,true));
    }

    @GetMapping("/suggest")
    public ResponseEntity<ResponseApi<List<VenueResponseDto>>> suggest(@RequestParam String q,
                                                                       @RequestParam(defaultValue = "10") @Max(20) int limit,
                                                                       @RequestParam (required = false) String cityHint){
        var venues = venueService.suggest(q,limit,cityHint);
        var dtos = venues.stream().map(venueMapper::toDto).toList();
        return ResponseEntity.ok(new ResponseApi<>("venues suggested",dtos,true));
    }

    @GetMapping("/{id}/shape")
    public ResponseEntity<String> shape(@PathVariable UUID id){
        String shape = venueService.getShape(id);
        if(shape == null) return ResponseEntity.noContent().build();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(shape);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import/osm-by-city")
    public ResponseEntity<ResponseApi<VenueImportService.ImportResult>> importOsmByCity(
            @RequestParam String city
    ){
        return nominatimClient.cityBbox(city.trim())
                .map(bb -> {
                    var r = venueImportService.importFromBBox(bb[0], bb[1], bb[2], bb[3]);
                    return ResponseEntity.ok(new ResponseApi<>("OSM import done ("+city+")", r, true));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(new ResponseApi<>("City not found", null, false)));
    }

}
