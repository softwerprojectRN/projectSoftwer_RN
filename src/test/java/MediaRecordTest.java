//import model.*;
//
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MediaRecordTest {
//
//    @Test
//    void testConstructorAndGetters() {
//        Media media = new Media(1, "Book Title", true, "book");
//        LocalDate dueDate = LocalDate.now().plusDays(5);
//
//        MediaRecord record = new MediaRecord(100, media, dueDate);
//
//        assertEquals(100, record.getRecordId());
//        assertEquals(media, record.getMedia());
//        assertEquals(dueDate, record.getDueDate());
//    }
//
//    @Test
//    void testSetRecordIdAndDueDate() {
//        Media media = new Media(1, "Book Title", true, "book");
//        LocalDate dueDate = LocalDate.now().plusDays(3);
//
//        MediaRecord record = new MediaRecord(50, media, dueDate);
//
//        record.setRecordId(99);
//        record.setDueDate(dueDate.plusDays(2));
//
//        assertEquals(99, record.getRecordId());
//        assertEquals(dueDate.plusDays(2), record.getDueDate());
//    }
//
//    @Test
//    void testIsOverdue_FutureDueDate() {
//        Media media = new Media(1, "Book Title", true, "book");
//        LocalDate dueDate = LocalDate.now().plusDays(2);
//        MediaRecord record = new MediaRecord(1, media, dueDate);
//
//        assertFalse(record.isOverdue());
//        assertEquals(0, record.getOverdueDays());
//    }
//
//    @Test
//    void testIsOverdue_PastDueDate() {
//        Media media = new Media(1, "Book Title", true, "book");
//        LocalDate dueDate = LocalDate.now().minusDays(5);
//        MediaRecord record = new MediaRecord(1, media, dueDate);
//
//        assertTrue(record.isOverdue());
//        assertEquals(5, record.getOverdueDays());
//    }
//
//    @Test
//    void testIsOverdue_TodayDueDate() {
//        Media media = new Media(1, "Book Title", true, "book");
//        LocalDate dueDate = LocalDate.now();
//        MediaRecord record = new MediaRecord(1, media, dueDate);
//
//        assertFalse(record.isOverdue());
//        assertEquals(0, record.getOverdueDays());
//    }
//}
