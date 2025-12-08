package dao;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and salt generation with enhanced error handling.
 *
 * Provides methods to generate a cryptographically secure salt and hash passwords
 * using SHA-256 combined with the salt. Includes explicit null checks and validation
 * for Base64-encoded salts.
 *
 * Usage example:
 *
 * String salt = PasswordUtil.generateSalt();
 * String hashedPassword = PasswordUtil.hashPassword("myPassword", salt);
 *
 *
 * Author: Library Management System
 * Version: 1.0
 */
public class PasswordUtil {
    private PasswordUtil() {
        throw new IllegalStateException("Utility class");
    }
    /**
     * Generates a random salt encoded in Base64.
     *
     * @return a Base64-encoded random salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password using SHA-256 combined with the provided salt.
     * Performs null checks and validates that the salt is a proper Base64 string.
     *
     * @param password the plain text password (must not be null)
     * @param salt the Base64-encoded salt (must not be null)
     * @return the Base64-encoded hashed password
     * @throws IllegalArgumentException if password or salt is null or salt is invalid Base64
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    public static String hashPassword(String password, String salt) {
        if (password == null) throw new IllegalArgumentException("Password cannot be null.");
        if (salt == null) throw new IllegalArgumentException("Salt cannot be null.");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Hashing algorithm not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid salt format. Salt must be a valid Base64 string.", e);
        }
    }
}