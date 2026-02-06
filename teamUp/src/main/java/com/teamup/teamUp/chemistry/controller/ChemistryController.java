package com.teamup.teamUp.chemistry.controller;


import com.teamup.teamUp.chemistry.dto.ChemistryResult;
import com.teamup.teamUp.chemistry.service.ChemistryService;
import com.teamup.teamUp.controller.ResponseApi;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/chemistry")
public class ChemistryController {

    private final ChemistryService chemistryService;
    private final UserRepository userRepository;

    public ChemistryController(ChemistryService chemistryService, UserRepository userRepository) {
        this.chemistryService = chemistryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{otherUserId}")
    public ResponseEntity<ResponseApi<ChemistryResult>> getChemistry(@PathVariable UUID otherUserId, Authentication auth) {
        String username = auth.getName();
        User me = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok(new ResponseApi<>("Chemistry with this player calculated", chemistryService.compute(me.getId(), otherUserId), true));
    }


}
