//package model;
//
//import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//class UserTest {
//
//    private User user;
//
//    @BeforeEach
//    void setUp() {
//        user = new User(1, "rahaf", "hash123", "salt123");
//    }
//
//    @Test
//    void testConstructor() {
//        assertEquals(1, user.getId());
//        assertEquals("rahaf", user.getUsername());
//        assertEquals("hash123", user.getPasswordHash());
//        assertEquals("salt123", user.getSalt());
//        assertFalse(user.isLoggedIn(), "User should start as logged out");
//    }
//
//    @Test
//    void testIdSetterGetter() {
//        user.setId(20);
//        assertEquals(20, user.getId());
//    }
//
//    @Test
//    void testUsernameSetterGetter() {
//        user.setUsername("newUser");
//        assertEquals("newUser", user.getUsername());
//    }
//
//    @Test
//    void testPasswordHashSetterGetter() {
//        user.setPasswordHash("newHash");
//        assertEquals("newHash", user.getPasswordHash());
//    }
//
//    @Test
//    void testSaltSetterGetter() {
//        user.setSalt("newSalt");
//        assertEquals("newSalt", user.getSalt());
//    }
//
//    @Test
//    void testLoggedInSetterGetter() {
//        user.setLoggedIn(true);
//        assertTrue(user.isLoggedIn());
//
//        user.setLoggedIn(false);
//        assertFalse(user.isLoggedIn());
//    }
//
//    @Test
//    void testLogoutWhenLoggedIn() {
//        user.setLoggedIn(true);
//
//        user.logout();
//
//        assertFalse(user.isLoggedIn(), "User should become logged out");
//    }
//
//    @Test
//    void testLogoutWhenNotLoggedIn() {
//        user.setLoggedIn(false);
//
//        user.logout();
//
//        assertFalse(user.isLoggedIn(), "User should remain logged out");
//    }
//}
