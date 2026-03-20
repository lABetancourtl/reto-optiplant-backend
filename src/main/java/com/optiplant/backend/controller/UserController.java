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

/**
 * Controlador para la gestión de usuarios con rol SUCURSAL.
 * Solo accesible por usuarios con rol ADMIN.
 * Permite consultar, crear, actualizar y eliminar usuarios de sucursal.
 * Asocia usuarios a sucursales y permite actualizar sus datos y contraseña.
 */
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

    /**
     * Obtiene todos los usuarios con rol SUCURSAL.
     * Solo ADMIN.
     * @return Lista de usuarios de sucursal.
     */
    @GetMapping
    public ResponseEntity<List<SucursalUserResponse>> getAllSucursalUsers() {
        List<User> users = userRepository.findAll().stream()
                .filter(user -> "SUCURSAL".equals(user.getRole()))
                .collect(Collectors.toList());
        List<SucursalUserResponse> responses = users.stream()
                .map(user -> new SucursalUserResponse(user.getId(), user.getUsername(), user.getName(),
                        user.getRole(),
                        user.getBranch() != null ? user.getBranch().getId() : null,
                        user.getBranch() != null ? user.getBranch().getName() : null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene un usuario de sucursal por su ID.
     * Solo ADMIN.
     * @param id ID del usuario.
     * @return Usuario de sucursal encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SucursalUserResponse> getSucursalUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .filter(u -> "SUCURSAL".equals(u.getRole()))
                .orElseThrow(() -> new RuntimeException("Sucursal user not found"));
        SucursalUserResponse response = new SucursalUserResponse(user.getId(), user.getUsername(), user.getName(),
                user.getRole(),
                user.getBranch() != null ? user.getBranch().getId() : null,
                user.getBranch() != null ? user.getBranch().getName() : null);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo usuario de sucursal.
     * Solo ADMIN.
     * @param request Datos para crear el usuario.
     * @return Usuario creado.
     */
    @PostMapping
    public ResponseEntity<User> createSucursalUser(@RequestBody CreateSucursalUserRequest request) {
        // Reuse AuthService register logic, but force role to SUCURSAL
        User user = new User();
        user.setUsername(request.userName());
        user.setName(request.name());
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

    /**
     * Actualiza los datos de un usuario de sucursal.
     * Solo ADMIN.
     * @param id ID del usuario.
     * @param request Datos actualizados.
     * @return Usuario actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateSucursalUser(@PathVariable Long id, @RequestBody UpdateSucursalUserRequest request) {
        User user = userRepository.findById(id)
                .filter(u -> "SUCURSAL".equals(u.getRole()))
                .orElseThrow(() -> new RuntimeException("Sucursal user not found"));
        user.setUsername(request.userName());
        user.setName(request.name());
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

    /**
     * Elimina un usuario de sucursal por su ID.
     * Solo ADMIN.
     * @param id ID del usuario.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSucursalUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .filter(u -> "SUCURSAL".equals(u.getRole()))
                .orElseThrow(() -> new RuntimeException("Sucursal user not found"));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}
