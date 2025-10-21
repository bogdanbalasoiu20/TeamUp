package com.teamup.teamUp.mapper;

import com.teamup.teamUp.client.overpass.OverpassClient;
import com.teamup.teamUp.model.dto.venue.VenueUpsertRequestDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class OsmMapper {

    public record UpsertWithShape(VenueUpsertRequestDto dto, String shapeGeoJson) {}

    public UpsertWithShape toUpsertWithShape(OverpassClient.Element e) {
        Map<String,Object> tags = e.tags == null ? Map.of() : e.tags;

        String name = str(tags.get("name"));
        if (name == null || name.isBlank()) {
            name = switch (e.type) { case "node" -> "OSM Pitch #"+e.id; case "way" -> "OSM Way #"+e.id; default -> "OSM Relation #"+e.id; };
        }

        Double lat = e.lat;
        Double lng = e.lon;
        if ((lat == null || lng == null) && e.geometry != null && !e.geometry.isEmpty()) {
            double sx=0, sy=0; int n=0;
            for (var c: e.geometry) if (c.lat!=null && c.lon!=null){ sx+=c.lat; sy+=c.lon; n++; }
            if (n>0) { lat = sx/n; lng = sy/n; }
        }

        String phone = firstNonBlank(str(tags.get("phone")), str(tags.get("contact:phone")));
        String city  = str(tags.get("addr:city"));
        String addr  = buildAddress(tags);

        var dto = new VenueUpsertRequestDto(
                name, addr, phone, city,
                lat, lng,
                e.type, e.id,
                tags
        );

        String shape = buildShapeGeoJson(e);

        return new UpsertWithShape(dto, shape);
    }

    private String buildShapeGeoJson(OverpassClient.Element e) {
        if (!"way".equals(e.type) || e.geometry == null || e.geometry.size() < 2) return null;

        var coords = e.geometry.stream()
                .map(c -> List.of(c.lon, c.lat))   // GeoJSON = [lng,lat]
                .toList();

        boolean closed = coords.size() >= 4 &&
                Objects.equals(coords.get(0), coords.get(coords.size()-1));

        if (closed) {
            return """
                   {"type":"Polygon","coordinates":[%s]}
                   """.formatted(toJsonArray(coords));
        } else {
            return """
                   {"type":"LineString","coordinates":%s}
                   """.formatted(toJsonArray(coords));
        }
    }

    private String toJsonArray(List<List<Double>> coords){
        StringBuilder sb = new StringBuilder("[");
        for (int i=0;i<coords.size();i++){
            var p = coords.get(i);
            sb.append("[").append(p.get(0)).append(",").append(p.get(1)).append("]");
            if (i+1<coords.size()) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
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


