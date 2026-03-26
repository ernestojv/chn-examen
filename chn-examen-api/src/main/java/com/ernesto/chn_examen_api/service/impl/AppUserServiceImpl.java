package com.ernesto.chn_examen_api.service.impl;

import com.ernesto.chn_examen_api.dto.AppUserDTO;
import com.ernesto.chn_examen_api.dto.AppUserPatchDTO;
import com.ernesto.chn_examen_api.entity.AppUser;
import com.ernesto.chn_examen_api.entity.Employee;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.mapper.AppUserMapper;
import com.ernesto.chn_examen_api.repository.AppUserRepository;
import com.ernesto.chn_examen_api.repository.EmployeeRepository;
import com.ernesto.chn_examen_api.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final EmployeeRepository employeeRepository;
    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AppUserDTO createUser(AppUserDTO dto) {
        if (appUserRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (appUserRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new IllegalArgumentException("Employee already has an assigned user");
        }

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));

        AppUser user = AppUser.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .employee(employee)
                .build();

        AppUser savedUser = appUserRepository.save(user);
        return appUserMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AppUserDTO getUserById(Integer id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return appUserMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppUserDTO> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(appUserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppUserDTO updateUser(Integer id, AppUserDTO dto) {
        AppUser existingUser = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check username uniqueness if changed
        if (!existingUser.getUsername().equals(dto.getUsername())) {
            if (appUserRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }
            existingUser.setUsername(dto.getUsername());
        }
        
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
             existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Check employee reassignment
        if (!existingUser.getEmployee().getId().equals(dto.getEmployeeId())) {
             if (appUserRepository.existsByEmployeeId(dto.getEmployeeId())) {
                 throw new IllegalArgumentException("Employee already has an assigned user");
             }
             Employee employee = employeeRepository.findById(dto.getEmployeeId())
                 .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));
             existingUser.setEmployee(employee);
        }

        return appUserMapper.toDto(appUserRepository.save(existingUser));
    }

    @Override
    @Transactional
    public AppUserDTO patchUser(Integer id, AppUserPatchDTO dto) {
        AppUser existingUser = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (dto.getUsername() != null) {
            if (dto.getUsername().isBlank()) {
                throw new IllegalArgumentException("Username must not be blank");
            }

            if (!existingUser.getUsername().equals(dto.getUsername())) {
                if (appUserRepository.findByUsername(dto.getUsername()).isPresent()) {
                    throw new IllegalArgumentException("Username already exists");
                }
                existingUser.setUsername(dto.getUsername());
            }
        }

        if (dto.getPassword() != null) {
            if (dto.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password must not be blank when provided");
            }
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getEmployeeId() != null && !existingUser.getEmployee().getId().equals(dto.getEmployeeId())) {
            if (appUserRepository.existsByEmployeeId(dto.getEmployeeId())) {
                throw new IllegalArgumentException("Employee already has an assigned user");
            }
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));
            existingUser.setEmployee(employee);
        }

        return appUserMapper.toDto(appUserRepository.save(existingUser));
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        if (!appUserRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        appUserRepository.deleteById(id);
    }
}
