import model.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class AdminTest {

    private Admin admin;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        admin = new Admin(100, "adminUser", "adminHash", "adminSalt");

        // Capture System.out prints
        System.setOut(new PrintStream(output));
    }

    @Test
    void testConstructorAndInheritance() {
        // Check inherited fields from User
        assertEquals(100, admin.getId());
        assertEquals("adminUser", admin.getUsername());
        assertEquals("adminHash", admin.getPasswordHash());
        assertEquals("adminSalt", admin.getSalt());
        assertFalse(admin.isLoggedIn(), "Admin should not be logged in by default");
    }

    @Test
    void testShowAdminInfoPrintsCorrectMessage() {
        admin.showAdminInfo();

        String printed = output.toString().trim();
        assertEquals("Admin username: adminUser", printed);
    }

    @Test
    void testAdminCanUseInheritedSetters() {
        admin.setUsername("newAdmin");
        admin.setPasswordHash("newHash");
        admin.setSalt("newSalt");
        admin.setLoggedIn(true);
        admin.setId(200);

        assertEquals(200, admin.getId());
        assertEquals("newAdmin", admin.getUsername());
        assertEquals("newHash", admin.getPasswordHash());
        assertEquals("newSalt", admin.getSalt());
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testAdminLogout() {
        admin.setLoggedIn(true);
        admin.logout();

        String printed = output.toString().trim();
        assertTrue(printed.contains("Logged out successfully."));
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testAdminLogoutWhenNotLoggedIn() {
        admin.setLoggedIn(false);
        admin.logout();

        String printed = output.toString().trim();
        assertTrue(printed.contains("You are not logged in yet."));
        assertFalse(admin.isLoggedIn());
    }
}
