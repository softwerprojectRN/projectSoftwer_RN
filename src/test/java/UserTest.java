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

    // Test constructor and getters
    @Test
    void testConstructorAndGetters() {
        assertEquals("testUser", user.getUsername());
        assertEquals("testPass", user.getPassword());
        assertFalse(user.isLoggedIn());
    }

    // Test login with correct credentials
    @Test
    void testLoginSuccess() {
        boolean result = user.login("testUser", "testPass");
        assertTrue(result);
        assertTrue(user.isLoggedIn());
    }

    // Test login with incorrect username
    @Test
    void testLoginFailureWrongUsername() {
        boolean result = user.login("wrongUser", "testPass");
        assertFalse(result);
        assertFalse(user.isLoggedIn());
    }

    // Test login with incorrect password
    @Test
    void testLoginFailureWrongPassword() {
        boolean result = user.login("testUser", "wrongPass");
        assertFalse(result, "Login should fail with incorrect password");
        assertFalse(user.isLoggedIn(), "User should not be logged in after failed login");
    }

    // Test logout when user is logged in
    @Test
    void testLogoutWhenLoggedIn() {
        user.login("testUser", "testPass");
        user.logout();
        assertFalse(user.isLoggedIn(), "User should be logged out after calling logout");
    }

    // Test logout when user is not logged in
    @Test
    void testLogoutWhenNotLoggedIn() {
        user.logout();
        assertFalse(user.isLoggedIn(), "User should remain logged out if not logged in");
    }

    // Test setUsername
    @Test
    void testSetUsername() {
        user.setUsername("newUser");
        assertEquals("newUser", user.getUsername(), "Username should be updated correctly");
    }

    // Test login sets loggedIn to true only once
    @Test
    void testMultipleLogin() {
        user.login("testUser", "testPass");
        assertTrue(user.isLoggedIn(), "User should be logged in after first login");
        // login again with correct credentials
        user.login("testUser", "testPass");
        assertTrue(user.isLoggedIn(), "User should remain logged in after second login");
    }

    // Test login does not set loggedIn if credentials are wrong
    @Test
    void testFailedLoginDoesNotChangeState() {
        user.login("wrongUser", "wrongPass");
        assertFalse(user.isLoggedIn(), "User should not be logged in after failed login");
    }

    // Test logout after failed login
    @Test
    void testLogoutAfterFailedLogin() {
        user.login("wrongUser", "wrongPass");
        user.logout();
        assertFalse(user.isLoggedIn(), "User should remain logged out after failed login and logout attempt");
    }
}
