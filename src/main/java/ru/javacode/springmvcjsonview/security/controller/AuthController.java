package ru.javacode.springmvcjsonview.security.controller;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javacode.springmvcjsonview.model.User;
import ru.javacode.springmvcjsonview.security.JWTUtils;
import ru.javacode.springmvcjsonview.security.dto.AuthRequest;
import ru.javacode.springmvcjsonview.security.service.OurUserDetailedService;
import ru.javacode.springmvcjsonview.service.UserService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final UserService userService;
    private final OurUserDetailedService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        try {
            User user = userDetailsService.loadUserByUsername(authRequest.getUsername());

            userDetailsService.unlockWhenTimeExpired(user);

            if (!user.isAccountNonLocked()) {
                return ResponseEntity.status(HttpStatus.LOCKED).body("Превышен лимит попыток входа. Попробуйте позже");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            user.setFailedAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            userService.updateUser(user.getUserId(), user);



            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);
            String refreshToken = jwtUtils.generateRefreshToken(userDetails);
            Date expirationDate = jwtUtils.extractExpiration(jwt);



            Map<String, Object> response = new HashMap<>();
            response.put("jwt", jwt);
            response.put("refreshToken", refreshToken);
            response.put("expiration", expirationDate);
            response.put("role", userDetails.getAuthorities());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            User user = userDetailsService.loadUserByUsername(authRequest.getUsername());
            if (user != null) {
                int attempts = (user.getFailedAttempts() != null) ? user.getFailedAttempts() + 1 : 1;
                user.setFailedAttempts(attempts);
                if (attempts >= 5) {
                    user.setAccountNonLocked(false);
                    user.setLockTime(System.currentTimeMillis());
                }
                userService.updateUser(user.getUserId(), user);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверные пользователь или пароль");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Отсутствует refresh токен");
        }

        try {
            String username = jwtUtils.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtils.isTokenValid(refreshToken, userDetails)) {
                String newJwt = jwtUtils.generateToken(userDetails);
                Map<String, String> tokens = new HashMap<>();
                tokens.put("jwt", newJwt);
                tokens.put("refreshToken", refreshToken);
                return ResponseEntity.ok(tokens);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невалидный refresh токен");
            }
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh токен истек");
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невалидный refresh токен");
        }
    }
}


