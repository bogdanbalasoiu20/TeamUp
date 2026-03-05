package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerChemistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerChemistryRepository extends JpaRepository<PlayerChemistry, UUID> {

    Optional<PlayerChemistry> findByUserAAndUserB(UUID userA, UUID userB);

    @Query("""
        SELECT pc
        FROM PlayerChemistry pc
        WHERE pc.userA = :user
        OR pc.userB = :user
    """)
    List<PlayerChemistry> findAllForUser(UUID user);

    @Query("""
        SELECT pc
        FROM PlayerChemistry pc
        WHERE pc.userA IN :users AND pc.userB IN :users
        """)
    List<PlayerChemistry> findForUsers(List<UUID> users);

}
