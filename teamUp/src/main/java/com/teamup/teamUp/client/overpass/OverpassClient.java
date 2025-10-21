package com.teamup.teamUp.client.overpass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OverpassClient {
    private static final URI ENDPOINT = URI.create("https://overpass-api.de/api/interpreter");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)).build();
    private final ObjectMapper om = new ObjectMapper();

    // interogare pe BBox: terenuri de fotbal, stadioane, sports_centre
    private String buildQuery(double minLat, double minLng, double maxLat, double maxLng) {
        String bbox = minLat + "," + minLng + "," + maxLat + "," + maxLng;
        return """
        [out:json][timeout:25];
        (
          node["leisure"="pitch"]["sport"~"soccer|football"](%s);
          way["leisure"="pitch"]["sport"~"soccer|football"](%s);
          relation["leisure"="pitch"]["sport"~"soccer|football"](%s);

          node["amenity"="stadium"](%s);
          way["amenity"="stadium"](%s);
          relation["amenity"="stadium"](%s);

          node["leisure"="sports_centre"](%s);
          way["leisure"="sports_centre"](%s);
          relation["leisure"="sports_centre"](%s);
        );
        out tags geom;
        """.formatted(bbox,bbox,bbox, bbox,bbox,bbox, bbox,bbox,bbox);
    }


    //trimiterea cererii si parsarea raspunsului
    public OverpassResponse fetchBBox(double minLat, double minLng, double maxLat, double maxLng) {
        String ql = buildQuery(minLat, minLng, maxLat, maxLng);
        HttpRequest req = HttpRequest.newBuilder(ENDPOINT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "TeamUp/1.0 (contact: you@teamup.local)")
                .POST(HttpRequest.BodyPublishers.ofString("data=" + URLEncoder.encode(ql, StandardCharsets.UTF_8)))
                .build();
        try {
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 == 2) {
                return om.readValue(res.body(), OverpassResponse.class);
            }
            throw new RuntimeException("Overpass error " + res.statusCode() + ": " + res.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Overpass call failed", e);
        }
    }

    // ---- DTO-uri răspuns Overpass
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverpassResponse { public List<Element> elements; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Element {
        public long id;
        public String type;
        public Double lat;
        public Double lon;
        public Map<String,Object> tags;

        public List<Coord> geometry;

        public List<Member> members;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Coord { public Double lat; public Double lon; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Member {
            public String type;
            public long ref;
            public String role;
        }
    }
}
