package com.example.chat.Utils;

import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TokenHashUtil {

    public static String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] result = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result);
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }
}
