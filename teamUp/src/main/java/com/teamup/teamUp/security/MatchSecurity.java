package com.teamup.teamUp.security;

import com.teamup.teamUp.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("matchSecurity")
@RequiredArgsConstructor
public class MatchSecurity {
    private final MatchRepository matchRepository;

    public boolean canEdit(UUID matchId, Authentication auth){
        if(auth ==null || !auth.isAuthenticated()){
            return false;
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(isAdmin) {
            return true;
        }

        String username = auth.getName();
        return matchRepository.findCreatorUsernameById(matchId).map(u-> u.equalsIgnoreCase(username)).orElse(false);
    }
}
