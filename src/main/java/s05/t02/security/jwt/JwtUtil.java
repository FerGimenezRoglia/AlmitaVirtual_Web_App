package s05.t02.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import s05.t02.exception.custom.InvalidJwtTokenException;
import s05.t02.repository.UserRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    /**
     * Constructor que inicializa la clave secreta a partir del valor del archivo application.properties
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = expiration;
    }

    /**
     * Genera un token JWT con el nombre de usuario y el rol como claims.
     */
    public String generateToken(String username, String role) {
        log.info("JWT token generated");

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date()) // Fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration)) // Fecha de expiración
                .signWith(secretKey) // Firma con la clave secreta
                .compact();
    }

    /**
     * Extrae el nombre de usuario (subject) desde el token.
     */
    public String extractUsername(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("Invalid or expired JWT token.");
        }
    }

    /**
     * Extrae todos los claims del token.
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Valida que el token sea correcto y que no esté vencido.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            boolean valid = !claims.getExpiration().before(new Date());
            log.debug("Token validation result: {}", valid);
            return valid;
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("Invalid or expired JWT token.");
        }
    }

    /**
     * Extrae el rol desde el token y lo transforma en un objeto GrantedAuthority para Spring Security.
     */
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = extractClaims(token);
        String role = claims.get("role", String.class);
        if (role == null) {
            throw new InvalidJwtTokenException("Missing role claim in token.");
        }
        return List.of(new SimpleGrantedAuthority(role));
    }
}