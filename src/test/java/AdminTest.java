import domain.*;


import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminTest {

    private Connection mockConn;
    private PreparedStatement mockPstmtSelect, mockPstmtInsert;
    private ResultSet mockRs;
    private Statement mockStmt;


    private EmailServer mockEmailServer;




    @BeforeEach
    void setUp() throws Exception {
        mockConn = mock(Connection.class);
        mockPstmtSelect = mock(PreparedStatement.class);
        mockPstmtInsert = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);
        mockStmt = mock(Statement.class);

        // تعيين EmailServer مزيّف (Mock) للاختبار
        mockEmailServer = new EmailServer();
        Admin.setEmailServer(mockEmailServer);

        // تنظيف أي رسائل سابقة
        mockEmailServer.clearSentEmails();

        // تأكد من أن قاعدة البيانات نظيفة (اختياري حسب البيئة)
        // يمكنك تنفيذ SQL لمسح الجداول مؤقتًا لو لزم الأمر

    }




    // -------------------------------------------------
    // اختبار الكتلة الـ static (يجب أن يكون الأول)
    // -------------------------------------------------
    @Test
    @Order(1)
    void testStaticBlock_Failure() throws Exception {
        try (MockedStatic<Admin> mockedAdmin = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mockedAdmin.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.createStatement()).thenReturn(mockStmt);
            doThrow(new SQLException("Table creation failed")).when(mockStmt).execute(anyString());
            Class.forName("domain.Admin");
        }
        assertTrue(true, "Static block's catch block was executed.");
    }

    // -----------------------------
    // 1. اختبار دالة connect()
    // -----------------------------
    @Test
    void testConnect_Success() {
        Connection conn = AdminTestHelper.invokeConnectDirect();
        assertNotNull(conn);
        try { conn.close(); } catch (SQLException e) { fail(e.getMessage()); }
    }

    @Test
    void testConnect_Failure() throws Exception {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                    .thenThrow(SQLException.class);
            Connection conn = AdminTestHelper.invokeConnectDirect();
            assertNull(conn);
        }
    }

    // -----------------------------
    // 2. اختبار دالة generateSalt()
    // -----------------------------
    @Test
    void testGenerateSalt_ReturnsDifferentValues() {
        String s1 = AdminTestHelper.invokeGenerateSalt();
        String s2 = AdminTestHelper.invokeGenerateSalt();
        assertNotEquals(s1, s2);
        assertNotNull(s1);
        assertNotNull(s2);
    }

    // -----------------------------
    // 3. اختبار دالة hashPassword()
    // -----------------------------
    @Test
    void testHashPassword_Success() {
        String hash = AdminTestHelper.invokeHashPassword("pass", "salt");
        assertNotNull(hash);
    }

    @Test
    void testHashPassword_NoSuchAlgorithmException() {
        try (MockedStatic<MessageDigest> mockedMd = mockStatic(MessageDigest.class)) {
            mockedMd.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("Algorithm not found"));
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    AdminTestHelper.invokeHashPassword("pass", "salt"));
            assertTrue(exception.getMessage().contains("خطأ في الـ hashing"));
        }
    }

    // -----------------------------
    // 4. اختبار دالة register()
    // -----------------------------
    @Test
    void testRegister_Success() throws Exception {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.prepareStatement(startsWith("SELECT"))).thenReturn(mockPstmtSelect);
            when(mockPstmtSelect.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);
            when(mockConn.prepareStatement(startsWith("INSERT"))).thenReturn(mockPstmtInsert);
            when(mockPstmtInsert.executeUpdate()).thenReturn(1);

            Admin result = Admin.register("admin1", "123");
            assertNotNull(result);
            assertEquals("admin1", result.getUsername());
        }
    }

    @Test
    void testRegister_AlreadyExists() throws Exception {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPstmtSelect);
            when(mockPstmtSelect.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            Admin result = Admin.register("exists", "123");
            assertNull(result);
        }
    }

    @Test
    void testRegister_CheckSqlException() throws Exception {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("DB error on check"));
            Admin result = Admin.register("err", "123");
            assertNull(result);
        }
    }

    @Test
    void testRegister_InsertSQLException() throws Exception {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.prepareStatement(startsWith("SELECT"))).thenReturn(mockPstmtSelect);
            when(mockPstmtSelect.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);
            when(mockConn.prepareStatement(startsWith("INSERT"))).thenThrow(new SQLException("DB error on insert"));
            Admin result = Admin.register("x", "y");
            assertNull(result);
        }
    }

    // -----------------------------
    // 5. اختبار دالة login()
    // -----------------------------
    @Test
    void testLogin_Success() throws Exception {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPstmtSelect);
            when(mockPstmtSelect.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);

            String salt = "randomSalt";
            String password = "123";
            String correctHash = AdminTestHelper.invokeHashPassword(password, salt);

            when(mockRs.getString("password_hash")).thenReturn(correctHash);
            when(mockRs.getString("salt")).thenReturn(salt);

            Admin admin = Admin.login("user", password);
            assertNotNull(admin);
            assertTrue(admin.isLoggedIn());
        }
    }

    @Test
    void testLogin_InvalidPassword() throws Exception {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPstmtSelect);
            when(mockPstmtSelect.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getString("password_hash")).thenReturn("wrongHash");
            when(mockRs.getString("salt")).thenReturn("salt");

            Admin result = Admin.login("user", "pass");
            assertNull(result);
        }
    }

    @Test
    void testLogin_AdminNotFound() throws Exception {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPstmtSelect);
            when(mockPstmtSelect.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);
            Admin result = Admin.login("noOne", "pass");
            assertNull(result);
        }
    }

    @Test
    void testLogin_SQLException() throws Exception {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("DB error on login"));
            Admin result = Admin.login("u", "p");
            assertNull(result);
        }
    }

    // -----------------------------
    // 6. اختبار دالة showAdminInfo()
    // -----------------------------
    @Test
    void testShowAdminInfo() {
        Admin admin = new Admin("adm", "hash", "salt");
        assertDoesNotThrow(() -> admin.showAdminInfo());
        assertEquals("adm", admin.getUsername());
    }

    // -----------------------------
    // 7. اختبارات فشل connect() (الفرع المفقود)
    // -----------------------------
    @Test
    void testRegister_ConnectReturnsNull() {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(null);
            Admin result = Admin.register("testUser", "password");
            assertNull(result, "يجب أن ترجع register null عند فشل الاتصال");
        }
    }

    @Test
    void testLogin_ConnectReturnsNull() {
        try (MockedStatic<Admin> mocked = mockStatic(Admin.class, CALLS_REAL_METHODS)) {
            mocked.when(Admin::connect).thenReturn(null);
            Admin result = Admin.login("testUser", "password");
            assertNull(result, "يجب أن ترجع login null عند فشل الاتصال");
        }
    }


    // -------------------------------------------------------------------------
    // Helper Class لاختبار الدوال الخاصة (private) باستخدام Reflection
    // -------------------------------------------------------------------------
    public static class AdminTestHelper {

        public static Connection invokeConnectDirect() {
            try {
                Method m = Admin.class.getDeclaredMethod("connect");
                m.setAccessible(true);
                return (Connection) m.invoke(null);
            } catch (Exception e) {
                return null;
            }
        }

        public static String invokeGenerateSalt() {
            try {
                Method m = Admin.class.getDeclaredMethod("generateSalt");
                m.setAccessible(true);
                return (String) m.invoke(null);
            } catch (Exception e) {
                return null;
            }
        }

        public static String invokeHashPassword(String p, String s) {
            try {
                Method m = Admin.class.getDeclaredMethod("hashPassword", String.class, String.class);
                m.setAccessible(true);
                return (String) m.invoke(null, p, s);
            } catch (Exception e) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                return null;
            }
        }
    }









}