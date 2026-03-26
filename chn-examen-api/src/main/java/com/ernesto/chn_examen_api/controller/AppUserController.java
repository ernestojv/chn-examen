package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.AppUserDTO;
import com.ernesto.chn_examen_api.dto.AppUserPatchDTO;
import com.ernesto.chn_examen_api.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping
    public ResponseEntity<AppUserDTO> createUser(@Valid @RequestBody AppUserDTO dto) {
        return new ResponseEntity<>(appUserService.createUser(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AppUserDTO>> getAllUsers() {
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(appUserService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUserDTO> updateUser(@PathVariable Integer id, @Valid @RequestBody AppUserDTO dto) {
        return ResponseEntity.ok(appUserService.updateUser(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AppUserDTO> patchUser(@PathVariable Integer id, @Valid @RequestBody AppUserPatchDTO dto) {
        return ResponseEntity.ok(appUserService.patchUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        appUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
