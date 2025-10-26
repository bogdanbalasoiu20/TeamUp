package com.teamup.teamUp.security;


import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getServletPath();

        return p.startsWith("/api/auth/")
                || p.startsWith("/public/")
                || HttpMethod.OPTIONS.matches(req.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Jws<Claims> jws = jwt.parse(token);
                Claims claims = jws.getPayload();

                String principal = claims.get("username", String.class);
                if (principal == null || principal.isBlank()) {
                    principal = claims.getSubject();
                }
                if (principal == null || principal.isBlank()) {
                    throw new JwtException("Missing subject (username)");
                }
                principal = principal.trim();

                Number tvClaim = claims.get("tokenVersion", Number.class);
                int tvTok = (tvClaim == null ? 0 : tvClaim.intValue());

                Number pwdClaim = claims.get("pwdChangedAt", Number.class);
                long pwdTok = (pwdClaim == null ? 0L : pwdClaim.longValue());

                User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(principal)
                        .orElseThrow(() -> new JwtException("User not found"));

                try {
                    var deletedField = User.class.getDeclaredField("deleted");
                    deletedField.setAccessible(true);
                    Object deletedVal = deletedField.get(user);
                    if (deletedVal instanceof Boolean && (Boolean) deletedVal) {
                        throw new JwtException("User deleted");
                    }
                } catch (NoSuchFieldException ignored) {
                } catch (IllegalAccessException e) {
                    throw new JwtException("User access error");
                }

                int tvUser = (user.getTokenVersion() == null ? 0 : user.getTokenVersion());
                if (tvUser != tvTok) {
                    throw new JwtException("Token version mismatch");
                }

                long pwdUser = 0L;
                if (user.getPasswordChangedAt() != null) {
                    pwdUser = user.getPasswordChangedAt().getEpochSecond();
                }
                if (pwdUser > 0 && pwdTok > 0 && pwdTok < pwdUser) {
                    throw new JwtException("Password changed after token was issued");
                }

                log.info("JWT OK -> username={}, role={}", user.getUsername(), user.getRole());

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String rawRole = (user.getRole() == null ? "USER" : user.getRole().toString());
                    String roleWithPrefix = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;

                    List<GrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority(roleWithPrefix));

                    var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (JwtException ex) {
                log.warn("JWT rejected: {}", ex.getMessage());
                req.setAttribute("jwt_error", ex.getMessage());
                SecurityContextHolder.clearContext();
                log.debug("JWT rejected: {}", ex.getMessage());
            } catch (RuntimeException ex) {
                req.setAttribute("jwt_error", "invalid_token");
                SecurityContextHolder.clearContext();
                log.debug("JWT processing error", ex);
            }
        }

        chain.doFilter(req, res);
    }
}
