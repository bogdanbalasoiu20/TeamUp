package com.teamup.teamUp.service.importers;

import com.teamup.teamUp.client.overpass.OverpassClient;
import com.teamup.teamUp.mapper.OsmMapper;
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
}


