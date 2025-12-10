import dao.BorrowRecordDAO;
import dao.FineDAO;
import model.Borrower;
import model.Media;
import model.MediaRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowerServiceTest {

    private BorrowRecordDAO borrowRecordDAOMock;
    private FineDAO fineDAOMock;
    private BorrowerService borrowerService;
    private Borrower borrower;

    @BeforeEach
    void setUp() throws Exception {
        borrowRecordDAOMock = mock(BorrowRecordDAO.class);
        fineDAOMock = mock(FineDAO.class);

        borrowerService = new BorrowerService();
        injectField(borrowerService, "borrowRecordDAO", borrowRecordDAOMock);
        injectField(borrowerService, "fineDAO", fineDAOMock);

        borrower = new Borrower(1, "rahaf", "hash", "salt");
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // -----------------------------------------------------------
    // TEST 1: loadBorrowerData loads media & fine correctly
    // -----------------------------------------------------------
    @Test
    void testLoadBorrowerData() {
        Media media = new Media(10, "Book Title", true, "book");
        MediaRecord r = new MediaRecord(1, media, LocalDate.now().plusDays(5));

        when(borrowRecordDAOMock.findActiveByUserId(1))
                .thenReturn(List.of(r));

        when(fineDAOMock.getFineBalance(1))
                .thenReturn(7.5);

        borrowerService.loadBorrowerData(borrower);

        // Validate borrowed media loaded
        assertEquals(1, borrower.getBorrowedMedia().size());
        assertEquals(r, borrower.getBorrowedMedia().get(0));

        // Validate fine balance loaded
        assertEquals(7.5, borrower.getFineBalance());
    }

    // -----------------------------------------------------------
    // TEST 2: payFine returns false if invalid amount
    // -----------------------------------------------------------
    @Test
    void testPayFine_InvalidAmount() {
        borrower.setFineBalance(10);

        assertFalse(borrowerService.payFine(borrower, -5));  // negative
        assertFalse(borrowerService.payFine(borrower, 0));   // zero
        assertFalse(borrowerService.payFine(borrower, 50));  // more than balance

        verify(fineDAOMock, never()).payFine(anyInt(), anyDouble());
    }

    // -----------------------------------------------------------
    // TEST 3: payFine success
    // -----------------------------------------------------------
    @Test
    void testPayFine_Success() {
        borrower.setFineBalance(20);

        when(fineDAOMock.payFine(1, 10.0)).thenReturn(true);

        boolean result = borrowerService.payFine(borrower, 10.0);

        assertTrue(result);
        assertEquals(10.0, borrower.getFineBalance());  // updated balance
        verify(fineDAOMock).payFine(1, 10.0);
    }

    // -----------------------------------------------------------
    // TEST 4: payFine fails because DAO returns false
    // -----------------------------------------------------------
    @Test
    void testPayFine_DAOFails() {
        borrower.setFineBalance(20);

        when(fineDAOMock.payFine(1, 10.0))
                .thenReturn(false);

        boolean result = borrowerService.payFine(borrower, 10.0);

        assertFalse(result);
        assertEquals(20, borrower.getFineBalance()); // unchanged
    }

    @Test
    void testGenerateOverdueReport_NoOverdueItems() {
        Borrower borrower = mock(Borrower.class);
        when(borrower.getOverdueMedia()).thenReturn(List.of());

        // Capture system output
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        try {
            borrowerService.generateOverdueReport(borrower);

            String result = output.toString().trim();

            assertTrue(result.contains("You have no overdue items."));
        } finally {
            // Restore original System.out
            System.setOut(originalOut);
        }
    }

    // ---------------------------
    // Test displayBorrowedMedia with empty list
    // ---------------------------
    @Test
    void testDisplayBorrowedMedia_Empty() {
        Borrower borrower = mock(Borrower.class);
        when(borrower.getBorrowedMedia()).thenReturn(List.of());

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        try {
            BorrowerService service = new BorrowerService();
            service.displayBorrowedMedia(borrower);

            String result = output.toString();
            assertTrue(result.contains("You have no borrowed items."));
        } finally {
            System.setOut(originalOut);
        }
    }

    // ---------------------------
    // Test displayBorrowedMedia with items (overdue & not overdue)
    // ---------------------------
    @Test
    void testDisplayBorrowedMedia_WithItems() {
        Media media1 = new Media(1, "Book A", false, "book");
        Media media2 = new Media(2, "CD B", false, "cd");

        MediaRecord record1 = mock(MediaRecord.class);
        when(record1.getMedia()).thenReturn(media1);
        when(record1.getDueDate()).thenReturn(LocalDate.now().minusDays(2));
        when(record1.isOverdue()).thenReturn(true);
        when(record1.getOverdueDays()).thenReturn(2L);

        MediaRecord record2 = mock(MediaRecord.class);
        when(record2.getMedia()).thenReturn(media2);
        when(record2.getDueDate()).thenReturn(LocalDate.now().plusDays(3));
        when(record2.isOverdue()).thenReturn(false);

        Borrower borrower = mock(Borrower.class);
        when(borrower.getBorrowedMedia()).thenReturn(List.of(record1, record2));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        try {
            BorrowerService service = new BorrowerService();
            service.displayBorrowedMedia(borrower);

            String outStr = output.toString();
            assertTrue(outStr.contains("Book A"));
            assertTrue(outStr.contains("CD B"));
            assertTrue(outStr.contains("OVERDUE by 2 days"));
        } finally {
            System.setOut(originalOut);
        }
    }

    // ---------------------------
    // Test generateOverdueReport with multiple overdue items
    // ---------------------------
    @Test
    void testGenerateOverdueReport_WithItems() {
        Media media1 = mock(Media.class);
        when(media1.getTitle()).thenReturn("Book A");
        when(media1.getMediaType()).thenReturn("book");

        Media media2 = mock(Media.class);
        when(media2.getTitle()).thenReturn("CD B");
        when(media2.getMediaType()).thenReturn("cd");

        MediaRecord record1 = mock(MediaRecord.class);
        when(record1.getMedia()).thenReturn(media1);
        when(record1.getOverdueDays()).thenReturn(3L);

        MediaRecord record2 = mock(MediaRecord.class);
        when(record2.getMedia()).thenReturn(media2);
        when(record2.getOverdueDays()).thenReturn(2L);

        Borrower borrower = mock(Borrower.class);
        when(borrower.getOverdueMedia()).thenReturn(List.of(record1, record2));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        try {
            BorrowerService service = new BorrowerService() {
                // Mock fine per day calculation
                public static double getFinePerDay(String mediaType) {
                    return mediaType.equals("book") ? 1.5 : 2.0;
                }
            };

            service.generateOverdueReport(borrower);

            String outStr = output.toString();
            assertTrue(outStr.contains("Book A"));
            assertTrue(outStr.contains("CD B"));
            assertTrue(outStr.contains("Total Overdue Fines"));
        } finally {
            System.setOut(originalOut);
        }
    }

    // ---------------------------
    // Test loadBorrowerData with empty borrowed media and zero fine
    // ---------------------------
    @Test
    void testLoadBorrowerData_Empty() throws Exception {
        BorrowRecordDAO borrowRecordDAOMock = mock(BorrowRecordDAO.class);
        FineDAO fineDAOMock = mock(FineDAO.class);

        BorrowerService service = new BorrowerService();
        injectField(service, "borrowRecordDAO", borrowRecordDAOMock);
        injectField(service, "fineDAO", fineDAOMock);

        Borrower borrower = new Borrower(1, "test", "hash", "salt");
        when(borrowRecordDAOMock.findActiveByUserId(1)).thenReturn(List.of());
        when(fineDAOMock.getFineBalance(1)).thenReturn(0.0);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        try {
            service.loadBorrowerData(borrower);

            assertEquals(0, borrower.getBorrowedMedia().size());
            assertEquals(0.0, borrower.getFineBalance());
            assertTrue(output.toString().contains("Loaded 0 borrowed items"));
        } finally {
            System.setOut(originalOut);
        }
    }


}

