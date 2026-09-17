package dev.relaydesk.security;

import dev.relaydesk.security.dto.LoginRequest;
import dev.relaydesk.security.dto.UserDto;
import dev.relaydesk.user.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        String jwt = jwtService.generateToken(userDetails);
        addCookie(response, "rd_at", jwt, 15 * 60, "/");

        String refreshToken = generateRandomToken();
        UUID familyId = UUID.randomUUID();
        
        saveRefreshToken(userDetails.getUser(), refreshToken, familyId);
        addCookie(response, "rd_rt", refreshToken, 14 * 24 * 60 * 60, "/api/v1/auth");

        return ResponseEntity.ok(toDto(userDetails.getUser()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rt = getCookieValue(request, "rd_rt");
        if (rt == null) {
            return ResponseEntity.status(401).build();
        }

        String hash = hashToken(rt);
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(hash);
        
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        RefreshToken token = tokenOpt.get();
        if (token.isUsed()) {
            // Revoke family
            refreshTokenRepository.findAll().stream()
                .filter(t -> t.getFamilyId().equals(token.getFamilyId()))
                .forEach(t -> {
                    t.setUsed(true);
                    refreshTokenRepository.save(t);
                });
            return ResponseEntity.status(401).build();
        }

        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            return ResponseEntity.status(401).build();
        }

        // Rotate
        token.setUsed(true);
        refreshTokenRepository.save(token);

        CustomUserDetails userDetails = new CustomUserDetails(token.getUser());
        String newJwt = jwtService.generateToken(userDetails);
        addCookie(response, "rd_at", newJwt, 15 * 60, "/");

        String newRefreshToken = generateRandomToken();
        saveRefreshToken(token.getUser(), newRefreshToken, token.getFamilyId());
        addCookie(response, "rd_rt", newRefreshToken, 14 * 24 * 60 * 60, "/api/v1/auth");

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String rt = getCookieValue(request, "rd_rt");
        if (rt != null) {
            String hash = hashToken(rt);
            refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
                token.setUsed(true);
                refreshTokenRepository.save(token);
            });
        }
        
        Cookie atCookie = new Cookie("rd_at", "");
        atCookie.setMaxAge(0);
        atCookie.setPath("/");
        response.addCookie(atCookie);

        Cookie rtCookie = new Cookie("rd_rt", "");
        rtCookie.setMaxAge(0);
        rtCookie.setPath("/api/v1/auth");
        response.addCookie(rtCookie);

        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(401).build();
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(toDto(userDetails.getUser()));
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Should be true in prod, but false for localhost docker
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found");
        }
    }

    private void saveRefreshToken(User user, String token, UUID familyId) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hashToken(token));
        rt.setFamilyId(familyId);
        rt.setExpiresAt(OffsetDateTime.now().plusDays(14));
        refreshTokenRepository.save(rt);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setTeamId(user.getTeam() != null ? user.getTeam().getId() : null);
        return dto;
    }
}
