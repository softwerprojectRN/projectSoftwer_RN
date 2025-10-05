import domain.LibrarySystem;
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
        assertFalse(result, "Login should fail with wrong password.");
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
}