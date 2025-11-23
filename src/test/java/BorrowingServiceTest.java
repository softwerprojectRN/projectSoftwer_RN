import service.*;

import model.*;

import dao.BorrowRecordDAO;
import dao.FineDAO;
import model.Borrower;
import model.Media;
import model.MediaRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BorrowingServiceTest {

    private BorrowerService borrowerService;
    private BorrowRecordDAO borrowRecordDAO;
    private FineDAO fineDAO;

    private Borrower borrower;

    @BeforeEach
    void setup() throws Exception {
        borrowerService = new BorrowerService();

        // Mock DAOs
        borrowRecordDAO = mock(BorrowRecordDAO.class);
        fineDAO = mock(FineDAO.class);

        // Inject mocks via reflection
        Field borrowField = BorrowerService.class.getDeclaredField("borrowRecordDAO");
        borrowField.setAccessible(true);
        borrowField.set(borrowerService, borrowRecordDAO);

        Field fineField = BorrowerService.class.getDeclaredField("fineDAO");
        fineField.setAccessible(true);
        fineField.set(borrowerService, fineDAO);

        borrower = new Borrower(1, "rahaf", "pass", "salt");
    }

    // --------------------------------------------------------------------------

    @Test
    void testLoadBorrowerData() {
        Media media = new Media(10, "Book A", true, "book");

        MediaRecord record = mock(MediaRecord.class);
        when(record.getMedia()).thenReturn(media);

        when(borrowRecordDAO.findActiveByUserId(1))
                .thenReturn(Collections.singletonList(record));

        when(fineDAO.getFineBalance(1)).thenReturn(5.0);

        borrowerService.loadBorrowerData(borrower);

        assertEquals(1, borrower.getBorrowedMedia().size());
        assertEquals(5.0, borrower.getFineBalance());
    }

    // --------------------------------------------------------------------------

    @Test
    void testPayFine_Success() {
        borrower.setFineBalance(10.0);

        when(fineDAO.payFine(1, 5.0)).thenReturn(true);

        boolean result = borrowerService.payFine(borrower, 5.0);

        assertTrue(result);
        assertEquals(5.0, borrower.getFineBalance());
    }

    @Test
    void testPayFine_InvalidAmount() {
        borrower.setFineBalance(10.0);

        boolean result = borrowerService.payFine(borrower, 0);

        assertFalse(result);
        assertEquals(10.0, borrower.getFineBalance());
    }

    @Test
    void testPayFine_TooMuch() {
        borrower.setFineBalance(10.0);

        boolean result = borrowerService.payFine(borrower, 20.0);

        assertFalse(result);
        assertEquals(10.0, borrower.getFineBalance());
    }

    // --------------------------------------------------------------------------

    @Test
    void testGenerateOverdueReport_Empty() {
        borrower.setBorrowedMedia(Collections.emptyList());

        borrowerService.generateOverdueReport(borrower);

        // No crash = success
    }

    @Test
    void testGenerateOverdueReport_WithItems() {

        Media book = new Media(1, "Book A", true, "book");

        MediaRecord record = mock(MediaRecord.class);
        when(record.isOverdue()).thenReturn(true);
        when(record.getMedia()).thenReturn(book);
        when(record.getOverdueDays()).thenReturn(3L);

        borrower.setBorrowedMedia(Collections.singletonList(record));

        borrowerService.generateOverdueReport(borrower);

        // We only check no exception — printing is allowed.
    }

    // --------------------------------------------------------------------------

    @Test
    void testDisplayBorrowedMedia_NoItems() {
        borrower.setBorrowedMedia(Collections.emptyList());

        borrowerService.displayBorrowedMedia(borrower);

        // No crash = OK
    }

    @Test
    void testDisplayBorrowedMedia_WithItems() {

        Media media = new Media(1, "CD Music", true, "cd");

        MediaRecord record = mock(MediaRecord.class);
        when(record.getMedia()).thenReturn(media);
        when(record.getDueDate()).thenReturn(LocalDate.now());
        when(record.isOverdue()).thenReturn(false);

        borrower.setBorrowedMedia(Collections.singletonList(record));
        borrowerService.displayBorrowedMedia(borrower);
    }
}
