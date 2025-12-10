
import dao.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    // ==================== Tests for generateSalt() ====================

    @Test
    @DisplayName("generateSalt() should return a non-null, non-empty string")
    void testGenerateSalt_returnsValidString() {
        String salt = PasswordUtil.generateSalt();
        assertNotNull(salt, "Generated salt should not be null.");
        assertFalse(salt.isEmpty(), "Generated salt should not be empty.");
    }

    @Test
    @DisplayName("generateSalt() should return a valid Base64 string")
    void testGenerateSalt_returnsValidBase64() {
        String salt = PasswordUtil.generateSalt();
        // If the string is not valid Base64, this will throw an IllegalArgumentException
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(salt),
                "Generated salt should be a valid Base64 string.");
    }

    @Test
    @DisplayName("generateSalt() should produce different salts on multiple calls")
    void testGenerateSalt_producesUniqueSalts() {
        // This is a probabilistic test. It's highly unlikely for a secure random generator
        // to produce the same value twice in a small number of tries.
        int attempts = 5;
        Set<String> salts = new HashSet<>();
        for (int i = 0; i < attempts; i++) {
            salts.add(PasswordUtil.generateSalt());
        }
        assertTrue(salts.size() > 1, "SecureRandom should produce different salts on multiple calls.");
    }


    // ==================== Tests for hashPassword() ====================

    @Test
    @DisplayName("hashPassword() with valid inputs should return a hash")
    void testHashPassword_happyPath() {
        String password = "mySecurePassword123";
        String salt = PasswordUtil.generateSalt(); // Use a valid salt

        String hashedPassword = PasswordUtil.hashPassword(password, salt);

        assertNotNull(hashedPassword, "Hashed password should not be null.");
        assertFalse(hashedPassword.isEmpty(), "Hashed password should not be empty.");
    }

    @Test
    @DisplayName("hashPassword() should be idempotent for same inputs")
    void testHashPassword_isIdempotent() {
        String password = "mySecurePassword123";
        String salt = PasswordUtil.generateSalt();

        String hash1 = PasswordUtil.hashPassword(password, salt);
        String hash2 = PasswordUtil.hashPassword(password, salt);

        assertEquals(hash1, hash2, "Hashing the same password with the same salt should produce the same result.");
    }

    @Test
    @DisplayName("hashPassword() with different salts should produce different hashes")
    void testHashPassword_differentSaltsDifferentHashes() {
        String password = "mySecurePassword123";
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();

        String hash1 = PasswordUtil.hashPassword(password, salt1);
        String hash2 = PasswordUtil.hashPassword(password, salt2);

        assertNotEquals(hash1, hash2, "Hashing the same password with different salts should produce different results.");
    }

    @Test
    @DisplayName("hashPassword() with different passwords should produce different hashes")
    void testHashPassword_differentPasswordsDifferentHashes() {
        String password1 = "passwordOne";
        String password2 = "passwordTwo";
        String salt = PasswordUtil.generateSalt();

        String hash1 = PasswordUtil.hashPassword(password1, salt);
        String hash2 = PasswordUtil.hashPassword(password2, salt);

        assertNotEquals(hash1, hash2, "Hashing different passwords with the same salt should produce different results.");
    }


    // ==================== Tests for hashPassword() Error Handling ====================

    @Test
    @DisplayName("hashPassword() with null password should throw IllegalArgumentException")
    void testHashPassword_nullPassword_throwsException() {
        String salt = PasswordUtil.generateSalt();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword(null, salt);
        });
        assertEquals("Password cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("hashPassword() with null salt should throw IllegalArgumentException")
    void testHashPassword_nullSalt_throwsException() {
        String password = "mySecurePassword123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword(password, null);
        });
        assertEquals("Salt cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("hashPassword() with invalid Base64 salt should throw IllegalArgumentException")
    void testHashPassword_invalidSalt_throwsException() {
        String password = "mySecurePassword123";
        String invalidSalt = "this is not a valid base64 string!@#$";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword(password, invalidSalt);
        });
        assertEquals("Invalid salt format. Salt must be a valid Base64 string.", exception.getMessage());
    }

    // Note: Testing the NoSuchAlgorithmException case for MessageDigest.getInstance("SHA-256")
    // would require mocking the MessageDigest class itself, which is complex and often
    // not necessary for this level of testing. SHA-256 is a standard, guaranteed algorithm.
    // The current implementation correctly handles this theoretical case by wrapping it in an
    // IllegalArgumentException, which is good design.


    // ==================== Test for Private Constructor ====================

    @Test
    @DisplayName("Private constructor should throw IllegalStateException")
    void testPrivateConstructor() throws Exception {
        Constructor<PasswordUtil> constructor = PasswordUtil.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");

        // --- FIX IS HERE ---
        // We must catch the InvocationTargetException and check its cause.
        IllegalStateException thrownException = assertThrows(IllegalStateException.class, () -> {
            try {
                constructor.setAccessible(true);
                constructor.newInstance();
                // If we reach here, the test should fail
                fail("Expected IllegalStateException to be thrown");
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Unwrap the actual exception thrown by the constructor
                Throwable cause = e.getCause();
                if (cause instanceof IllegalStateException) {
                    throw (IllegalStateException) cause; // Re-throw to be caught by assertThrows
                } else {
                    fail("Expected IllegalStateException as cause, but got: " + cause.getClass().getName());
                }
            }
        });

        assertEquals("Utility class", thrownException.getMessage());
    }
}