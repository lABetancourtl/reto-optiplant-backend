package com.optiplant.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.dto.CreateBranchRequest;

@RestController
@RequestMapping("/branches")
public class BranchController {

    private final BranchRepository branchRepository;

    public BranchController(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Branch> createBranch(@RequestBody CreateBranchRequest request) {
        Branch branch = new Branch();
        branch.setName(request.name());
        Branch saved = branchRepository.save(branch);
        return ResponseEntity.ok(saved);
    }
}
