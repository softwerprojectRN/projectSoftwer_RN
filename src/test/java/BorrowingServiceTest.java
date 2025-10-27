import domain.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowingServiceTest {

    @Mock
    private Borrower borrower;
    @Mock
    private Media mediaBook;
    @Mock
    private Media mediaCD;
    @Mock
    private Borrower.MediaRecord mediaRecord;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mediaBook.getId()).thenReturn(1);
        when(mediaBook.getTitle()).thenReturn("Clean Code");
        when(mediaBook.getMediaType()).thenReturn("book");

        when(mediaCD.getId()).thenReturn(2);
        when(mediaCD.getTitle()).thenReturn("Best of Mozart");
        when(mediaCD.getMediaType()).thenReturn("cd");
    }

    // ==================== اختبارات دالة الاستعارة (borrowMedia) ====================

    @Test
    void testBorrowMedia_Success_Book() {
        when(borrower.isLoggedIn()).thenReturn(true);
        when(borrower.getFineBalance()).thenReturn(0.0);
        when(borrower.getOverdueMedia()).thenReturn(Collections.emptyList());
        when(mediaBook.isAvailable()).thenReturn(true);

        boolean result = BorrowingService.borrowMedia(borrower, mediaBook);

        assertTrue(result);
        verify(mediaBook).borrow();
        verify(borrower).addBorrowRecord(eq(mediaBook), any(LocalDate.class));
    }

    @Test
    void testBorrowMedia_Success_CD() {
        when(borrower.isLoggedIn()).thenReturn(true);
        when(borrower.getFineBalance()).thenReturn(0.0);
        when(borrower.getOverdueMedia()).thenReturn(Collections.emptyList());
        when(mediaCD.isAvailable()).thenReturn(true);

        boolean result = BorrowingService.borrowMedia(borrower, mediaCD);

        assertTrue(result);
        verify(mediaCD).borrow();
        verify(borrower).addBorrowRecord(eq(mediaCD), any(LocalDate.class));
    }

    @Test
    void testBorrowMedia_Fail_UserNotLoggedIn() {
        when(borrower.isLoggedIn()).thenReturn(false);

        boolean result = BorrowingService.borrowMedia(borrower, mediaBook);

        assertFalse(result);
        verify(mediaBook, never()).borrow();
        verify(borrower, never()).addBorrowRecord(any(), any());
    }

    @Test
    void testBorrowMedia_Fail_UserHasFines() {
        when(borrower.isLoggedIn()).thenReturn(true);
        when(borrower.getFineBalance()).thenReturn(50.0);

        boolean result = BorrowingService.borrowMedia(borrower, mediaBook);

        assertFalse(result);
        verify(mediaBook, never()).borrow();
    }

    @Test
    void testBorrowMedia_Fail_UserHasOverdueItems() {
        when(borrower.isLoggedIn()).thenReturn(true);
        when(borrower.getFineBalance()).thenReturn(0.0);
        when(borrower.getOverdueMedia()).thenReturn(List.of(mediaRecord));

        boolean result = BorrowingService.borrowMedia(borrower, mediaBook);

        assertFalse(result);
        verify(mediaBook, never()).borrow();
    }

    @Test
    void testBorrowMedia_Fail_MediaNotAvailable() {
        when(borrower.isLoggedIn()).thenReturn(true);
        when(borrower.getFineBalance()).thenReturn(0.0);
        when(borrower.getOverdueMedia()).thenReturn(Collections.emptyList());
        when(mediaBook.isAvailable()).thenReturn(false);

        boolean result = BorrowingService.borrowMedia(borrower, mediaBook);

        assertFalse(result);
        verify(mediaBook, never()).borrow();
    }

    // ==================== اختبارات دالة الإرجاع (returnMedia) ====================

    @Test
    void testReturnMedia_Success_OnTime() {
        when(mediaRecord.getMedia()).thenReturn(mediaBook);
        when(mediaRecord.isOverdue()).thenReturn(false);
        when(borrower.getBorrowedMedia()).thenReturn(List.of(mediaRecord));

        boolean result = BorrowingService.returnMedia(borrower, mediaBook);

        assertTrue(result);
        verify(mediaBook).returnMedia();
        verify(borrower).removeBorrowRecord(eq(mediaRecord), eq(0.0));
    }

    @Test
    void testReturnMedia_Success_OverdueBook() {
        long overdueDays = 5;
        double expectedFine = overdueDays * BorrowingService.getFinePerDay("book");

        when(mediaRecord.getMedia()).thenReturn(mediaBook);
        when(mediaRecord.isOverdue()).thenReturn(true);
        when(mediaRecord.getOverdueDays()).thenReturn(overdueDays);
        when(borrower.getBorrowedMedia()).thenReturn(List.of(mediaRecord));

        boolean result = BorrowingService.returnMedia(borrower, mediaBook);

        assertTrue(result);
        verify(mediaBook).returnMedia();
        verify(borrower).removeBorrowRecord(eq(mediaRecord), eq(expectedFine));
    }

    @Test
    void testReturnMedia_Success_OverdueCD() {
        long overdueDays = 3;
        double expectedFine = overdueDays * BorrowingService.getFinePerDay("cd");

        when(mediaRecord.getMedia()).thenReturn(mediaCD);
        when(mediaRecord.isOverdue()).thenReturn(true);
        when(mediaRecord.getOverdueDays()).thenReturn(overdueDays);
        when(borrower.getBorrowedMedia()).thenReturn(List.of(mediaRecord));

        boolean result = BorrowingService.returnMedia(borrower, mediaCD);

        assertTrue(result);
        verify(mediaCD).returnMedia();
        verify(borrower).removeBorrowRecord(eq(mediaRecord), eq(expectedFine));
    }

    @Test
    void testReturnMedia_Fail_MediaNotBorrowedByUser() {
        when(mediaRecord.getMedia()).thenReturn(mediaCD);
        when(borrower.getBorrowedMedia()).thenReturn(List.of(mediaRecord));

        boolean result = BorrowingService.returnMedia(borrower, mediaBook);

        assertFalse(result);
        verify(mediaBook, never()).returnMedia();
        // --- تم تصحيح هذا السطر ---
        verify(borrower, never()).removeBorrowRecord(any(), anyDouble());
    }

    @Test
    void testReturnMedia_Fail_UserHasNoBorrowedMedia() {
        when(borrower.getBorrowedMedia()).thenReturn(Collections.emptyList());

        boolean result = BorrowingService.returnMedia(borrower, mediaBook);

        assertFalse(result);
        verify(mediaBook, never()).returnMedia();
        // --- تم تصحيح هذا السطر ---
        verify(borrower, never()).removeBorrowRecord(any(), anyDouble());
    }

    // ==================== اختبارات دالة المساعدة getBorrowDays ====================

    @Test
    void testGetBorrowDays_ValidBook() {
        // Act
        int days = BorrowingService.getBorrowDays("book");

        // Assert
        assertEquals(28, days, "يجب أن تكون مدة استعارة الكتاب 28 يوماً");
    }

    @Test
    void testGetBorrowDays_ValidCD() {
        // Act
        int days = BorrowingService.getBorrowDays("cd");

        // Assert
        assertEquals(7, days, "يجب أن تكون مدة استعارة القرص المدمج 7 أيام");
    }

    @Test
    void testGetBorrowDays_InvalidMediaType() {
        // Act
        int days = BorrowingService.getBorrowDays("magazine");

        // Assert
        assertEquals(0, days, "يجب أن يعيد 0 لأن 'magazine' ليس نوعاً معرفاً");
    }

    @Test
    void testGetBorrowDays_NullMediaType() {
        // Act
        int days = BorrowingService.getBorrowDays(null);

        // Assert
        assertEquals(0, days, "يجب أن يعيد 0 عند تمرير null");
    }

    @Test
    void testGetBorrowDays_EmptyStringMediaType() {
        // Act
        int days = BorrowingService.getBorrowDays("");

        // Assert
        assertEquals(0, days, "يجب أن يعيد 0 عند تمرير سلسلة نصية فارغة");
    }


}