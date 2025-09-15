package com.teamup.teamUp.controller;


import com.teamup.teamUp.model.TestEntity;
import com.teamup.teamUp.repository.TestRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class TestController {
    private TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @PostMapping
    public TestEntity save(@RequestBody TestEntity testEntity) {
        return testRepository.save(testEntity);
    }

    @GetMapping
    public List<TestEntity> findAll(){
        return testRepository.findAll();
    }

}
