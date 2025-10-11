import domain.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Ola", "1234");
    }

    @Test
    void testGetUsername() {
        assertEquals("Ola", user.getUsername());
    }

    @Test
    void testGetPassword() {
        assertEquals("1234", user.getPassword());
    }


    @Test
    void testUserIsLoggedOutByDefault() {
        assertFalse(user.isLoggedIn());
    }


    @Test
    void testSetUsernameUpdatesUsername() {
        user.setUsername("OlaRabi");
        assertEquals("OlaRabi", user.getUsername());
    }

    @Test
    void testLoginWithCorrectCredentialsReturnsTrue() {
        boolean result = user.login("Ola", "1234");
        assertTrue(result);
    }

    @Test
    void testLoginWithCorrectCredentialsSetsLoggedInTrue() {
        user.login("Ola", "1234");
        assertTrue(user.isLoggedIn());
    }

    @Test
    void testLoginWithIncorrectUsernameReturnsFalse() {
        boolean result = user.login("WrongUser", "1234");
        assertFalse(result);
    }

    @Test
    void testLoginWithIncorrectPasswordReturnsFalse() {
        boolean result = user.login("Ola", "wrongpass");
        assertFalse(result);
    }

    @Test
    void testLoginWithIncorrectCredentialsKeepsLoggedInFalse() {
        user.login("WrongUser", "wrongpass");
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testLogoutWhenLoggedInSetsLoggedInFalse() {
        user.login("Ola", "1234"); // log in first
        user.logout();
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testLogoutWhenNotLoggedInRemainsFalse() {
        user.logout(); // still false by default
        assertFalse(user.isLoggedIn());
    }
}
