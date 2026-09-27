package com.pallet.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String tenDangNhap, Long nhanVienId, String vaiTro) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenExpirationMinutes * 60 * 1000);
        return Jwts.builder()
                .subject(tenDangNhap)
                .claim("nhanVienId", nhanVienId)
                .claim("vaiTro", vaiTro)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    public String extractTenDangNhap(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractNhanVienId(String token) {
        return extractAllClaims(token).get("nhanVienId", Long.class);
    }

    public String extractVaiTro(String token) {
        return extractAllClaims(token).get("vaiTro", String.class);
    }

    public boolean isTokenValid(String token, String tenDangNhap) {
        try {
            String subject = extractTenDangNhap(token);
            return subject.equals(tenDangNhap) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
    }
}
