package com.nrkgo.accounts.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenUtils {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Generates a cryptographically strong random token.
     * Uses 32 bytes (256 bits) of entropy.
     * 
     * @return A URL-safe Base64 string (approx 43 characters).
     */
    public static String generateToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
