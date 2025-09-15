package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity,Long> {
}
