import domain.Book;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BookTest {

    @Test
    public void testBookConstructor() {
        // Test the constructor
        Book book = new Book(1, "Test Book", "Test Author", "1234567890", true);

        assertEquals(1, book.getId());
        assertEquals("Test Book", book.getTitle());
        assertEquals("Test Author", book.getAuthor());
        assertEquals("1234567890", book.getIsbn());
        assertTrue(book.isAvailable());
        assertEquals("book", book.getMediaType());
    }

    @Test
    public void testToString() {
        // Test the toString method
        Book book = new Book(1, "Test Book", "Test Author", "1234567890", true);
        String expected = "ID: 1, Title: 'Test Book', Type: book, Available: Yes, Author: 'Test Author', ISBN: 1234567890";
        assertEquals(expected, book.toString());
    }

    @Test
    public void testAddBookSuccess() throws SQLException {
        // Mock connection and prepared statements
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        PreparedStatement mockMediaStmt = mock(PreparedStatement.class);
        PreparedStatement mockBookStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            // Set up the prepared statements
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockCheckStmt);
            when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(mockMediaStmt);

            // Set up the check statement
            when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
            when(mockCheckRs.next()).thenReturn(false); // ISBN doesn't exist

            // Set up the media statement
            when(mockMediaStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
            when(mockGeneratedKeys.next()).thenReturn(true);
            when(mockGeneratedKeys.getInt(1)).thenReturn(1);

            // Set up the book statement
            when(mockConnection.prepareStatement(contains("INSERT INTO books"))).thenReturn(mockBookStmt);

            // Call the method
            Book result = Book.addBook("Test Book", "Test Author", "1234567890");

            // Verify
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Test Book", result.getTitle());
            assertEquals("Test Author", result.getAuthor());
            assertEquals("1234567890", result.getIsbn());
            assertTrue(result.isAvailable());

            // Verify the interactions
            verify(mockCheckStmt).setString(1, "1234567890");
            verify(mockCheckStmt).executeQuery();
            verify(mockMediaStmt).setString(1, "Test Book");
            verify(mockMediaStmt).executeUpdate();
            verify(mockMediaStmt).getGeneratedKeys();
            verify(mockBookStmt).setInt(1, 1);
            verify(mockBookStmt).setString(2, "Test Author");
            verify(mockBookStmt).setString(3, "1234567890");
            verify(mockBookStmt).executeUpdate();
        }
    }

    @Test
    public void testAddBookWithEmptyTitle() {
        // Call the method with empty title
        Book result = Book.addBook("", "Test Author", "1234567890");

        // Verify
        assertNull(result);
    }

    @Test
    public void testAddBookWithNullAuthor() {
        // Call the method with null author
        Book result = Book.addBook("Test Book", null, "1234567890");

        // Verify
        assertNull(result);
    }

    @Test
    public void testAddBookWithEmptyIsbn() {
        // Call the method with empty ISBN
        Book result = Book.addBook("Test Book", "Test Author", "");

        // Verify
        assertNull(result);
    }

    @Test
    public void testAddBookWithDuplicateIsbn() throws SQLException {
        // Mock connection and prepared statements
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockCheckStmt);
            when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
            when(mockCheckRs.next()).thenReturn(true); // ISBN exists

            // Call the method
            Book result = Book.addBook("Test Book", "Test Author", "1234567890");

            // Verify
            assertNull(result);

            // Verify the interactions
            verify(mockCheckStmt).setString(1, "1234567890");
            verify(mockCheckStmt).executeQuery();
        }
    }

    @Test
    public void testAddBookWithSQLExceptionInCheck() {
        // Mock connection and prepared statements
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockCheckStmt);

                // Create the exception outside the when() call
                SQLException exception = new SQLException("Test exception");
                when(mockCheckStmt.executeQuery()).thenThrow(exception);

                // Call the method
                Book result = Book.addBook("Test Book", "Test Author", "1234567890");

                // Verify
                assertNull(result);
            } catch (SQLException e) {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void testAddBookWithSQLExceptionInInsert() throws SQLException {
        // Mock connection and prepared statements
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        PreparedStatement mockMediaStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockCheckStmt);
            when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(mockMediaStmt);
            when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
            when(mockCheckRs.next()).thenReturn(false); // ISBN doesn't exist

            try {
                // Create the exception outside the when() call
                SQLException exception = new SQLException("Test exception");
                when(mockMediaStmt.executeUpdate()).thenThrow(exception);

                // Call the method
                Book result = Book.addBook("Test Book", "Test Author", "1234567890");

                // Verify
                assertNull(result);
            } catch (SQLException e) {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void testAddBookWithNoGeneratedKeys() throws SQLException {
        // Mock connection and prepared statements
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        PreparedStatement mockMediaStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockCheckStmt);
            when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(mockMediaStmt);
            when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
            when(mockCheckRs.next()).thenReturn(false); // ISBN doesn't exist
            when(mockMediaStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
            when(mockGeneratedKeys.next()).thenReturn(false);

            // Call the method
            Book result = Book.addBook("Test Book", "Test Author", "1234567890");

            // Verify
            assertNull(result);
        }
    }

    @Test
    public void testGetAllBooksSuccess() throws SQLException {
        // Mock connection, statement, and result set
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            // Mock the result set to return two rows
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(1, 2);
            when(mockResultSet.getString("title")).thenReturn("Book 1", "Book 2");
            when(mockResultSet.getString("author")).thenReturn("Author 1", "Author 2");
            when(mockResultSet.getString("isbn")).thenReturn("1234567890", "0987654321");
            when(mockResultSet.getInt("available")).thenReturn(1, 0);

            // Call the method
            List<Book> result = Book.getAllBooks();

            // Verify
            assertEquals(2, result.size());

            Book book1 = result.get(0);
            assertEquals(1, book1.getId());
            assertEquals("Book 1", book1.getTitle());
            assertEquals("Author 1", book1.getAuthor());
            assertEquals("1234567890", book1.getIsbn());
            assertTrue(book1.isAvailable());

            Book book2 = result.get(1);
            assertEquals(2, book2.getId());
            assertEquals("Book 2", book2.getTitle());
            assertEquals("Author 2", book2.getAuthor());
            assertEquals("0987654321", book2.getIsbn());
            assertFalse(book2.isAvailable());
        }
    }

    @Test
    public void testGetAllBooksWithSQLException() {
        // Mock connection and statement
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            try {
                when(mockConnection.createStatement()).thenReturn(mockStatement);

                // Create the exception outside the when() call
                SQLException exception = new SQLException("Test exception");
                when(mockStatement.executeQuery(anyString())).thenThrow(exception);

                // Call the method
                List<Book> result = Book.getAllBooks();

                // Verify
                assertEquals(0, result.size());
            } catch (SQLException e) {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void testInheritedMethods() throws SQLException {
        // Test inherited methods from Media class
        Book book = new Book(1, "Test Book", "Test Author", "1234567890", true);

        // Mock connection and prepared statement
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Test borrow method
            book.borrow();
            assertFalse(book.isAvailable());

            // Reset the mock for the next test
            reset(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Test returnMedia method
            book.returnMedia();
            assertTrue(book.isAvailable());

            // Reset the mock for the next test
            reset(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Test updateAvailability method
            book.updateAvailability(false);
            assertFalse(book.isAvailable());
        }
    }

        // ==================== اختبارات حالات الفشل (Validation Failures) ====================

        @Test
        public void testAddBook_WhenTitleIsNull_ShouldReturnNull() {
            // عندما يكون عنوان الكتاب (title) هو null
            Book result = Book.addBook(null, "Author Name", "1234567890");
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون العنوان فارغًا");
        }

        @Test
        public void testAddBook_WhenTitleIsEmpty_ShouldReturnNull() {
            // عندما يكون عنوان الكتاب (title) هو سلسلة نصية فارغة ""
            Book result = Book.addBook("", "Author Name", "1234567890");
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون العنوان سلسلة فارغة");
        }

        @Test
        public void testAddBook_WhenTitleIsWhitespace_ShouldReturnNull() {
            // عندما يكون عنوان الكتاب (title) يحتوي على مسافات فقط "   "
            Book result = Book.addBook("   ", "Author Name", "1234567890");
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون العنوان مسافات فقط");
        }

        @Test
        public void testAddBook_WhenAuthorIsNull_ShouldReturnNull() {
            // عندما يكون اسم المؤلف (author) هو null
            Book result = Book.addBook("Book Title", null, "1234567890");
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون اسم المؤلف فارغًا");
        }

        @Test
        public void testAddBook_WhenAuthorIsEmpty_ShouldReturnNull() {
            // عندما يكون اسم المؤلف (author) هو سلسلة نصية فارغة ""
            Book result = Book.addBook("Book Title", "", "1234567890");
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون اسم المؤلف سلسلة فارغة");
        }

        @Test
        public void testAddBook_WhenAuthorIsWhitespace_ShouldReturnNull() {
            // عندما يكون اسم المؤلف (author) يحتوي على مسافات فقط "   "
            Book result = Book.addBook("Book Title", "   ", "1234567890");
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون اسم المؤلف مسافات فقط");
        }

        @Test
        public void testAddBook_WhenIsbnIsNull_ShouldReturnNull() {
            // عندما يكون رقم ISBN (isbn) هو null
            Book result = Book.addBook("Book Title", "Author Name", null);
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون رقم ISBN فارغًا");
        }

        @Test
        public void testAddBook_WhenIsbnIsEmpty_ShouldReturnNull() {
            // عندما يكون رقم ISBN (isbn) هو سلسلة نصية فارغة ""
            Book result = Book.addBook("Book Title", "Author Name", "");
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون رقم ISBN سلسلة فارغة");
        }

        @Test
        public void testAddBook_WhenIsbnIsWhitespace_ShouldReturnNull() {
            // عندما يكون رقم ISBN (isbn) يحتوي على مسافات فقط "   "
            Book result = Book.addBook("Book Title", "Author Name", "   ");
            assertNull(result, "يجب أن ترجع الدالة null عندما يكون رقم ISBN مسافات فقط");
        }


        // ==================== اختبار حالة النجاح (Happy Path) ====================
        // هذا الاختبار يتأكد من أن الدالة تعمل بشكل صحيح عندما تكون جميع المدخلات صالحة

        @Test
        public void testAddBook_WithValidInputs_ShouldProceedToDatabaseOperations() throws Exception {
            // 1. تجهيز الـ Mocks لمحاكاة قاعدة البيانات
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
            PreparedStatement mockMediaStmt = mock(PreparedStatement.class);
            PreparedStatement mockBookStmt = mock(PreparedStatement.class);
            ResultSet mockCheckRs = mock(ResultSet.class);
            ResultSet mockGeneratedKeys = mock(ResultSet.class);

            // 2. استخدام MockedStatic لمحاكاة DriverManager
            try (MockedStatic<java.sql.DriverManager> mockedDriverManager = Mockito.mockStatic(java.sql.DriverManager.class)) {
                mockedDriverManager.when(() -> java.sql.DriverManager.getConnection(anyString())).thenReturn(mockConnection);

                // 3. تحديد سلوك الـ Mocks
                // محاكاة استعلام التحقق من ISBN (يجب أن لا يجد أي نتيجة)
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockCheckStmt);
                when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
                when(mockCheckRs.next()).thenReturn(false); // ISBN غير موجود

                // محاكاة إدخال في جدول media وإرجاع ID
                when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                        .thenReturn(mockMediaStmt);
                when(mockMediaStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
                when(mockGeneratedKeys.next()).thenReturn(true);
                when(mockGeneratedKeys.getInt(1)).thenReturn(1);

                // محاكاة إدخال في جدول books
                when(mockConnection.prepareStatement(contains("INSERT INTO books"))).thenReturn(mockBookStmt);

                // 4. تنفيذ الاختبار
                Book result = Book.addBook("Valid Title", "Valid Author", "1112223334");

                // 5. التحقق من النتائج
                assertNotNull(result, "عندما تكون المدخلات صالحة، يجب ألا ترجع الدالة null");
                assertEquals("Valid Title", result.getTitle());
                assertEquals("Valid Author", result.getAuthor());
                assertEquals("1112223334", result.getIsbn());
                assertTrue(result.isAvailable());
            }
        }

}