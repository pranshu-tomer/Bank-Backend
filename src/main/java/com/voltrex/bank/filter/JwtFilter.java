package com.voltrex.bank.filter;

import com.voltrex.bank.entities.User;
import com.voltrex.bank.services.JwtService;
import com.voltrex.bank.services.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;
    private final UserService userService;

    public JwtFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1) If there's no header or it doesn't start with "Bearer ", just continue the chain and return.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // IMPORTANT: stop here so we don't continue processing a null token
        }

        // 2) Extract token safely
        final String token = authHeader.substring(7).trim(); // remove "Bearer "

        Long userId;
        try {
            userId = jwtService.getUserIdFromToken(token); // this may throw JwtException / ExpiredJwtException
        } catch (ExpiredJwtException ex) {
            log.info("JWT expired for request {}: {}", request.getRequestURI(), ex.getMessage());
            // token expired -> do not authenticate the request, let controller return 401/403 as appropriate
            filterChain.doFilter(request, response);
            return;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT for request {}: {}", request.getRequestURI(), ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 3) If we have a userId and no SecurityContext yet, load user and set Authentication
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // load domain user (optional: use UserDetailsService to get authorities)
            Optional<User> maybeUser = Optional.ofNullable(userService.getUserById(userId));
            if (maybeUser.isPresent()) {
                User user = maybeUser.get();

                // IMPORTANT: give authorities — otherwise hasRole checks will fail with 403.
                // Replace with real role extraction if you store roles on the User entity.
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("JWT authenticated userId={} path={}", userId, request.getRequestURI());
            } else {
                log.warn("User id from JWT not found in DB: {}", userId);
            }
        }

        // 4) Continue filter chain exactly once
        filterChain.doFilter(request, response);
    }
}
