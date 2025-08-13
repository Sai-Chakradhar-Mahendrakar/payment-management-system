package miniproject2.paymentmanagementsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    private String testToken;

    @BeforeEach
    void setUp() {
        testToken = "test.jwt.token";
    }

    @Test
    void blacklistToken_ShouldAddTokenToBlacklist() {
        // When
        tokenBlacklistService.blacklistToken(testToken);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(testToken));
    }

    @Test
    void isTokenBlacklisted_ShouldReturnFalseForNonBlacklistedToken() {
        // When & Then
        assertFalse(tokenBlacklistService.isTokenBlacklisted(testToken));
    }

    @Test
    void isTokenBlacklisted_ShouldReturnTrueForBlacklistedToken() {
        // Given
        tokenBlacklistService.blacklistToken(testToken);

        // When & Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(testToken));
    }

    @Test
    void blacklistToken_ShouldHandleMultipleTokens() {
        // Given
        String token1 = "token1";
        String token2 = "token2";

        // When
        tokenBlacklistService.blacklistToken(token1);
        tokenBlacklistService.blacklistToken(token2);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token1));
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token2));
    }

    @Test
    void blacklistToken_ShouldHandleDuplicateTokens() {
        // When
        tokenBlacklistService.blacklistToken(testToken);
        tokenBlacklistService.blacklistToken(testToken);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(testToken));
    }

    @Test
    void removeExpiredTokens_ShouldExecuteWithoutError() {
        // When & Then
        assertDoesNotThrow(() -> tokenBlacklistService.removeExpiredTokens());
    }
}
