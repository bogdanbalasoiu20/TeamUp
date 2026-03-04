package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.dto.ChemistryResult;
import com.teamup.teamUp.model.entity.PlayerChemistry;
import com.teamup.teamUp.repository.PlayerChemistryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlayerChemistryCacheService {

    private final PlayerChemistryRepository repo;
    private final ChemistryService chemistryService;

    public PlayerChemistryCacheService(PlayerChemistryRepository repo, ChemistryService chemistryService) {
        this.repo = repo;
        this.chemistryService = chemistryService;
    }

    public int getChemistry(UUID a, UUID b) {
        UUID userA = a.compareTo(b) < 0 ? a : b;
        UUID userB = a.compareTo(b) < 0 ? b : a;

        Optional<PlayerChemistry> cached = repo.findByUserAAndUserB(userA, userB);

        if (cached.isPresent()) {
            return cached.get().getChemistryScore();
        }

        ChemistryResult result = chemistryService.compute(userA, userB);

        PlayerChemistry entity = PlayerChemistry.builder()
                .userA(userA)
                .userB(userB)
                .chemistryScore(result.score())
                .updatedAt(LocalDateTime.now())
                .build();

        repo.save(entity);

        return result.score();
    }
}
