import model.Admin;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.Level;

class AdminTest {

    private Admin admin;
    private final ByteArrayOutputStream loggerOutput = new ByteArrayOutputStream();
    private final ByteArrayOutputStream systemOutput = new ByteArrayOutputStream();
    private StreamHandler handler;
    private Logger logger;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        admin = new Admin(100, "adminUser", "adminHash", "adminSalt");

        // Capture logger output
        logger = Logger.getLogger("model.Admin");
        handler = new StreamHandler(new PrintStream(loggerOutput), new java.util.logging.SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        // Capture System.out
        originalOut = System.out;
        System.setOut(new PrintStream(systemOutput));
    }

    @AfterEach
    void tearDown() {
        if (handler != null) {
            handler.flush();
            logger.removeHandler(handler);
            handler.close();
        }
        System.setOut(originalOut);
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
        handler.flush();

        String printed = loggerOutput.toString().trim();
        assertTrue(printed.contains("Admin username: adminUser"), 
                   "Expected output to contain 'Admin username: adminUser', but got: " + printed);
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

        String printed = systemOutput.toString().trim();
        assertTrue(printed.contains("Logged out successfully."));
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testAdminLogoutWhenNotLoggedIn() {
        admin.setLoggedIn(false);
        admin.logout();

        String printed = systemOutput.toString().trim();
        assertTrue(printed.contains("You are not logged in yet."));
        assertFalse(admin.isLoggedIn());
    }
}
