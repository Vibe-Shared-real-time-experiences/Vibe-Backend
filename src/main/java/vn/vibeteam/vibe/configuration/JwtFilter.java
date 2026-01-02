package vn.vibeteam.vibe.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.vibeteam.vibe.util.JwtUtil;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final String ROLE_PREFIX = "ROLE_";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {
            String token = extractTokenFromRequestHeader(request);
            if (token != null && jwtUtil.isTokenValid(token)) {

                // 1. Extract info
                String userID = jwtUtil.extractUserID(token);
                List<String> roles = jwtUtil.extractRoles(token);

                // 2. Create simple User object in memory
                List<SimpleGrantedAuthority> authorities = roles.stream()
                                                                .map(role -> new SimpleGrantedAuthority(role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role))
                                                                .toList();

                UserDetails userDetails = new User(userID, "", authorities);

                // 3. Put it in the Context
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            log.error("JWT processing failed: {}", e.getMessage());

            // Handle other signature errors
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }

    private String extractTokenFromRequestHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}