//import model.UserWithOverdueBooks;
//
//import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.Test;
//
//public class UserWithOverdueBooksTest {
//
//    @Test
//    public void testConstructorAndGetters() {
//        int userId = 101;
//        String username = "rahaf";
//        int overdueCount = 3;
//
//        UserWithOverdueBooks user = new UserWithOverdueBooks(userId, username, overdueCount);
//
//        assertEquals(userId, user.getUserId());
//        assertEquals(username, user.getUsername());
//        assertEquals(overdueCount, user.getOverdueCount());
//    }
//
//    @Test
//    public void testOverdueCountZero() {
//        UserWithOverdueBooks user = new UserWithOverdueBooks(5, "userX", 0);
//
//        assertEquals(0, user.getOverdueCount());
//    }
//
//    @Test
//    public void testUsernameNotNull() {
//        UserWithOverdueBooks user = new UserWithOverdueBooks(7, "john", 2);
//
//        assertNotNull(user.getUsername());
//    }
//
//    @Test
//    public void testUserIdPositive() {
//        UserWithOverdueBooks user = new UserWithOverdueBooks(12, "sara", 4);
//
//        assertTrue(user.getUserId() > 0);
//    }
//}
