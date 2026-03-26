package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.AuthResponseDTO;
import com.ernesto.chn_examen_api.dto.LoginRequestDTO;
import com.ernesto.chn_examen_api.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_shouldAuthenticateAndReturnToken() {
        LoginRequestDTO request = new LoginRequestDTO("admin", "123");

        when(jwtService.generateToken("admin")).thenReturn("jwt-token");

        ResponseEntity<AuthResponseDTO> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken("admin");
    }

    @Test
    void login_shouldPropagateAuthenticationError() {
        LoginRequestDTO request = new LoginRequestDTO("admin", "bad-pass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authController.login(request)
        );

        assertEquals("Bad credentials", ex.getMessage());
    }
}
