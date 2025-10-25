package com.teamup.teamUp.security;

import com.teamup.teamUp.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

//Scop: să „agațe” fiecare request, să caute un Authorization: Bearer ..., să valideze token-ul și să seteze user-ul curent.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        // Exclude endpoints publice / static / auth
        String p = req.getServletPath();
        return p.startsWith("/api/auth/") || p.startsWith("/public/") || "OPTIONS".equals(req.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var jws    = jwt.parse(token);            // să verifice și exp/nbf înăuntru
                var claims = jws.getPayload();

                // 1) Ia sub ca ID (tu emiți token cu sub = userId)
                String sub = claims.getSubject();
                if (sub == null || sub.isBlank()) {
                    throw new JwtException("Missing subject");
                }

                UUID userId = UUID.fromString(sub);
                var user = userRepository.findById(userId)
                        .orElseThrow(() -> new JwtException("User not found"));

                // 2) Blochează conturi șterse
                if (user.isDeleted()) {
                    throw new JwtException("User deleted");
                }

                // 3) Verifică tokenVersion
                Integer tvClaim = claims.get("tokenVersion", Integer.class);
                int tvUser = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
                int tvTok = tvClaim == null ? 0 : tvClaim;
                if (tvUser != tvTok) {
                    throw new JwtException("Token version mismatch");
                }

                // 4) Verifică pwdChangedAt (epoch seconds)
                Long pwdClaim = claims.get("pwdChangedAt", Long.class);
                long pwdUser = user.getPasswordChangedAt() == null ? 0L : user.getPasswordChangedAt().getEpochSecond();
                if (pwdUser > 0 && pwdClaim != null && pwdClaim < pwdUser) {
                    throw new JwtException("Password changed after token was issued");
                }

                // 5) Setează Authentication doar dacă nu e deja setat
                var role = user.getRole(); // UserRole enum: USER / ADMIN
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Dacă ai roluri, pune-le aici; dacă nu, List.of()
                    var auth = new UsernamePasswordAuthenticationToken(
                            user.getUsername(), // principal (poți folosi și un UserDetails propriu)
                            null,
                            authorities
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (JwtException ex) {
                // Opțiunea A (recomandat): marchezi motivul și la access to a protected endpoint
                // AuthenticationEntryPoint va răspunde 401.
                req.setAttribute("jwt_error", ex.getMessage());
                SecurityContextHolder.clearContext();
                // NU trimite răspuns aici: lasă chain-ul și configurarea de security
                // să decidă (endpoint public vs. protejat).
            }
        }

        chain.doFilter(req, res);
    }
}

