package com.teamup.teamUp.security;

import com.teamup.teamUp.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

//Scop: să „agațe” fiecare request, să caute un Authorization: Bearer ..., să valideze token-ul și să seteze user-ul curent.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        var header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            var token = header.substring(7);
            try {
                var jws = jwt.parse(token);
                var claims = jws.getPayload();


                String principal = claims.get("username", String.class);
                if (principal == null) principal = claims.getSubject();

                Integer tokenVersionClaim = claims.get("tokenVersion", Integer.class);
                if (tokenVersionClaim == null) tokenVersionClaim = 0;
                Long pwdChangedAtClaim = claims.get("pwdChangedAt", Long.class);

                var user = userRepository.findByUsernameIgnoreCase(principal)
                        .orElseThrow(() -> new JwtException("User not found"));

                Integer currentVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
                if (!currentVersion.equals(tokenVersionClaim)) {
                    throw new JwtException("Token version mismatch");
                }
                if (pwdChangedAtClaim != null && user.getPasswordChangedAt() != null) {
                    long dbEpoch = user.getPasswordChangedAt().getEpochSecond();
                    if (pwdChangedAtClaim < dbEpoch) {
                        throw new JwtException("Password changed after token was issued");
                    }
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (JwtException ignored) {}
        }
        chain.doFilter(req, res);
    }
}
