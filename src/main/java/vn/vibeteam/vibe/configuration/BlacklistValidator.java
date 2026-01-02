package vn.vibeteam.vibe.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import vn.vibeteam.vibe.service.auth.TokenBlacklistService;

/**
 * Custom JWT Validator to check if the token is blacklisted.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BlacklistValidator implements OAuth2TokenValidator<Jwt> {

    private final TokenBlacklistService blacklistService;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // 1. Blacklist Check
        if (blacklistService.isTokenBlacklisted(jwt.getTokenValue())) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                "invalid_token", "The token has been blacklisted", null
            ));
        }

        // 2. Expiration & Signature are ALREADY handled by Spring before this runs.
        return OAuth2TokenValidatorResult.success();
    }
}