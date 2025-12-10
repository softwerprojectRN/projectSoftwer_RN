//import model.Borrower;
//import model.Media;
//import model.MediaRecord;
//import model.UserWithOverdueBooks;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import service.BorrowingService;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class BorrowingServiceTest {
//
//    private BorrowingService borrowingService;
//    private Borrower borrower;
//    private Media media;
//
//    // Mock DAOs
//    private dao.BorrowRecordDAO borrowRecordDAOMock;
//    private dao.MediaDAO mediaDAOMock;
//    private dao.FineDAO fineDAOMock;
//
//    @BeforeEach
//    void setup() throws Exception {
//        borrowingService = new BorrowingService();
//
//        // Mock DAOs
//        borrowRecordDAOMock = mock(dao.BorrowRecordDAO.class);
//        mediaDAOMock = mock(dao.MediaDAO.class);
//        fineDAOMock = mock(dao.FineDAO.class);
//
//        // Inject mocks via reflection
//        java.lang.reflect.Field borrowField = BorrowingService.class.getDeclaredField("borrowRecordDAO");
//        borrowField.setAccessible(true);
//        borrowField.set(borrowingService, borrowRecordDAOMock);
//
//        java.lang.reflect.Field mediaField = BorrowingService.class.getDeclaredField("mediaDAO");
//        mediaField.setAccessible(true);
//        mediaField.set(borrowingService, mediaDAOMock);
//
//        java.lang.reflect.Field fineField = BorrowingService.class.getDeclaredField("fineDAO");
//        fineField.setAccessible(true);
//        fineField.set(borrowingService, fineDAOMock);
//
//        // Create default borrower & media
//        borrower = mock(Borrower.class);
//        media = mock(Media.class);
//    }
//
//    // -------------------------------------------------------------------------
//    // borrowMedia() tests
//    // -------------------------------------------------------------------------
//
//    @Test
//    void testBorrowMedia_Success() {
//        when(borrower.isLoggedIn()).thenReturn(true);
//        when(borrower.getFineBalance()).thenReturn(0.0);
//        when(borrower.getOverdueMedia()).thenReturn(new ArrayList<>());
//        when(media.isAvailable()).thenReturn(true);
//        when(media.getMediaType()).thenReturn("book");
//        when(media.getTitle()).thenReturn("Java Programming");
//        when(media.getId()).thenReturn(1);
//        when(borrower.getBorrowedMedia()).thenReturn(new ArrayList<>());
//        when(borrowRecordDAOMock.insert(anyInt(), anyInt(), anyString(), anyString(), any(), any())).thenReturn(100);
//
//        boolean result = borrowingService.borrowMedia(borrower, media);
//
//        assertTrue(result);
//        verify(mediaDAOMock).updateAvailability(1, false);
//        verify(borrowRecordDAOMock).insert(anyInt(), anyInt(), anyString(), anyString(), any(), any());
//    }
//
//    @Test
//    void testBorrowMedia_Fails_NotLoggedIn() {
//        when(borrower.isLoggedIn()).thenReturn(false);
//
//        boolean result = borrowingService.borrowMedia(borrower, media);
//        assertFalse(result);
//    }
//
//    @Test
//    void testBorrowMedia_Fails_FineBalance() {
//        when(borrower.isLoggedIn()).thenReturn(true);
//        when(borrower.getFineBalance()).thenReturn(50.0);
//
//        boolean result = borrowingService.borrowMedia(borrower, media);
//        assertFalse(result);
//    }
//
//    @Test
//    void testBorrowMedia_Fails_OverdueItems() {
//        when(borrower.isLoggedIn()).thenReturn(true);
//        when(borrower.getFineBalance()).thenReturn(0.0);
//        List<MediaRecord> overdueList = new ArrayList<>();
//        overdueList.add(mock(MediaRecord.class));
//        when(borrower.getOverdueMedia()).thenReturn(overdueList);
//
//        boolean result = borrowingService.borrowMedia(borrower, media);
//        assertFalse(result);
//    }
//
//    @Test
//    void testBorrowMedia_Fails_MediaNotAvailable() {
//        when(borrower.isLoggedIn()).thenReturn(true);
//        when(borrower.getFineBalance()).thenReturn(0.0);
//        when(borrower.getOverdueMedia()).thenReturn(new ArrayList<>());
//        when(media.isAvailable()).thenReturn(false);
//
//        boolean result = borrowingService.borrowMedia(borrower, media);
//        assertFalse(result);
//    }
//
//    // -------------------------------------------------------------------------
//    // returnMedia() tests
//    // -------------------------------------------------------------------------
//
//    @Test
//    void testReturnMedia_Success_NoFine() {
//        MediaRecord record = mock(MediaRecord.class);
//        when(record.getMedia()).thenReturn(media);
//        when(record.isOverdue()).thenReturn(false);
//        when(media.getId()).thenReturn(1);
//        when(media.getMediaType()).thenReturn("book");
//        when(borrower.getBorrowedMedia()).thenReturn(new ArrayList<>(List.of(record)));
//        when(borrower.getFineBalance()).thenReturn(0.0);
//
//        boolean result = borrowingService.returnMedia(borrower, media);
//
//        assertTrue(result);
//        verify(mediaDAOMock).updateAvailability(1, true);
//        verify(borrowRecordDAOMock).markAsReturned(anyInt(), any(), anyDouble());
//    }
//
//    @Test
//    void testReturnMedia_Success_WithFine() {
//        MediaRecord record = mock(MediaRecord.class);
//        when(record.getMedia()).thenReturn(media);
//        when(record.isOverdue()).thenReturn(true);
//        when(record.getOverdueDays()).thenReturn(2L);
//        when(record.getRecordId()).thenReturn(100);
//        when(media.getId()).thenReturn(1);
//        when(media.getMediaType()).thenReturn("book");
//        when(media.getTitle()).thenReturn("Test Book");
//        when(borrower.getId()).thenReturn(0);
//        when(borrower.getBorrowedMedia()).thenReturn(new ArrayList<>(List.of(record)));
//        when(borrower.getFineBalance()).thenReturn(0.0);
//        when(fineDAOMock.getFineBalance(0)).thenReturn(20.0);
//
//        boolean result = borrowingService.returnMedia(borrower, media);
//
//        assertTrue(result);
//        verify(mediaDAOMock).updateAvailability(1, true);
//        verify(borrowRecordDAOMock).markAsReturned(anyInt(), any(), eq(20.0)); // 2 * 10.0
//        verify(fineDAOMock).addFine(0, 20.0);
//        verify(fineDAOMock).getFineBalance(0);
//    }
//
//    @Test
//    void testReturnMedia_Fails_NotBorrowed() {
//        when(borrower.getBorrowedMedia()).thenReturn(new ArrayList<>());
//
//        boolean result = borrowingService.returnMedia(borrower, media);
//        assertFalse(result);
//    }
//
//    // -------------------------------------------------------------------------
//    // getUsersWithOverdueBooks()
//    // -------------------------------------------------------------------------
//
//    @Test
//    void testGetUsersWithOverdueBooks() {
//        List<UserWithOverdueBooks> mockList = List.of(new UserWithOverdueBooks(1, "rahaf", 2));
//        when(borrowRecordDAOMock.getUsersWithOverdueBooks()).thenReturn(mockList);
//
//        List<UserWithOverdueBooks> result = borrowingService.getUsersWithOverdueBooks();
//        assertEquals(1, result.size());
//        assertEquals("rahaf", result.get(0).getUsername());
//    }
//
//    // -------------------------------------------------------------------------
//    // static helper methods
//    // -------------------------------------------------------------------------
//
//    @Test
//    void testGetBorrowDays() {
//        assertEquals(28, BorrowingService.getBorrowDays("book"));
//        assertEquals(7, BorrowingService.getBorrowDays("cd"));
//        assertEquals(0, BorrowingService.getBorrowDays("unknown"));
//    }
//
//    @Test
//    void testGetFinePerDay() {
//        assertEquals(10.0, BorrowingService.getFinePerDay("book"));
//        assertEquals(20.0, BorrowingService.getFinePerDay("cd"));
//        assertEquals(0.0, BorrowingService.getFinePerDay("unknown"));
//    }
//}
