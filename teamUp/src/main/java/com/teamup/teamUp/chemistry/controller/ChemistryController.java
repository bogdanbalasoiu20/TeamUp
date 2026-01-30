package com.teamup.teamUp.chemistry.controller;


import com.teamup.teamUp.chemistry.dto.ChemistryResult;
import com.teamup.teamUp.chemistry.service.ChemistryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/chemistry")
public class ChemistryController {

    private final ChemistryService chemistryService;

    public ChemistryController(ChemistryService chemistryService) {
        this.chemistryService = chemistryService;
    }

    @GetMapping("/{userA}/{userB}")
    public ChemistryResult getChemistry(@PathVariable UUID userA, @PathVariable UUID userB) {
        return chemistryService.compute(userA,userB);
    }
}
