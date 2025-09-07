package com.example.chat.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for reading RSA public/private keys from PEM files.
 */
public class PemUtils {

    /**
     * Reads an RSA PublicKey from a PEM InputStream.
     */
    public static PublicKey readPublicKey(InputStream input) throws IOException, GeneralSecurityException {
        String key = new String(input.readAllBytes());
        String sanitized = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(sanitized);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * Reads an RSA PrivateKey from a PEM InputStream.
     */
    public static PrivateKey readPrivateKey(InputStream input) throws IOException, GeneralSecurityException {
        String key = new String(input.readAllBytes());
        String sanitized = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(sanitized);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
