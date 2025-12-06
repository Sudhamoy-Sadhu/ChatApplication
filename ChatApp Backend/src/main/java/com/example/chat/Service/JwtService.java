package com.example.chat.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.example.chat.DTO.LoginResponseDTO;
import com.example.chat.Model.RefreshToken;
import com.example.chat.Model.User;
import com.example.chat.Repository.RefreshTokenRepository;
import com.example.chat.Utils.PemUtils;
import com.example.chat.Utils.TokenHashUtil;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final java.time.Duration accessTtl;
    private final java.time.Duration refreshTtl;
    private final String issuer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final String jtiHmacSecret;

    public JwtService(@Value("${security.jwt.private-key-path}") Resource priv,
            @Value("${security.jwt.public-key-path}") Resource pub,
            @Value("${security.jwt.access-ttl}") java.time.Duration accessTtl,
            @Value("${security.jwt.refresh-ttl}") java.time.Duration refreshTtl,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.jti-hmac-secret}") String jtiHmacSecret,
            RefreshTokenRepository refreshTokenRepository) throws Exception {

        var key = PemUtils.readPrivateKey(priv.getInputStream());
        if (!(key instanceof RSAPrivateKey)) {
            throw new IllegalArgumentException("Private key is not an RSA key");
        }
        this.privateKey = (RSAPrivateKey) key;

        var pubKey = PemUtils.readPublicKey(pub.getInputStream());
        if (!(pubKey instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("Public key is not an RSA key");
        }
        this.publicKey = (RSAPublicKey) pubKey;

        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
        this.issuer = issuer;
        this.jtiHmacSecret = jtiHmacSecret;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createAccessToken(User user, Collection<String> roles) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(accessTtl)))
                .claim("roles", roles)
                .build();

        SignedJWT signed = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build(), claims);
        try {
            signed.sign(new RSASSASigner(privateKey));
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign access token", e);
        }
        return signed.serialize();
    }

    /**
     * Creates a refresh token, stores its hashed jti in DB and returns the
     * serialized token.
     * Caller should return both access token and refresh token to the client.
     */
    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer(issuer)
                .jwtID(jti)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(refreshTtl)))
                .claim("typ", "refresh")
                .build();

        SignedJWT signed = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
                claims);
        try {
            signed.sign(new RSASSASigner(privateKey));
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign refresh token", e);
        }

        // store hashed jti
        String hash = TokenHashUtil.hmacSha256Hex(jtiHmacSecret, jti);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hash);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(now.plus(refreshTtl));
        refreshToken.setActive(true);
        refreshToken.setCreatedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return signed.serialize();
    }

    /**
     * Validates refresh token signature and DB record. Does NOT rotate.
     */
    public boolean validateRefreshToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            // verify signature
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwt.verify(verifier))
                return false;

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            if (!issuer.equals(claims.getIssuer()))
                return false;
            if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(Instant.now()))
                return false;
            if (!"refresh".equals(claims.getStringClaim("typ")))
                return false;

            String jti = claims.getJWTID();
            String hash = TokenHashUtil.hmacSha256Hex(jtiHmacSecret, jti);
            Optional<RefreshToken> db = refreshTokenRepository.findByTokenHashAndActiveTrue(hash);
            return db.isPresent() && db.get().getExpiresAt().isAfter(Instant.now());

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rotates refresh token: checks provided refresh token, marks old DB row
     * inactive,
     * issues new refresh token and DB row, returns new token string.
     * Throws RuntimeException for invalid token.
     */
    public String rotateRefreshToken(String oldToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(oldToken);

            // verify signature
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwt.verify(verifier))
                throw new RuntimeException("Invalid refresh token signature");

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!issuer.equals(claims.getIssuer()))
                throw new RuntimeException("Invalid issuer");
            if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(Instant.now()))
                throw new RuntimeException("Refresh token expired");
            if (!"refresh".equals(claims.getStringClaim("typ")))
                throw new RuntimeException("Not a refresh token");

            String jti = claims.getJWTID();
            String hash = TokenHashUtil.hmacSha256Hex(jtiHmacSecret, jti);

            RefreshToken dbToken = refreshTokenRepository.findByTokenHashAndActiveTrue(hash)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found or inactive"));

            // mark old inactive
            dbToken.setActive(false);
            dbToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(dbToken);

            // create new token for same user
            User user = dbToken.getUser();
            return createRefreshToken(user);

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to rotate refresh token", ex);
        }
    }

    /**
     * Revoke a refresh token (by token string)
     */
    public void revokeRefreshToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            String jti = jwt.getJWTClaimsSet().getJWTID();
            String hash = TokenHashUtil.hmacSha256Hex(jtiHmacSecret, jti);
            refreshTokenRepository.findByTokenHashAndActiveTrue(hash).ifPresent(rt -> {
                rt.setActive(false);
                rt.setRevokedAt(Instant.now());
                refreshTokenRepository.save(rt);
            });
        } catch (Exception ignored) {
        }
    }

    // Validate an access token signature, issuer and expiry
    public boolean validateAccessToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwt.verify(verifier))
                return false;

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!issuer.equals(claims.getIssuer()))
                return false;
            if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(Instant.now()))
                return false;

            // you might also check token type claim if you set one for access tokens
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract subject (user id) from any valid JWT token (returns null if invalid)
    public String extractUsername(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            // optionally verify signature here too:
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwt.verify(verifier))
                return null;
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // Extract roles claim (if present) as collection of strings
    @SuppressWarnings("unchecked")
    public Collection<String> extractRoles(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwt.verify(verifier))
                return java.util.Collections.emptyList();
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Object rolesClaim = claims.getClaim("roles");
            if (rolesClaim instanceof Collection) {
                return (Collection<String>) rolesClaim;
            }
            return java.util.Collections.emptyList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public LoginResponseDTO loginUser(User user) {
        Long id = user.getId();
        String accessToken = createAccessToken(user, Set.of("USER"));
        String username = user.getUsername();
        String email = user.getEmail();
        User.Status status = user.getStatus();
        return new LoginResponseDTO(id,accessToken, username, email, status);
    }

}
