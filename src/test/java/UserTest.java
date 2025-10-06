import domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testUser", "testPass");
    }

    @Test
    void testLoginSuccess() {
        boolean result = user.login("testUser", "testPass");
        assertTrue(result, "Login should be successful with correct credentials");
        assertTrue(user.isLoggedIn(), "User should be marked as logged in after successful login");
    }

    @Test
    void testLoginFailure() {
        boolean result = user.login("wrongUser", "wrongPass");
        assertFalse(result, "Login should fail with incorrect credentials");
        assertFalse(user.isLoggedIn(), "User should not be logged in after failed login");
    }

    @Test
    void testLogoutWhenLoggedIn() {
        user.login("testUser", "testPass");
        user.logout();
        assertFalse(user.isLoggedIn(), "User should be logged out after calling logout");
    }

    @Test
    void testLogoutWhenNotLoggedIn() {
        user.logout();
        assertFalse(user.isLoggedIn(), "User should remain logged out if not logged in");
    }

    @Test
    void testSetUsername() {
        user.setUsername("newUser");
        assertEquals("newUser", user.getUsername(), "Username should be updated correctly");
    }
}
