package miniproject2.paymentmanagementsystem.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    public void removeExpiredTokens() {
        // In a production environment, you would implement token expiration cleanup
        // This could be done with a scheduled task that removes expired tokens
        // For now, this is a placeholder method
    }
}
