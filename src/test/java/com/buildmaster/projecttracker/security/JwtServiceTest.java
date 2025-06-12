package com.buildmaster.projecttracker.security;

import io.jsonwebtoken.Claims;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;



    private final String TEST_SECRET_KEY = "averylongandsecuresecretkeythatissufficientlylongforencryptionanddecryptionpurposes";
    private final long TEST_EXPIRATION = 1000 * 60 * 15;
    private final long TEST_REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to inject @Value fields into the JwtService instance
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", TEST_REFRESH_EXPIRATION);

        // Create a mock UserDetails object for testing
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVELOPER"));
        userDetails = new User("test@example.com", "password", authorities);
    }

    @Test
    @DisplayName("Should generate a valid access token")
    void generateToken_shouldGenerateValidAccessToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.extractUsername(token)).isEqualTo(userDetails.getUsername());
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();

        Claims claims = jwtService.extractAllClaims(token);
        assertThat(claims.getSubject()).isEqualTo(userDetails.getUsername());
        assertThat(claims.get("role")).isEqualTo("ROLE_DEVELOPER");
        assertThat(claims.getExpiration()).isAfter(new Date(System.currentTimeMillis()));
    }

    @Test
    @DisplayName("Should generate a valid refresh token")
    void generateRefreshToken_shouldGenerateValidRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        assertThat(refreshToken).isNotNull().isNotEmpty();
        assertThat(jwtService.extractUsername(refreshToken)).isEqualTo(userDetails.getUsername());
        assertThat(jwtService.isTokenValid(refreshToken, userDetails)).isTrue();


        Claims claims = jwtService.extractAllClaims(refreshToken);
        assertThat(claims.getExpiration().getTime()).isCloseTo(System.currentTimeMillis() + TEST_REFRESH_EXPIRATION, Percentage.withPercentage(2000));
    }

    @Test
    @DisplayName("Should extract username correctly")
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("Should extract custom claims correctly")
    void extractClaim_shouldExtractCustomClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("custom_field", "custom_value");
        String token = jwtService.generateToken(extraClaims, userDetails);

        String customFieldValue = jwtService.extractClaim(token, claims -> claims.get("custom_field", String.class));
        assertThat(customFieldValue).isEqualTo("custom_value");
    }
}
