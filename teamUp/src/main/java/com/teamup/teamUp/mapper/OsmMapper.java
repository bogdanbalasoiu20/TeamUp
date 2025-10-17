package com.teamup.teamUp.mapper;

import com.teamup.teamUp.client.overpass.OverpassClient;
import com.teamup.teamUp.model.dto.venue.VenueUpsertRequestDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OsmMapper {
    public VenueUpsertRequestDto toUpsert(OverpassClient.Element e) {
        Double lat = e.lat != null ? e.lat : (e.center != null ? e.center.lat : null);
        Double lng = e.lon != null ? e.lon : (e.center != null ? e.center.lon : null);
        Map<String, Object> tags = e.tags == null ? Map.of() : e.tags;

        String name = str(tags.get("name"));
        if (name == null || name.isBlank()) {
            name = switch (e.type) {
                case "node" -> "OSM Pitch #" + e.id;
                case "way"  -> "OSM Way #" + e.id;
                default     -> "OSM Relation #" + e.id;
            };
        }

        String phone = firstNonBlank(str(tags.get("phone")), str(tags.get("contact:phone")));
        String city  = str(tags.get("addr:city"));
        String addr  = buildAddress(tags);

        return new VenueUpsertRequestDto(
                name, addr, phone, city,
                lat, lng,
                e.type, e.id,
                tags
        );
    }

    private String buildAddress(Map<String,Object> t) {
        String street = str(t.get("addr:street"));
        String nr     = str(t.get("addr:housenumber"));
        String city   = str(t.get("addr:city"));
        String pc     = str(t.get("addr:postcode"));
        List<String> parts = new ArrayList<>();
        if (street != null) parts.add(nr == null ? street : street + " " + nr);
        if (city != null)   parts.add(city);
        if (pc != null)     parts.add(pc);
        return String.join(", ", parts);
    }
    private String str(Object o){ return (o instanceof String s) ? s.trim() : null; }
    private String firstNonBlank(String... arr){ for (var s: arr) if (s != null && !s.isBlank()) return s; return null; }
}

