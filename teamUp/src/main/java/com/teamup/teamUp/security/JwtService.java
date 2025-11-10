package com.teamup.teamUp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Service

//genereaza si verifica token-urile
public class JwtService {
    private final JwtProperties props;

    //decodifica secretul base64 si timpul de expirare al token-ului
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
    }

    //construiesc un jwt semnat
    public String generate(String subject, Map<String,?> claims) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.expMinutes(), ChronoUnit.MINUTES)))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    //verific tokenul
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
    }

    //extract the username (subject) from the token
    public String extractUsername(String token) {
        try {
            return parse(token).getPayload().getSubject();
        } catch (Exception e) {
            return null; // return null if invalid or expired
        }
    }

    //check if the token is valid and not expired
    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            var claims = parse(token).getPayload();
            var username = claims.getSubject();
            var exp = claims.getExpiration().toInstant();
            return username.equals(expectedUsername) && exp.isAfter(Instant.now());
        } catch (Exception e) {
            return false;
        }
    }
}
