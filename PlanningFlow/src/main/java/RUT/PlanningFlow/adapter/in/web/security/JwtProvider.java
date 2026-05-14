package RUT.PlanningFlow.adapter.in.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {

    static final String CLAIM_TYP = "typ";
    static final String TYP_ACCESS = "access";
    static final String TYP_REFRESH = "refresh";
    static final String CLAIM_USERNAME = "username";
    static final String CLAIM_ROLES = "roles";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtProvider(final JwtProperties properties) {
        this.properties = properties;
        final byte[] bytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes for HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(final int userId, final String username, final List<String> roles) {
        final Instant now = Instant.now();
        final Instant exp = now.plus(properties.accessTokenTtl());
        return Jwts.builder()
                .subject(Integer.toString(userId))
                .issuer("PlanningFlow")
                .claim(CLAIM_TYP, TYP_ACCESS)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLES, roles == null ? List.of() : roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(final int userId, final String jti, final Instant expiresAt) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(Integer.toString(userId))
                .issuer("PlanningFlow")
                .claim(CLAIM_TYP, TYP_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseAndValidateAccess(final String token) throws JwtException {
        final Claims claims = parseSigned(token);
        requireTyp(claims, TYP_ACCESS);
        return claims;
    }

    public Claims parseAndValidateRefresh(final String token) throws JwtException {
        final Claims claims = parseSigned(token);
        requireTyp(claims, TYP_REFRESH);
        return claims;
    }

    private Claims parseSigned(final String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static void requireTyp(final Claims claims, final String expected) {
        final String actual = claims.get(CLAIM_TYP, String.class);
        if (!expected.equals(actual)) {
            throw new JwtException("Invalid token type");
        }
    }

    public static List<String> rolesFrom(final Claims claims) {
        final Object raw = claims.get(CLAIM_ROLES);
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public static String usernameFrom(final Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    public static int userIdFromSubject(final Claims claims) {
        return Integer.parseInt(claims.getSubject());
    }

    public static String jtiFrom(final Claims claims) {
        return claims.getId();
    }

    public JwtPrincipal principalFromAccessClaims(final Claims claims) {
        return new JwtPrincipal(userIdFromSubject(claims), usernameFrom(claims));
    }
}
