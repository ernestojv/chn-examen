package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.AppUserDTO;
import com.ernesto.chn_examen_api.dto.AppUserPatchDTO;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserControllerTest {

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private AppUserController appUserController;

    @Test
    void createUser_shouldReturnCreated() {
        AppUserDTO request = sampleAppUserDto(null, "admin", "123", 1);
        AppUserDTO created = sampleAppUserDto(1, "admin", null, 1);

        when(appUserService.createUser(request)).thenReturn(created);

        ResponseEntity<AppUserDTO> response = appUserController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
        assertEquals("admin", response.getBody().getUsername());
    }

    @Test
    void getAllUsers_shouldReturnOkWithList() {
        when(appUserService.getAllUsers()).thenReturn(List.of(
                sampleAppUserDto(1, "admin", null, 1),
                sampleAppUserDto(2, "user", null, 2)
        ));

        ResponseEntity<List<AppUserDTO>> response = appUserController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getUserById_shouldReturnOkWhenFound() {
        when(appUserService.getUserById(1)).thenReturn(sampleAppUserDto(1, "admin", null, 1));

        ResponseEntity<AppUserDTO> response = appUserController.getUserById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("admin", response.getBody().getUsername());
    }

    @Test
    void getUserById_shouldPropagateNotFound() {
        when(appUserService.getUserById(9))
                .thenThrow(new ResourceNotFoundException("User not found with id: 9"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserController.getUserById(9)
        );

        assertEquals("User not found with id: 9", ex.getMessage());
    }

    @Test
    void updateUser_shouldReturnOk() {
        AppUserDTO request = sampleAppUserDto(null, "admin", "123", 1);
        AppUserDTO updated = sampleAppUserDto(1, "admin", null, 1);

        when(appUserService.updateUser(1, request)).thenReturn(updated);

        ResponseEntity<AppUserDTO> response = appUserController.updateUser(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void patchUser_shouldReturnOk() {
        AppUserPatchDTO patch = AppUserPatchDTO.builder().username("new-admin").build();
        AppUserDTO updated = sampleAppUserDto(1, "new-admin", null, 1);

        when(appUserService.patchUser(1, patch)).thenReturn(updated);

        ResponseEntity<AppUserDTO> response = appUserController.patchUser(1, patch);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-admin", response.getBody().getUsername());
    }

    @Test
    void patchUser_shouldPropagateBusinessError() {
        AppUserPatchDTO patch = AppUserPatchDTO.builder().password(" ").build();

        when(appUserService.patchUser(1, patch))
                .thenThrow(new IllegalArgumentException("Password must not be blank when provided"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> appUserController.patchUser(1, patch)
        );

        assertEquals("Password must not be blank when provided", ex.getMessage());
    }

    @Test
    void deleteUser_shouldReturnNoContent() {
        ResponseEntity<Void> response = appUserController.deleteUser(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteUser_shouldCallService() {
        appUserController.deleteUser(2);

        verify(appUserService).deleteUser(2);
    }

    private static AppUserDTO sampleAppUserDto(Integer id, String username, String password, Integer employeeId) {
        return AppUserDTO.builder()
                .id(id)
                .username(username)
                .password(password)
                .employeeId(employeeId)
                .build();
    }
}
