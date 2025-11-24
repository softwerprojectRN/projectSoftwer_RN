import service.*;

import dao.AdminDAO;
import dao.UserDAO;
import model.Admin;
import model.User;
import model.UserWithOverdueBooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import util.EmailNotifier;
import util.EmailServer;
import util.PasswordUtil;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

    private AdminService adminService;
    private AdminDAO adminDAOMock;
    private UserDAO userDAOMock;
    private EmailServer emailServerMock;
    private EmailNotifier emailNotifierMock;

    @BeforeEach
    public void setup() throws Exception {
        adminService = new AdminService();

        adminDAOMock = Mockito.mock(AdminDAO.class);
        userDAOMock = Mockito.mock(UserDAO.class);
        emailServerMock = Mockito.mock(EmailServer.class);
        emailNotifierMock = Mockito.mock(EmailNotifier.class);

        injectField(adminService, "adminDAO", adminDAOMock);
        injectField(adminService, "userDAO", userDAOMock);
        injectField(adminService, "emailServer", emailServerMock);
        injectField(adminService, "emailNotifier", emailNotifierMock);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // -------------------------------------------------------------------------

    @Test
    public void testRegister_Success() {
        Admin expected = new Admin(1, "admin", "hash", "salt");
        when(adminDAOMock.findByUsername("admin")).thenReturn(null, expected);
        when(adminDAOMock.insert(anyString(), anyString(), anyString())).thenReturn(true);

        Admin result = adminService.register("admin", "password");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        verify(adminDAOMock).insert(anyString(), anyString(), anyString());
    }

    @Test
    public void testRegister_FailsIfExists() {
        Admin existingAdmin = new Admin(1, "admin", "hash", "salt");
        when(adminDAOMock.findByUsername("admin")).thenReturn(existingAdmin);

        Admin result = adminService.register("admin", "password");

        assertNull(result);
        verify(adminDAOMock, never()).insert(anyString(), anyString(), anyString());
    }

    // -------------------------------------------------------------------------

    @Test
    public void testLogin_Success() {
        Admin admin = new Admin(1, "admin", PasswordUtil.hashPassword("pass", "salt"), "salt");

        when(adminDAOMock.findByUsername("admin")).thenReturn(admin);

        Admin loggedIn = adminService.login("admin", "pass");

        assertNotNull(loggedIn);
        assertTrue(loggedIn.isLoggedIn());
    }

    @Test
    public void testLogin_InvalidPassword() {
        Admin admin = new Admin(1, "admin", "hash123", "salt");
        when(adminDAOMock.findByUsername("admin")).thenReturn(admin);

        Admin result = adminService.login("admin", "wrongpass");

        assertNull(result);
    }

    @Test
    public void testLogin_UserNotFound() {
        when(adminDAOMock.findByUsername("unknown")).thenReturn(null);

        Admin result = adminService.login("unknown", "pass");

        assertNull(result);
    }

    // -------------------------------------------------------------------------

    @Test
    public void testUnregisterUser() {
        when(userDAOMock.delete("john")).thenReturn(true);

        boolean result = adminService.unregisterUser("john");

        assertTrue(result);
        verify(userDAOMock).delete("john");
    }

    // -------------------------------------------------------------------------

    @Test
    public void testSendOverdueReminders_SendsEmails() {
        UserWithOverdueBooks overdueUser = new UserWithOverdueBooks(1, "john", 2);
        BorrowingService borrowingService = mock(BorrowingService.class);

        when(borrowingService.getUsersWithOverdueBooks())
                .thenReturn(List.of(overdueUser));

        User dbUser = new User(1, "john", "hash", "salt");
        when(userDAOMock.findByUsername("john")).thenReturn(dbUser);

        adminService.sendOverdueReminders(borrowingService);

        verify(emailNotifierMock).notify(dbUser, "You have 2 overdue book(s).");
    }

    @Test
    public void testSendOverdueReminders_NoEmailServer() throws Exception {
        injectField(adminService, "emailNotifier", null);

        BorrowingService borrowingService = mock(BorrowingService.class);

        adminService.sendOverdueReminders(borrowingService);

        verify(emailNotifierMock, never()).notify(any(), anyString());
    }

    // -------------------------------------------------------------------------

    @Test
    public void testSetEmailServer() {
        EmailServer newServer = mock(EmailServer.class);

        adminService.setEmailServer(newServer);

        assertEquals(newServer, adminService.getEmailServer());
    }

}
