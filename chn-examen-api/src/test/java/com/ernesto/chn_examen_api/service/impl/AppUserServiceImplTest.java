package com.ernesto.chn_examen_api.service.impl;

import com.ernesto.chn_examen_api.dto.AppUserDTO;
import com.ernesto.chn_examen_api.dto.AppUserPatchDTO;
import com.ernesto.chn_examen_api.entity.AppUser;
import com.ernesto.chn_examen_api.entity.Employee;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.mapper.AppUserMapper;
import com.ernesto.chn_examen_api.repository.AppUserRepository;
import com.ernesto.chn_examen_api.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AppUserMapper appUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    @Test
    void createUser_shouldThrowWhenUsernameExists() {
        AppUserDTO request = sampleAppUserDto(null, "admin", "123", 1);

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(new AppUser()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.createUser(request)
        );

        assertEquals("Username already exists", ex.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUser_shouldThrowWhenEmployeeAlreadyAssigned() {
        AppUserDTO request = sampleAppUserDto(null, "admin", "123", 1);

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(appUserRepository.existsByEmployeeId(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.createUser(request)
        );

        assertEquals("Employee already has an assigned user", ex.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUser_shouldThrowWhenEmployeeNotFound() {
        AppUserDTO request = sampleAppUserDto(null, "admin", "123", 1);

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(appUserRepository.existsByEmployeeId(1)).thenReturn(false);
        when(employeeRepository.findById(1)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserService.createUser(request)
        );

        assertEquals("Employee not found with id: 1", ex.getMessage());
    }

    @Test
    void createUser_shouldEncodePasswordPersistAndReturnDto() {
        AppUserDTO request = sampleAppUserDto(null, "admin", "123", 1);
        Employee employee = sampleEmployee(1);
        AppUser saved = sampleUserEntity(1, "admin", "encoded-123", employee);
        AppUserDTO expected = sampleAppUserDto(1, "admin", null, 1);

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(appUserRepository.existsByEmployeeId(1)).thenReturn(false);
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(passwordEncoder.encode("123")).thenReturn("encoded-123");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(saved);
        when(appUserMapper.toDto(saved)).thenReturn(expected);

        AppUserDTO result = appUserService.createUser(request);

        assertEquals(1, result.getId());
        assertEquals("admin", result.getUsername());
        verify(passwordEncoder).encode("123");
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(appUserRepository.findById(8)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserService.getUserById(8)
        );

        assertEquals("User not found with id: 8", ex.getMessage());
    }

    @Test
    void getAllUsers_shouldReturnMappedList() {
        AppUser entity = sampleUserEntity(1, "admin", "enc", sampleEmployee(1));
        AppUserDTO dto = sampleAppUserDto(1, "admin", null, 1);

        when(appUserRepository.findAll()).thenReturn(List.of(entity));
        when(appUserMapper.toDto(entity)).thenReturn(dto);

        List<AppUserDTO> result = appUserService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("admin", result.getFirst().getUsername());
    }

    @Test
    void updateUser_shouldThrowWhenUsernameChangedAndAlreadyExists() {
        AppUser existing = sampleUserEntity(1, "admin", "enc", sampleEmployee(1));
        AppUserDTO request = sampleAppUserDto(1, "newadmin", "456", 1);

        when(appUserRepository.findById(1)).thenReturn(Optional.of(existing));
        when(appUserRepository.findByUsername("newadmin")).thenReturn(Optional.of(new AppUser()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.updateUser(1, request)
        );

        assertEquals("Username already exists", ex.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void updateUser_shouldThrowWhenEmployeeReassignedAndAlreadyAssigned() {
        AppUser existing = sampleUserEntity(1, "admin", "enc", sampleEmployee(1));
        AppUserDTO request = sampleAppUserDto(1, "admin", "456", 2);

        when(appUserRepository.findById(1)).thenReturn(Optional.of(existing));
        when(appUserRepository.existsByEmployeeId(2)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.updateUser(1, request)
        );

        assertEquals("Employee already has an assigned user", ex.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void updateUser_shouldUpdateAndEncodePassword() {
        AppUser existing = sampleUserEntity(1, "admin", "enc", sampleEmployee(1));
        AppUserDTO request = sampleAppUserDto(1, "newadmin", "456", 2);
        Employee newEmployee = sampleEmployee(2);
        AppUserDTO expected = sampleAppUserDto(1, "newadmin", null, 2);

        when(appUserRepository.findById(1)).thenReturn(Optional.of(existing));
        when(appUserRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(appUserRepository.existsByEmployeeId(2)).thenReturn(false);
        when(employeeRepository.findById(2)).thenReturn(Optional.of(newEmployee));
        when(passwordEncoder.encode("456")).thenReturn("enc-456");
        when(appUserRepository.save(existing)).thenReturn(existing);
        when(appUserMapper.toDto(existing)).thenReturn(expected);

        AppUserDTO result = appUserService.updateUser(1, request);

        assertEquals("newadmin", result.getUsername());
        verify(passwordEncoder).encode("456");
        verify(appUserRepository).save(existing);
    }

    @Test
    void patchUser_shouldThrowWhenUsernameBlank() {
        AppUser existing = sampleUserEntity(1, "admin", "enc", sampleEmployee(1));
        AppUserPatchDTO patch = AppUserPatchDTO.builder().username("   ").build();

        when(appUserRepository.findById(1)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.patchUser(1, patch)
        );

        assertEquals("Username must not be blank", ex.getMessage());
    }

    @Test
    void patchUser_shouldThrowWhenPasswordBlank() {
        AppUser existing = sampleUserEntity(1, "admin", "enc", sampleEmployee(1));
        AppUserPatchDTO patch = AppUserPatchDTO.builder().password(" ").build();

        when(appUserRepository.findById(1)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.patchUser(1, patch)
        );

        assertEquals("Password must not be blank when provided", ex.getMessage());
    }

    @Test
    void patchUser_shouldApplyPartialChanges() {
        AppUser existing = sampleUserEntity(1, "admin", "enc", sampleEmployee(1));
        Employee newEmployee = sampleEmployee(2);
        AppUserPatchDTO patch = AppUserPatchDTO.builder()
                .username("newadmin")
                .password("789")
                .employeeId(2)
                .build();
        AppUserDTO expected = sampleAppUserDto(1, "newadmin", null, 2);

        when(appUserRepository.findById(1)).thenReturn(Optional.of(existing));
        when(appUserRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(appUserRepository.existsByEmployeeId(2)).thenReturn(false);
        when(employeeRepository.findById(2)).thenReturn(Optional.of(newEmployee));
        when(passwordEncoder.encode("789")).thenReturn("enc-789");
        when(appUserRepository.save(existing)).thenReturn(existing);
        when(appUserMapper.toDto(existing)).thenReturn(expected);

        AppUserDTO result = appUserService.patchUser(1, patch);

        assertEquals("newadmin", result.getUsername());
        verify(passwordEncoder).encode("789");
        verify(appUserRepository).save(existing);
    }

    @Test
    void deleteUser_shouldThrowWhenNotFound() {
        when(appUserRepository.existsById(5)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserService.deleteUser(5)
        );

        assertEquals("User not found with id: 5", ex.getMessage());
        verify(appUserRepository, never()).deleteById(any(Integer.class));
    }

    @Test
    void deleteUser_shouldDeleteWhenFound() {
        when(appUserRepository.existsById(5)).thenReturn(true);

        appUserService.deleteUser(5);

        verify(appUserRepository).deleteById(5);
    }

    private static AppUserDTO sampleAppUserDto(Integer id, String username, String password, Integer employeeId) {
        return AppUserDTO.builder()
                .id(id)
                .username(username)
                .password(password)
                .employeeId(employeeId)
                .build();
    }

    private static Employee sampleEmployee(Integer id) {
        return Employee.builder()
                .id(id)
                .firstName("Ana")
                .lastName("Lopez")
                .employeeCode("EMP-00" + id)
                .position("Analista")
                .build();
    }

    private static AppUser sampleUserEntity(Integer id, String username, String password, Employee employee) {
        return AppUser.builder()
                .id(id)
                .username(username)
                .password(password)
                .employee(employee)
                .build();
    }
}
