import domain.LibrarySystem;
import domain.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//ctrl +shift +T حتى يظهروا عندي

class LibrarySystemTest {

    private LibrarySystem system ;

    //الدالة setUp() بتنادى قبل كل اختبار (@Test).
    //لهدف: إنشاء نسخة جديدة من LibrarySystem حتى تكون الاختبارات مستقلة عن بعض.

    @BeforeEach
    void setUp() {
        system = new LibrarySystem();
    }


    //يعني أنا هون بدي أختبر إنو نجح في الدخول والمعلومات صحيحة
    @Test
    void testloginSuccess(){

        boolean result = system.login("rahaf","1234");
        assertTrue(result,"Login Success");


    }

    @Test
    void testloginFailusernamenotfound(){
        boolean result = system.login("raghad", "1234");

        assertFalse(result, "Login should fail with a non-existent username.");
        assertFalse(system.isLoggedIn(), "System should report that no user is logged in.");
    }
    @Test
    void testloginFailpasswnotfound(){
        boolean result = system.login("rahaf", "7896");

        assertFalse(result, "Login should fail with a non-existent username.");

        assertFalse(system.isLoggedIn(), "System should report that no user is logged in.");
    }

    @Test
    void testlogoutSuccess(){

        //هاي لازم بالأول أسجل الدخول بنجاح بعدها بعمل logout
        system.login("rahaf","1234");
        assertTrue(system.isLoggedIn(),"user should be logged in before logout");
        // بعدها سجل الخروج
        system.logout();
        assertFalse(system.isLoggedIn(),"not logout");
    }

    // فرضا كانت كلمة السر الي دخلها فارغة :
    @Test
    void testifnullpassword(){
        boolean result = system.login("rahaf",null);
        assertFalse(result,"Login should fail with a non-null password.");
        assertFalse(system.isLoggedIn());

    }

    @Test
    void testifnullusername(){
        boolean result = system.login(null,"1234");
        assertFalse(result,"Login should fail with a non-null username.");
        assertFalse(system.isLoggedIn());
    }
    @Test
    void testLoginFailureWithEmptyUsername() {
        boolean result = system.login("", "1234");

        assertFalse(result, "Login should fail with an empty username.");
        assertFalse(system.isLoggedIn(), "System should report that no user is logged in.");
    }

    @Test
    void testLoginFailureWithEmptyPassword() {

        boolean result = system.login("rahaf", "");

        assertFalse(result, "Login should fail with an empty password.");
        assertFalse(system.isLoggedIn(), "System should report that no user is logged in.");
    }
    @Test
    void testLoginFailureWithWhitespaceOnlyUsername() {
        boolean result = system.login("   ", "1234");

        assertFalse(result, "Login should fail with a whitespace-only username.");
        assertFalse(system.isLoggedIn());
    }
    @Test
    void testLoginFailureWithWhitespaceOnlyPassword() {
        boolean result = system.login("rahaf", "    ");
        assertFalse(result, "Login should fail with a whitespace-only password.");
        assertFalse(system.isLoggedIn());

    }
///////////////////////////////////////////////////////////////////////

    // نور هون رح تضيف test cases على عمليات الكتب

    @Test
    void testBookConstructorAndGetters() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884");

        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthor());
        assertEquals("9780132350884", book.getIsbn());
        assertTrue(book.isAvailable());
    }

    @Test
    void testSetAvailable() {
        Book book = new Book("Design Patterns", "GoF", "9780201633610");

        // Initially available
        assertTrue(book.isAvailable());

        // Change availability
        book.setAvailable(false);
        assertFalse(book.isAvailable());
    }

    @Test
    void testToString() {
        Book book = new Book("Refactoring", "Martin Fowler", "9780201485677");

        String expected = "Title: 'Refactoring', Author: 'Martin Fowler', ISBN: 9780201485677, Available: Yes";
        assertEquals(expected, book.toString());

        // After marking as unavailable
        book.setAvailable(false);
        String expectedUnavailable = "Title: 'Refactoring', Author: 'Martin Fowler', ISBN: 9780201485677, Available: No";
        assertEquals(expectedUnavailable, book.toString());
    }





}