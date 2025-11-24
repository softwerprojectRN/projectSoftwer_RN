import dao.UserDAO;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.PasswordUtil;
import service.UserService;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAOMock;

    @BeforeEach
    public void setup() throws Exception {
        userService = new UserService();
        userDAOMock = mock(UserDAO.class);

        // Inject mock DAO using reflection
        Field field = UserService.class.getDeclaredField("userDAO");
        field.setAccessible(true);
        field.set(userService, userDAOMock);
    }

    // -------------------------------------------------------------------------
    // register() Tests
    // -------------------------------------------------------------------------

    @Test
    public void testRegister_Success() {
        String username = "rahaf";
        String password = "12345";

        User expectedUser = new User(1, username, "hashed", "salt");
        when(userDAOMock.findByUsername(username)).thenReturn(null, expectedUser);
        when(userDAOMock.insert(anyString(), anyString(), anyString())).thenReturn(true);

        User result = userService.register(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userDAOMock).insert(anyString(), anyString(), anyString());
    }

    @Test
    public void testRegister_Fails_UserExists() {
        String username = "existing";


        User existingUser = new User(1, username, "hashedPass", "salt123");
        when(userDAOMock.findByUsername(username)).thenReturn(existingUser);

        User result = userService.register(username, "pass");

        assertNull(result);
        verify(userDAOMock, never()).insert(anyString(), anyString(), anyString());
    }


    @Test
    public void testRegister_Fails_EmptyFields() {
        assertNull(userService.register("", "password"));
        assertNull(userService.register("user", ""));
        assertNull(userService.register(null, "pass"));
        assertNull(userService.register("user", null));

        verify(userDAOMock, never()).insert(anyString(), anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // login() Tests
    // -------------------------------------------------------------------------

    @Test
    public void testLogin_Success() {
        String username = "rahaf";
        String password = "12345";
        String salt = PasswordUtil.generateSalt();
        String hashed = PasswordUtil.hashPassword(password, salt);

        User mockUser = new User(1, username, hashed, salt);
        when(userDAOMock.findByUsername(username)).thenReturn(mockUser);

        User result = userService.login(username, password);

        assertNotNull(result);
        assertTrue(result.isLoggedIn());
        assertEquals(username, result.getUsername());
    }

    @Test
    public void testLogin_Fails_WrongPassword() {
        String username = "rahaf";
        String salt = PasswordUtil.generateSalt();
        String correctHash = PasswordUtil.hashPassword("12345", salt);

        User mockUser = new User(1, username, correctHash, salt);
        when(userDAOMock.findByUsername(username)).thenReturn(mockUser);

        User result = userService.login(username, "wrongpassword");

        assertNull(result);
        assertFalse(mockUser.isLoggedIn());
    }

    @Test
    public void testLogin_Fails_UserNotFound() {
        when(userDAOMock.findByUsername("unknown")).thenReturn(null);

        User result = userService.login("unknown", "any");

        assertNull(result);
    }
}
