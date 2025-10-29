package com.teamup.teamUp.service.importers;

import com.teamup.teamUp.client.overpass.OverpassClient;
import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.OsmMapper;
import com.teamup.teamUp.repository.CityRepository;
import com.teamup.teamUp.repository.VenueRepository;
import com.teamup.teamUp.service.VenueService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueImportService {
    private final OverpassClient overpass;
    private final OsmMapper osmMapper;
    private final VenueService venueService;
    private final VenueRepository venueRepository;
    private final CityRepository cityRepository;

    @Transactional
    public ImportResult importFromBBox(double minLat, double minLng, double maxLat, double maxLng) {
        if (minLat > maxLat || minLng > maxLng) throw new IllegalArgumentException("Invalid bbox");

        var resp = overpass.fetchBBox(minLat, minLng, maxLat, maxLng);
        int created = 0, updated = 0;

        if (resp.elements == null || resp.elements.isEmpty()) {
            return new ImportResult(created, updated);
        }

        for (var el : resp.elements) {
            var mapped = osmMapper.toUpsertWithShape(el);
            var dto    = mapped.dto();
            var shape  = mapped.shapeGeoJson();

            boolean existed = venueRepository
                    .findByOsmTypeAndOsmId(dto.osmType(), dto.osmId())
                    .isPresent();

            var saved = venueService.upsert(dto);

            if (shape != null) {
                venueRepository.updateAreaGeomFromGeoJson(saved.getId(), shape);
            }

            if (existed) updated++; else created++;
        }
        return new ImportResult(created, updated);
    }

    public record ImportResult(int created, int updated) {}


    @Transactional
    public ImportResult importFromCitySlug(String slug) {
        String s = slug == null ? "" : slug.trim().toLowerCase();
        var city = cityRepository.findBySlug(s)
                .orElseThrow(() -> new NotFoundException("City not found in DB: " + slug));


        if (city.getMinLat() == null || city.getMinLng() == null ||
                city.getMaxLat() == null || city.getMaxLng() == null) {
            throw new BadRequestException("City bbox is not set for slug: " + slug);
        }
        if (city.getMinLat() > city.getMaxLat() || city.getMinLng() > city.getMaxLng()) {
            throw new BadRequestException("Invalid bbox for city: " + slug);
        }

        return importFromBBox(city.getMinLat(), city.getMinLng(), city.getMaxLat(), city.getMaxLng());
    }
}


