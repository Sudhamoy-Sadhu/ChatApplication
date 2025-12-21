package com.example.chat.Utils;

import java.util.Base64;

public class ImageUtils {

    private static final String DEFAULT_IMAGE = "/assets/default-logo.png";
    private static final String BASE64_PREFIX = "data:image/jpeg;base64,";

    public static String getProfilePicture(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return DEFAULT_IMAGE;
        }
        
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return BASE64_PREFIX + base64;
    }
}
