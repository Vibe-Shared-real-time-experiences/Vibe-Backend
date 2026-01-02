package vn.vibeteam.vibe.service.auth;

public interface TokenBlacklistService {
    boolean isTokenBlacklisted(String token);
    boolean blacklistToken(String token, long expirationMillis);
}
