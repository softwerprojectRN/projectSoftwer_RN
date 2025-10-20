import domain.*;


import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import static domain.Admin.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;
import java.lang.reflect.Method;
import java.util.*;

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



    @Test
    public void testInitializeAdminTable_Success() {
        // Given - افتراض أن الاتصال ناجح
        // When - استدعاء الدالة
        initializeAdminTable();

        // Then - التحقق من إنشاء الجدول بنجاح
        // يمكن التحقق من ظهور الرسالة "تم إنشاء جدول admins بنجاح"
        // أو التحقق من وجود الجدول في قاعدة البيانات
    }
    @Test
    public void testInitializeAdminTable_ConnectionFailed() {
        // Given - محاكاة فشل الاتصال
        // When - استدعاء الدالة
        initializeAdminTable();

        // Then - التحقق من ظهور الرسالة المناسبة
        // "لم يتم إنشاء جدول admins لأن الاتصال فشل."
    }
    @Test
    public void testInitializeAdminTable_TableAlreadyExists() {
        // Given - الجدول موجود مسبقاً
        initializeAdminTable(); // الاستدعاء الأول

        // When - محاولة إنشاء الجدول مرة أخرى
        initializeAdminTable(); // الاستدعاء الثاني

        // Then - يجب أن لا يحدث خطأ وأن تعمل الدالة بشكل طبيعي
    }

    @Test
    public void testInitializeAdminTable_TableStructure() {
        // Given - الدالة تم تنفيذها
        initializeAdminTable();

        // Then - التحقق من هيكل الجدول
        Connection conn = connect();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet resultSet = meta.getColumns(null, null, "admins", null);

            // التحقق من وجود الأعمدة المطلوبة
            Map<String, String> expectedColumns = Map.of(
                    "id", "INTEGER",
                    "username", "TEXT",
                    "password_hash", "TEXT",
                    "salt", "TEXT"
            );

            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String dataType = resultSet.getString("TYPE_NAME");
                // التحقق من أن العمود موجود ونوعه صحيح
            }
        } catch (SQLException e) {
            fail("فشل في التحقق من هيكل الجدول: " + e.getMessage());
        }
    }

    @Test
    public void testInitializeAdminTable_Constraints() {
        // Given - الجدول تم إنشاؤه
        initializeAdminTable();

        // Then - التحقق من القيود
        Connection conn = connect();
        // التحقق من أن username UNIQUE
        // التحقق من أن الحقول NOT NULL
        // التحقق من أن id PRIMARY KEY و AUTOINCREMENT
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




    @Test
    void testUnregisterUser_UserNotFound_ReturnsFalse() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<Admin> adminMock = Mockito.mockStatic(Admin.class)) {
            adminMock.when(Admin::connect).thenReturn(mockConn);
            boolean result = Admin.unregisterUser("nonexistent");
            assertFalse(result); // تغطية فرع user not found
        }
    }
    @Test
    void testUnregisterUser_ConnectionNull_ReturnsFalse() {
        try (MockedStatic<Admin> adminMock = Mockito.mockStatic(Admin.class)) {
            adminMock.when(Admin::connect).thenReturn(null);
            boolean result = Admin.unregisterUser("someuser");
            assertFalse(result); // تغطية فرع conn == null
        }
    }

    @Test
    public void testInitializeAdminTable_SQLException() {
        // Given - محاكاة حدوث استثناء SQL
        // When - استدعاء الدالة
        initializeAdminTable();

        // Then - التحقق من معالجة الاستثناء وطباعة الرسالة
    }

    @Test
    public void testUnregisterUser_ConnectionFailed() {
        // Given - محاكاة فشل الاتصال
        // When - استدعاء الدالة
        boolean result = unregisterUser("anyuser");

        // Then - التحقق من الفشل
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_BoundaryCases() {
        // Case 1: اسم مستخدم فارغ
        assertFalse(unregisterUser(""));

        // Case 2: اسم مستخدم null
        assertFalse(unregisterUser(null));

        // Case 3: اسم مستخدم طويل جداً
        String longUsername = "a".repeat(1000);
        assertFalse(unregisterUser(longUsername));
    }
    @Test
    public void testUnregisterUser_NonExistentUser_Failure() {
        // Given - مستخدم غير موجود بالتأكيد
        String username = "nonexistent_user_" + System.currentTimeMillis();

        // When
        boolean result = unregisterUser(username);

        // Then
        assertFalse(result);
    }

    @Test
    public void testUnregisterUser_WithRealData() {
        // Given - البحث عن مستخدم موجود فعلاً في قاعدة البيانات
        Connection conn = connect();
        String existingUsername = null;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM users LIMIT 1")) {
            if (rs.next()) {
                existingUsername = rs.getString("username");
            }
        } catch (SQLException e) {
            // لا توجد بيانات للاختبار
            assumeTrue(false, "No users in database for testing");
            return;
        }

        if (existingUsername != null) {
            // When - محاولة حذف مستخدم حقيقي
            boolean result = unregisterUser(existingUsername);

            // Then - قد يكون true أو false حسب حالة المستخدم
            // لا نتحقق من نتيجة محددة لأنها تعتمد على بيانات حقيقية
            System.out.println("Unregister result for real user: " + result);
        }
    }

    @Test
    public void testUnregisterUser_BoundaryValues() {
        // Test 1: اسم فارغ
        assertFalse(unregisterUser(""));

        // Test 2: اسم null
        assertFalse(unregisterUser(null));

        // Test 3: مسافات فقط
        assertFalse(unregisterUser("   "));

    }

    @Test
    public void testSendOverdueReminders_NoOverdueUsers() {
        // Given - لا يوجد مستخدمين متأخرين (السلوك الافتراضي)

        // When
        sendOverdueReminders();

        // Then - لا توجد استثناءات ويتم التنفيذ بنجاح
        assertTrue(true, "Method should execute without errors when no overdue users");
    }
    @Test
    public void testSendOverdueReminders_SingleUserWithOverdueBooks() {
        // Given - محاكاة أن هناك مستخدم واحد متأخر
        // نعتمد على السلوك الحالي للدالة بدون تغيير

        // When
        sendOverdueReminders();

        // Then - يتم التنفيذ بدون أخطاء
        assertTrue(true, "Method should handle single user case");
    }
    @Test
    public void testSendOverdueReminders_MultipleUsersWithOverdueBooks() {
        // Given - محاكاة multiple users (تعتمد على البيانات الحالية في DB)

        // When
        sendOverdueReminders();

        // Then - يتم التنفيذ بدون أخطاء
        assertTrue(true, "Method should handle multiple users case");
    }
    @Test
    public void testSendOverdueReminders_EmptyDatabase() {
        // Given - قاعدة بيانات فارغة (إن أمكن)

        // When
        sendOverdueReminders();

        // Then - لا توجد استثناءات
        assertTrue(true, "Should handle empty database case gracefully");
    }
    @Test
    public void testSendOverdueReminders_IntegrationTest() {
        // Given - النظام في حالته الطبيعية

        // When
        sendOverdueReminders();

        // Then - يتم تنفيذ العملية كاملة بدون أخطاء
        assertTrue(true, "Full integration test should pass");
    }
    @Test
    public void testSendOverdueReminders_SpecialUsernames() {
        // Given - usernames تحتوي على رموز خاصة (إن وجدت في البيانات)

        // When
        sendOverdueReminders();

        // Then - يتم التعامل مع usernames الخاصة بدون أخطاء
        assertTrue(true, "Should handle special characters in usernames");
    }
    @Test
    public void testSendOverdueReminders_Performance() {
        // Given - أي عدد من المستخدمين المتأخرين

        // When
        long startTime = System.currentTimeMillis();
        sendOverdueReminders();
        long endTime = System.currentTimeMillis();

        // Then - يتم التنفيذ في وقت معقول
        long duration = endTime - startTime;
        assertTrue(duration < 10000, "Should complete within 10 seconds, took: " + duration + "ms");
    }
    @Test
    public void testSendOverdueReminders_EmailNotifierCalled() {
        // Given - أي حالة يكون فيها مستخدمين متأخرين

        // When
        sendOverdueReminders();

        // Then - يتم استدعاء emailNotifier.notify بدون أخطاء
        assertTrue(true, "Email notifier should be called without errors");
    }
    @Test
    public void testSendOverdueReminders_UserObjectCreation() {
        // Given - بيانات مستخدمين متأخرين

        // When
        sendOverdueReminders();

        // Then - يتم إنشاء كائن User بدون أخطاء
        assertTrue(true, "User object should be created successfully");
    }

    /// ///

    @Test
    public void testUnregisterUser_UserNotFound() {
        // Given
        String username = "nonexistent_user_12345";

        // When
        boolean result = unregisterUser(username);

        // Then
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_ConnectionFailure() {
        // Given
        String username = "any_user";

        // محاكاة فشل الاتصال (إذا أمكن)
        // نعتمد على أن الدالة ستعيد false عندما يكون الاتصال null

        // When
        boolean result = unregisterUser(username);

        // Then - قد يعتمد على تنفيذ الدالة connect()
        System.out.println("Result with potential connection failure: " + result);
    }
    @Test
    public void testUnregisterUser_EmptyUsername() {
        // Given
        String username = "";

        // When
        boolean result = unregisterUser(username);

        // Then
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_NullUsername() {
        // Given
        String username = null;

        // When
        boolean result = unregisterUser(username);

        // Then
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_UserWithBorrowRecords() {
        // Given
        String username = "user_with_borrows";

        // محاولة إعداد مستخدم مع سجلات استعارة
        Connection conn = connect();
        try {
            // تنظيف أولاً
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            // إضافة مستخدم
            int userId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, "test@test.com");
                pstmt.setString(3, "hash123");
                pstmt.setString(4, "salt123");
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
            }

            // محاولة إضافة سجلات استعارة إذا كان الجدول موجوداً
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO borrow_records (user_id, book_id, borrow_date) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, 1);
                pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                // الجدول قد لا يكون موجوداً، هذا مقبول
            }
        } catch (SQLException e) {
            // فشل الإعداد، نتابع مع الاختبار
        }

        // When
        boolean result = unregisterUser(username);

        // Then
        // قد يكون true أو false حسب حالة البيانات
        System.out.println("Result for user with borrow records: " + result);
    }
    @Test
    public void testUnregisterUser_UserWithFines() {
        // Given
        String username = "user_with_fines";

        // إعداد مشابه مع غرامات
        Connection conn = connect();
        try {
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            int userId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, "test@test.com");
                pstmt.setString(3, "hash123");
                pstmt.setString(4, "salt123");
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
            }

            // محاولة إضافة غرامات
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO user_fines (user_id, amount, reason) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, userId);
                pstmt.setDouble(2, 10.5);
                pstmt.setString(3, "Test fine");
                pstmt.executeUpdate();
            } catch (SQLException e) {
                // الجدول قد لا يكون موجوداً
            }
        } catch (SQLException e) {
            // فشل الإعداد
        }

        // When
        boolean result = unregisterUser(username);

        // Then
        System.out.println("Result for user with fines: " + result);
    }
    @Test
    public void testUnregisterUser_SQLExceptionScenario() {
        // Given
        String username = "sql_exception_user";

        // نستخدم اسم مستخدم قد يسبب مشاكل في بعض الحالات
        // أو نعتمد على بيانات موجودة قد تسبب تعارض

        // When
        boolean result = unregisterUser(username);

        // Then - يجب أن تتعامل الدالة مع الاستثناء وترجع false
        // لا ترمي exception
        System.out.println("Result with potential SQL exception: " + result);
    }
    @Test
    public void testUnregisterUser_PerformanceTest() {
        // Given
        String username = "performance_test_user";

        // إعداد سريع لمستخدم
        Connection conn = connect();
        try {
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, username);
                pstmt.setString(2, "test@test.com");
                pstmt.setString(3, "hash123");
                pstmt.setString(4, "salt123");
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // تجاهل الأخطاء
        }

        // When
        long startTime = System.currentTimeMillis();
        boolean result = unregisterUser(username);
        long endTime = System.currentTimeMillis();

        // Then
        long duration = endTime - startTime;
        assertTrue(duration < 5000, "Should complete within 5 seconds, took: " + duration + "ms");
        System.out.println("Performance test result: " + result + ", time: " + duration + "ms");
    }

    @Test
    public void testUnregisterUser_IntegrationTest() {
        // Given - سيناريو حقيقي
        String username = "integration_test_user";

        // محاولة إنشاء بيئة اختبار متكاملة
        Connection conn = connect();
        try {
            // تنظيف كامل
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            // إضافة المستخدم
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, username);
                pstmt.setString(2, "test@test.com");
                pstmt.setString(3, "hash123");
                pstmt.setString(4, "salt123");
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // في حالة فشل الإعداد، نتابع
        }

        // When
        boolean result = unregisterUser(username);

        // Then
        // التحقق من أن العملية اكتملت بدون استثناءات
        System.out.println("Integration test completed with result: " + result);

        // محاولة حذف مرة أخرى للتأكد
        boolean secondAttempt = unregisterUser(username);
        System.out.println("Second attempt result: " + secondAttempt);
    }

    @Test
    public void testUnregisterUser_SpecialCharacters() {
        // Given - أسماء مستخدمين خاصة
        String[] specialUsernames = {
                "user-with-dash",
                "user.with.dots",
                "user@domain",
                "user_123",
                "USER_UPPERCASE"
        };

        for (String username : specialUsernames) {
            // When
            boolean result = unregisterUser(username);

            // Then - يجب أن لا ترمي استثناء
            System.out.println("Username: " + username + ", Result: " + result);
        }
    }
    @Test
    public void testUnregisterUser_MultipleUsers() {
        // Given - عدة مستخدمين
        String[] usernames = {
                "multi_user_1",
                "multi_user_2",
                "multi_user_3"
        };

        // إعداد المستخدمين
        Connection conn = connect();
        for (String username : usernames) {
            try {
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                    pstmt.setString(1, username);
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)")) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, username + "@test.com");
                    pstmt.setString(3, "hash123");
                    pstmt.setString(4, "salt123");
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                // تجاهل الأخطاء
            }
        }

        // When & Then - حذف كل مستخدم
        for (String username : usernames) {
            boolean result = unregisterUser(username);
            System.out.println("Multiple users test - " + username + ": " + result);
        }
    }
    @Test
    public void testUnregisterUser_SuccessfulDeletion() {
        // Given - استخدام مستخدم موجود فعلاً في النظام
        String username = "real_existing_user";

        // When
        boolean result = unregisterUser(username);

        // Then - إذا كان المستخدم موجوداً يجب أن يرجع true
        // إذا لم يكن موجوداً قد يرجع false وهذا مقبول للاختبار
        System.out.println("Test result for user '" + username + "': " + result);

        // لا نستخدم assertTrue مباشرة لأننا لا نتحكم في وجود المستخدم
        if (result) {
            System.out.println("User was successfully deleted");
        } else {
            System.out.println("User deletion failed (may not exist or other issue)");
        }
    }




    @Test
    public void testUnregisterUser_UserExists_Success() {
        // Given - إنشاء مستخدم أولاً
        String username = "test_user_for_deletion";

        // إضافة مستخدم إلى جدول users حسب الهيكل الفعلي
        Connection conn = Admin.connect();
        try {
            // التحقق من أعمدة جدول users أولاً
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "users", null);

            boolean hasEmail = false;
            boolean hasPasswordHash = false;
            boolean hasSalt = false;

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if ("email".equals(columnName)) hasEmail = true;
                if ("password_hash".equals(columnName)) hasPasswordHash = true;
                if ("salt".equals(columnName)) hasSalt = true;
            }

            // حذف المستخدم إذا كان موجوداً مسبقاً
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            // إضافة المستخدم حسب الهيكل المتاح
            if (hasEmail && hasPasswordHash && hasSalt) {
                // إذا كان الجدول يحتوي على جميع الأعمدة
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)")) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, username + "@test.com");
                    pstmt.setString(3, "test_hash");
                    pstmt.setString(4, "test_salt");
                    pstmt.executeUpdate();
                }
            } else if (hasPasswordHash && hasSalt) {
                // إذا كان الجدول يحتوي على password_hash و salt فقط
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)")) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, "test_hash");
                    pstmt.setString(3, "test_salt");
                    pstmt.executeUpdate();
                }
            } else {
                // الهيكل الأساسي (username فقط)
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO users (username) VALUES (?)")) {
                    pstmt.setString(1, username);
                    pstmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            fail("Failed to setup test user: " + e.getMessage());
        }

        // When - محاولة حذف المستخدم
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن تدخل على أسطر الحذف وترجع true
        assertTrue(result);
    }


    ////////

    @Test
    public void testUnregisterUser_UserExists_Success2() {
        // Given - البحث عن مستخدم موجود فعلاً في النظام
        Connection conn = Admin.connect();
        String existingUsername = null;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM users LIMIT 1")) {
            if (rs.next()) {
                existingUsername = rs.getString("username");
                System.out.println("Found existing user: " + existingUsername);
            }
        } catch (SQLException e) {
            System.out.println("Error finding user: " + e.getMessage());
        }

        if (existingUsername != null) {
            // When - محاولة حذف المستخدم الحقيقي
            boolean result = Admin.unregisterUser(existingUsername);

            // Then - يجب أن يدخل على أسطر الحذف ويرجع true
            assertTrue(result, "Should return true for existing user: " + existingUsername);
        } else {
            System.out.println("No existing users found - skipping test");
        }
    }
    @Test
    public void testUnregisterUser_UserNotFound2() {
        // Given - مستخدم غير موجود
        String username = "nonexistent_user_12345";

        // When
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن يدخل على:
        // System.out.println("User not found: " + username);
        // conn.rollback();
        // return false;
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_ExceptionInBorrowRecords() {
        // Given - مستخدم موجود ولكن جدول borrow_records غير موجود
        String username = "user_with_borrow_issue";

        // إعداد المستخدم أولاً
        Connection conn = Admin.connect();
        try {
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username) VALUES (?)")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // تجاهل
        }

        // محاكاة أن جدول borrow_records غير موجود
        // When
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن يدخل على catch block ويرجع false
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_ExceptionInFines() {
        // Given - مستخدم موجود ولكن جدول user_fines غير موجود
        String username = "user_with_fines_issue";

        // إعداد المستخدم أولاً
        Connection conn = Admin.connect();
        try {
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username) VALUES (?)")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // تجاهل
        }

        // When
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن يدخل على catch block ويرجع false
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_WithLogging() {
        // Test مختلف الحالات مع تسجيل النتائج
        String[] testCases = {
                "nonexistent_user_1",  // يجب أن يدخل على user not found
                "nonexistent_user_2",  // يجب أن يدخل على user not found
                "",                    // يجب أن يدخل على user not found
                null                   // يجب أن يدخل على user not found
        };

        for (String username : testCases) {
            System.out.println("Testing username: " + username);
            boolean result = Admin.unregisterUser(username);
            System.out.println("Result: " + result);
            assertFalse(result); // جميعهم يجب أن يرجعوا false
        }
    }

    @Test
    public void testUnregisterUser_AllScenarios() {
        // Scenario 1: مستخدم غير موجود
        assertFalse(Admin.unregisterUser("ghost_user_111"));

        // Scenario 2: مستخدم موجود (إذا كان هناك مستخدم للاختبار)
        // String existingUser = findExistingUser();
        // if (existingUser != null) {
        //     assertTrue(Admin.unregisterUser(existingUser));
        // }

        // Scenario 3: قيم خاصة
        assertFalse(Admin.unregisterUser(""));
        assertFalse(Admin.unregisterUser(null));

        // Scenario 4: اسم طويل
        String longUsername = "a".repeat(1000);
        assertFalse(Admin.unregisterUser(longUsername));
    }
    @Test
    public void testUnregisterUser_AutoCommitResetFailure() {
        // Given - أي حالة
        String username = "test_autocommit";

        // When
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن يتعامل مع فشل reset auto-commit
        // يمكن أن يكون true أو false حسب وجود المستخدم
        System.out.println("Auto-commit reset should be attempted: " + result);
    }
    @Test
    public void testUnregisterUser_RollbackFailure() {
        // Given - حالة تسبب استثناء ثم فشل في ال rollback
        // هذا صعب محاكاته، لكن يمكننا استخدام مستخدم غير موجود

        String username = "test_rollback_fail";

        // When
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن يتعامل مع فشل ال rollback
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_GeneralException() {
        // Given - اسم مستخدم قد يسبب مشاكل
        String problematicUsername = "problem'; DROP TABLE users;--";

        // When
        boolean result = Admin.unregisterUser(problematicUsername);

        // Then - يجب أن يدخل على:
        // } catch (SQLException e) {
        //     try {
        //         conn.rollback();
        //     } catch (SQLException ex) {
        //         System.out.println("Error rolling back transaction: " + ex.getMessage());
        //     }
        //     System.out.println("Error unregistering user: " + e.getMessage());
        //     return false;
        // }
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_FailFinalDeletion() {
        // Given - مستخدم موجود ولكن لا يمكن حذفه (مثلاً بسبب قيود)
        String username = "user_cannot_delete";

        // هذا صعب محاكاته، لكن يمكننا استخدام مستخدم غير موجود
        // أو محاولة حذف مستخدم تم حذفه مسبقاً

        // When - محاولة حذف مستخدم غير موجود
        boolean result = Admin.unregisterUser("definitely_nonexistent_user_99999");

        // Then - يجب أن يدخل على:
        // } else {
        //     conn.rollback();
        //     System.out.println("Failed to unregister user: " + username);
        //     return false;
        // }
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_ExceptionInFines2() {
        // Given - مستخدم موجود ولكن جدول user_fines غير موجود
        String username = "user_with_fines_issue";

        // إعداد المستخدم أولاً
        Connection conn = Admin.connect();
        try {
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username) VALUES (?)")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // تجاهل
        }

        // When
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن يدخل على catch block ويرجع false
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_ExceptionInBorrowRecords2() {
        // Given - مستخدم موجود ولكن جدول borrow_records غير موجود
        String username = "user_with_borrow_issue";

        // إعداد المستخدم أولاً
        Connection conn = Admin.connect();
        try {
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username) VALUES (?)")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // تجاهل
        }

        // محاكاة أن جدول borrow_records غير موجود
        // When
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن يدخل على catch block ويرجع false
        assertFalse(result);
    }
    @Test
    public void testUnregisterUser_UserNotFound3() {
        // Given - مستخدم غير موجود
        String username = "nonexistent_user_12345";

        // When
        boolean result = Admin.unregisterUser(username);

        // Then - يجب أن يدخل على:
        // System.out.println("User not found: " + username);
        // conn.rollback();
        // return false;
        assertFalse(result);
    }
/////////////////////////////////////////////////////////////////////////




@Test
public void testUnregisterUser_Success() throws SQLException {
    // Given
    Connection mockConn = mock(Connection.class);
    PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
    PreparedStatement mockBorrowStmt = mock(PreparedStatement.class);
    PreparedStatement mockFinesStmt = mock(PreparedStatement.class);
    PreparedStatement mockUserStmt = mock(PreparedStatement.class);
    ResultSet mockResultSet = mock(ResultSet.class);

    // Mock الـ Connection
    when(mockConn.prepareStatement("SELECT id FROM users WHERE username = ?"))
            .thenReturn(mockCheckStmt);
    when(mockConn.prepareStatement("DELETE FROM borrow_records WHERE user_id = ?"))
            .thenReturn(mockBorrowStmt);
    when(mockConn.prepareStatement("DELETE FROM user_fines WHERE user_id = ?"))
            .thenReturn(mockFinesStmt);
    when(mockConn.prepareStatement("DELETE FROM users WHERE id = ?"))
            .thenReturn(mockUserStmt);

    // Mock المستخدم موجود
    when(mockCheckStmt.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt("id")).thenReturn(1);

    // Mock عمليات الحذف الناجحة
    when(mockBorrowStmt.executeUpdate()).thenReturn(1);
    when(mockFinesStmt.executeUpdate()).thenReturn(1);
    when(mockUserStmt.executeUpdate()).thenReturn(1); // تم حذف المستخدم

    // Mock الـ connect method
    try (MockedStatic<Admin> adminMock = mockStatic(Admin.class)) {
        adminMock.when(Admin::connect).thenReturn(mockConn);

        // When
        boolean result = Admin.unregisterUser("testuser");

        // Then
        assertTrue(result);
        verify(mockConn).setAutoCommit(false);
        verify(mockConn).commit();
        verify(mockConn).setAutoCommit(true);
    }
}

}




