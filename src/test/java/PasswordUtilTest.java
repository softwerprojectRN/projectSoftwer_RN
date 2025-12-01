
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import dao.PasswordUtil;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for PasswordUtil")
class PasswordUtilTest {

    private static final String TEST_PASSWORD = "mySecurePassword123!";
    private String salt;

    @BeforeEach
    void setUp() {
        salt = PasswordUtil.generateSalt();
    }

    @Nested
    @DisplayName("generateSalt() Tests")
    class GenerateSaltTests {

        @Test
        @DisplayName("generateSalt() should not return null")
        void testGenerateSaltShouldNotBeNull() {
            assertNotNull(salt);
        }

        @Test
        @DisplayName("generateSalt() should produce unique salts on multiple calls")
        void testGenerateSaltShouldBeUnique() {
            String salt1 = PasswordUtil.generateSalt();
            String salt2 = PasswordUtil.generateSalt();

            assertNotEquals(salt1, salt2, "Two generated salts should not be the same");

            Set<String> salts = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                salts.add(PasswordUtil.generateSalt());
            }
            assertEquals(100, salts.size(), "All 100 generated salts should be unique");
        }

        @Test
        @DisplayName("generateSalt() should produce a valid Base64 string")
        void testGenerateSaltShouldBeBase64() {
            assertDoesNotThrow(() -> Base64.getDecoder().decode(salt));
        }

        @Test
        @DisplayName("generateSalt() should produce a salt of consistent length")
        void testGenerateSaltShouldHaveConsistentLength() {
            assertEquals(24, salt.length());
        }
    }

    @Nested
    @DisplayName("hashPassword() Tests")
    class HashPasswordTests {

        @Test
        @DisplayName("hashPassword() with same password and salt should produce the same hash")
        void testHashPasswordWithSameInputsShouldBeIdentical() {
            String hash1 = PasswordUtil.hashPassword(TEST_PASSWORD, salt);
            String hash2 = PasswordUtil.hashPassword(TEST_PASSWORD, salt);

            assertEquals(hash1, hash2, "Hashing the same password with the same salt must be deterministic");
        }

        @Test
        @DisplayName("hashPassword() with same password but different salts should produce different hashes")
        void testHashPasswordWithDifferentSaltsShouldBeDifferent() {
            String salt1 = PasswordUtil.generateSalt();
            String salt2 = PasswordUtil.generateSalt();

            String hash1 = PasswordUtil.hashPassword(TEST_PASSWORD, salt1);
            String hash2 = PasswordUtil.hashPassword(TEST_PASSWORD, salt2);

            assertNotEquals(hash1, hash2, "Hashing the same password with different salts must produce different results");
        }

        @Test
        @DisplayName("hashPassword() with different passwords and same salt should produce different hashes")
        void testHashPasswordWithDifferentPasswordsShouldBeDifferent() {
            String hash1 = PasswordUtil.hashPassword(TEST_PASSWORD, salt);
            String hash2 = PasswordUtil.hashPassword("anotherPassword", salt);

            assertNotEquals(hash1, hash2, "Hashing different passwords with the same salt must produce different results");
        }

        @Test
        @DisplayName("hashPassword() should produce a valid Base64 string")
        void testHashPasswordShouldBeBase64() {
            String hash = PasswordUtil.hashPassword(TEST_PASSWORD, salt);
            assertDoesNotThrow(() -> Base64.getDecoder().decode(hash));
        }

        @Test
        @DisplayName("hashPassword() should throw IllegalArgumentException for null password")
        void testHashPasswordWithNullPassword() {
            // UPDATED: Now expects IllegalArgumentException for consistency
            assertThrows(IllegalArgumentException.class, () -> {
                PasswordUtil.hashPassword(null, salt);
            });
        }

        @Test
        @DisplayName("hashPassword() should throw IllegalArgumentException for null salt")
        void testHashPasswordWithNullSalt() {
            assertThrows(IllegalArgumentException.class, () -> {
                PasswordUtil.hashPassword(TEST_PASSWORD, null);
            });
        }

        @Test
        @DisplayName("hashPassword() should throw IllegalArgumentException for invalid salt format")
        void testHashPasswordWithInvalidSaltFormat() {
            String invalidSalt = "this-is-not-a-valid-base64-string!";
            assertThrows(IllegalArgumentException.class, () -> {
                PasswordUtil.hashPassword(TEST_PASSWORD, invalidSalt);
            });
        }
    }
}