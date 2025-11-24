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
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        borrowerService.generateOverdueReport(borrower);

        String result = output.toString().trim();

        assertTrue(result.contains("You have no overdue items."));
    }

}

