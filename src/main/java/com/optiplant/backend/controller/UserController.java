package com.optiplant.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.optiplant.backend.dto.CreateSucursalUserRequest;
import com.optiplant.backend.dto.UpdateSucursalUserRequest;
import com.optiplant.backend.dto.SucursalUserResponse;
import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.entity.User;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.repository.UserRepository;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<SucursalUserResponse>> getAllSucursalUsers() {
        List<User> users = userRepository.findAll().stream()
                .filter(user -> "SUCURSAL".equals(user.getRole()))
                .collect(Collectors.toList());
        List<SucursalUserResponse> responses = users.stream()
                .map(user -> new SucursalUserResponse(user.getId(), user.getUsername(), user.getRole(),
                        user.getBranch() != null ? user.getBranch().getId() : null,
                        user.getBranch() != null ? user.getBranch().getName() : null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalUserResponse> getSucursalUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .filter(u -> "SUCURSAL".equals(u.getRole()))
                .orElseThrow(() -> new RuntimeException("Sucursal user not found"));
        SucursalUserResponse response = new SucursalUserResponse(user.getId(), user.getUsername(), user.getRole(),
                user.getBranch() != null ? user.getBranch().getId() : null,
                user.getBranch() != null ? user.getBranch().getName() : null);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<User> createSucursalUser(@RequestBody CreateSucursalUserRequest request) {
        // Reuse AuthService register logic, but force role to SUCURSAL
        User user = new User();
        user.setUsername(request.userName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("SUCURSAL");
        if (request.branchId() != null) {
            Branch branch = branchRepository.findById(request.branchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            user.setBranch(branch);
        }
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateSucursalUser(@PathVariable Long id, @RequestBody UpdateSucursalUserRequest request) {
        User user = userRepository.findById(id)
                .filter(u -> "SUCURSAL".equals(u.getRole()))
                .orElseThrow(() -> new RuntimeException("Sucursal user not found"));
        user.setUsername(request.userName());
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.branchId() != null) {
            Branch branch = branchRepository.findById(request.branchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            user.setBranch(branch);
        }
        User updated = userRepository.save(user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSucursalUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .filter(u -> "SUCURSAL".equals(u.getRole()))
                .orElseThrow(() -> new RuntimeException("Sucursal user not found"));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}
