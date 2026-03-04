package com.teamup.teamUp.chemistry.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TeamChemistryService {
    private final PlayerChemistryCacheService chemistryCache;

    public TeamChemistryService(PlayerChemistryCacheService chemistryCache) {
        this.chemistryCache = chemistryCache;
    }

    public double computeTeamChemistry(List<UUID> players) {
        double sum = 0;
        int pairs = 0;

        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                int chem = chemistryCache.getChemistry(
                        players.get(i),
                        players.get(j)
                );
                sum += chem;
                pairs++;
            }
        }

        return pairs == 0 ? 0 : sum / pairs;
    }
}
