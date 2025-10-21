package com.teamup.teamUp.client.nominatim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Component
public class NominatimClient {
    private static final URI ENDPOINT = URI.create("https://nominatim.openstreetmap.org/search");
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper om = new ObjectMapper();

    public Optional<double[]> cityBbox(String city){
        try {
            String url = ENDPOINT + "?format=json&limit=1&q=" + URLEncoder.encode(city, StandardCharsets.UTF_8);
            var req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent","TeamUp/1.0 (contact: you@teamup.local)")
                    .GET().build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode()/100 != 2) return Optional.empty();
            var arr = om.readTree(res.body());
            if (!arr.isArray() || arr.isEmpty()) return Optional.empty();
            var bb = arr.get(0).get("boundingbox"); // [south, north, west, east]
            double south = bb.get(0).asDouble();
            double north = bb.get(1).asDouble();
            double west  = bb.get(2).asDouble();
            double east  = bb.get(3).asDouble();
            return Optional.of(new double[]{ south, west, north, east });
        } catch (Exception e) { return Optional.empty(); }
    }
}

