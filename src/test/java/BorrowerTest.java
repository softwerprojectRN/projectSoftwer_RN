//package model;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//class BorrowerTest {
//
//    private Borrower borrower;
//
//    @BeforeEach
//    void setUp() {
//        borrower = new Borrower(10, "borrowUser", "hash123", "salt123");
//    }
//
//    @Test
//    void testConstructorAndInheritance() {
//        // Inherited from User
//        assertEquals(10, borrower.getId());
//        assertEquals("borrowUser", borrower.getUsername());
//        assertEquals("hash123", borrower.getPasswordHash());
//        assertEquals("salt123", borrower.getSalt());
//        assertFalse(borrower.isLoggedIn(), "Borrower should not be logged in initially");
//
//        // Borrower fields
//        assertEquals(0.0, borrower.getFineBalance());
//        assertTrue(borrower.getBorrowedMedia().isEmpty());
//    }
//
//    @Test
//    void testBorrowedMediaSetterGetter() {
//        MediaRecord mockRecord1 = mock(MediaRecord.class);
//        MediaRecord mockRecord2 = mock(MediaRecord.class);
//
//        List<MediaRecord> list = new ArrayList<>();
//        list.add(mockRecord1);
//        list.add(mockRecord2);
//
//        borrower.setBorrowedMedia(list);
//
//        List<MediaRecord> result = borrower.getBorrowedMedia();
//
//        assertEquals(2, result.size());
//        assertTrue(result.contains(mockRecord1));
//        assertTrue(result.contains(mockRecord2));
//
//        // Ensure getter returns a COPY (not the internal list)
//        result.add(mock(MediaRecord.class));
//        assertEquals(2, borrower.getBorrowedMedia().size(), "Internal list should remain unchanged");
//    }
//
//    @Test
//    void testFineBalanceSetterGetter() {
//        borrower.setFineBalance(15.75);
//        assertEquals(15.75, borrower.getFineBalance());
//    }
//
//    @Test
//    void testGetOverdueMedia() {
//        MediaRecord overdue1 = mock(MediaRecord.class);
//        MediaRecord overdue2 = mock(MediaRecord.class);
//        MediaRecord notOverdue = mock(MediaRecord.class);
//
//        when(overdue1.isOverdue()).thenReturn(true);
//        when(overdue2.isOverdue()).thenReturn(true);
//        when(notOverdue.isOverdue()).thenReturn(false);
//
//        List<MediaRecord> list = new ArrayList<>();
//        list.add(overdue1);
//        list.add(notOverdue);
//        list.add(overdue2);
//
//        borrower.setBorrowedMedia(list);
//
//        List<MediaRecord> overdue = borrower.getOverdueMedia();
//
//        assertEquals(2, overdue.size());
//        assertTrue(overdue.contains(overdue1));
//        assertTrue(overdue.contains(overdue2));
//    }
//
//    @Test
//    void testGetOverdueMediaWhenNoneAreOverdue() {
//        MediaRecord rec1 = mock(MediaRecord.class);
//        MediaRecord rec2 = mock(MediaRecord.class);
//
//        when(rec1.isOverdue()).thenReturn(false);
//        when(rec2.isOverdue()).thenReturn(false);
//
//        List<MediaRecord> list = new ArrayList<>();
//        list.add(rec1);
//        list.add(rec2);
//
//        borrower.setBorrowedMedia(list);
//
//        List<MediaRecord> overdue = borrower.getOverdueMedia();
//
//        assertTrue(overdue.isEmpty(), "No overdue media should return an empty list");
//    }
//}
