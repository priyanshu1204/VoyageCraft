package com.voyagecraft.service;

import com.voyagecraft.dto.auth.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.exception.BadRequestException;
import com.voyagecraft.repository.UserRepository;
import com.voyagecraft.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks private AuthService authService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ModelMapper modelMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com")
                .firstName("John").lastName("Doe").passwordHash("hashed").build();
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com"); req.setPassword("Pass123!"); req.setFirstName("John"); req.setLastName("Doe");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass123!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateTokenFromEmail("test@example.com")).thenReturn("jwt-token");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("jwt-token", resp.getToken());
        assertEquals("Bearer", resp.getTokenType());
        assertEquals("test@example.com", resp.getEmail());
        assertEquals("John", resp.getFirstName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com"); req.setPassword("Pass123!");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com"); req.setPassword("Pass123!");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt-token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("jwt-token", resp.getToken());
        assertEquals(1L, resp.getUserId());
    }

    @Test
    void login_userNotFound_throwsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("unknown@test.com"); req.setPassword("Pass123!");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("token");
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(req));
    }

    @Test
    void getCurrentUser_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        UserResponse mockResp = UserResponse.builder().id(1L).email("test@example.com").build();
        when(modelMapper.map(testUser, UserResponse.class)).thenReturn(mockResp);

        UserResponse resp = authService.getCurrentUser("test@example.com");

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
    }

    @Test
    void getCurrentUser_notFound_throwsException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> authService.getCurrentUser("unknown@test.com"));
    }

    @Test
    void getUserByEmail_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        User result = authService.getUserByEmail("test@example.com");
        assertEquals(testUser, result);
    }

    @Test
    void getUserByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> authService.getUserByEmail("x@y.com"));
    }
}
