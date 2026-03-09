package gasi.gps.core.api.infrastructure.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility for hashing values with standard algorithms.
 */
public final class HashUtil {

    private HashUtil() {
    }

    /**
     * Hashes input text using SHA-256 and encodes as Base64.
     *
     * @param value value to hash
     * @return Base64 SHA-256 hash
     */
    public static String sha256Base64(String value) {
        return digestBase64("SHA-256", value);
    }

    /**
     * Hashes input text with the given algorithm and encodes as Base64.
     *
     * @param algorithm message digest algorithm (e.g. SHA-256, SHA-512)
     * @param value     value to hash
     * @return Base64 digest
     */
    public static String digestBase64(String algorithm, String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Hash algorithm is not available: " + algorithm, ex);
        }
    }
}
