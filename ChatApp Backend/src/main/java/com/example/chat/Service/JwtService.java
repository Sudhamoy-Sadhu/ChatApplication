package com.example.chat.Service;

import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.chat.Model.RefreshToken;
import com.example.chat.Model.User;
import com.example.chat.Repository.RefreshTokenRepository;
import com.example.chat.Utils.PemUtils;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.core.io.Resource;

@Service
public class JwtService {

    private final RSAPrivateKey privateKey;
    private final java.time.Duration accessTtl;
    private final java.time.Duration refreshTtl;
    private final String issuer;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(@Value("${security.jwt.private-key-path}") Resource priv,
                      @Value("${security.jwt.access-ttl}") java.time.Duration accessTtl,
                      @Value("${security.jwt.refresh-ttl}") java.time.Duration refreshTtl,
                      @Value("${security.jwt.issuer}") String issuer,
                      RefreshTokenRepository refreshTokenRepository) throws Exception {

        // Read and cast private key
        var key = PemUtils.readPrivateKey(priv.getInputStream());
        if (!(key instanceof RSAPrivateKey)) {
            throw new IllegalArgumentException("Private key is not an RSA key");
        }
        this.privateKey = (RSAPrivateKey) key;

        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
        this.issuer = issuer;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Create JWT access token
    public String createAccessToken(User user, Collection<String> roles) {
        var now = Instant.now();
        var claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer(issuer)
                .issueTime(java.util.Date.from(now))
                .expirationTime(java.util.Date.from(now.plus(accessTtl)))
                .claim("roles", roles)
                .build();

        var signer = new RSASSASigner(privateKey);
        var signed = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .type(JOSEObjectType.JWT)
                        .build(),
                claims);
        try {
            signed.sign(signer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
        return signed.serialize();
    }

    // Create refresh token and save to DB
    public String createRefreshToken(User user) {
        var now = Instant.now();
        var jti = UUID.randomUUID().toString();

        var claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer(issuer)
                .jwtID(jti)
                .issueTime(java.util.Date.from(now))
                .expirationTime(java.util.Date.from(now.plus(refreshTtl)))
                .claim("typ", "refresh")
                .build();

        var signed = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        try {
            signed.sign(new RSASSASigner(privateKey));
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign refresh token", e);
        }

        // Save refresh token to DB
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenId(jti);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(now.plus(refreshTtl));
        refreshToken.setActive(true);
        refreshTokenRepository.save(refreshToken);

        return signed.serialize();
    }

    // Validate refresh token against DB
    public boolean validateRefreshToken(String token) throws ParseException {
        var jwt = SignedJWT.parse(token);
        var jti = jwt.getJWTClaimsSet().getJWTID();
        var dbTokenOpt = refreshTokenRepository.findByTokenIdAndActiveTrue(jti);
        return dbTokenOpt.isPresent() && dbTokenOpt.get().getExpiresAt().isAfter(Instant.now());
    }
}
