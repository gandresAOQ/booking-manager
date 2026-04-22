package com.library.application.services;

import com.booking.application.ports.IBookingService;
import com.booking.grpc.Booking.BookFormat;
import com.booking.grpc.Booking.BookLanguage;
import com.booking.grpc.Booking.BookingRequest;
import com.booking.grpc.Booking.BookingRequestId;
import com.booking.grpc.Booking.BookingRequestPaged;
import com.booking.grpc.Booking.BookingResponse;
import com.booking.infrastructure.contracts.IBooksRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class BookingServiceTest {

    @InjectMock
    IBooksRepository booksRepository;

    @Inject
    IBookingService bookingService;

    // ── getBook ──────────────────────────────────────────────────────────────

    @Test
    void getBook_returnsJsonPayloadFromRow() {
        BookingRequestId request = BookingRequestId.newBuilder()
                .setUuid("uuid-1").setId("id-1").build();
        JsonObject json = new JsonObject().put("id", "id-1").put("name", "Clean Code");
        Row row = mock(Row.class);
        when(row.toJson()).thenReturn(json);
        when(booksRepository.findById("id-1")).thenReturn(Uni.createFrom().item(row));

        BookingResponse result = bookingService.getBook(request).await().indefinitely();

        assertEquals(json.encode(), result.getPayload());
    }

    @Test
    void getBook_whenNotFound_throwsNotFoundStatus() {
        BookingRequestId request = BookingRequestId.newBuilder()
                .setUuid("uuid-1").setId("id-1").build();
        when(booksRepository.findById("id-1"))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException("Book not found with id: id-1")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> bookingService.getBook(request).await().indefinitely());

        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode());
        assertEquals("Book not found with id: id-1", ex.getStatus().getDescription());
    }

    @Test
    void getBook_whenUnexpectedError_throwsInternalStatus() {
        BookingRequestId request = BookingRequestId.newBuilder()
                .setUuid("uuid-1").setId("id-1").build();
        when(booksRepository.findById("id-1"))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("connection lost")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> bookingService.getBook(request).await().indefinitely());

        assertEquals(Status.INTERNAL.getCode(), ex.getStatus().getCode());
    }

    // ── getAllBooks ───────────────────────────────────────────────────────────

    @Test
    void getAllBooks_returnsEncodedJsonArrayPayload() {
        BookingRequestPaged request = BookingRequestPaged.newBuilder()
                .setUuid("uuid-1").setPage("1").build();
        JsonArray jsonArray = new JsonArray()
                .add(new JsonObject().put("id", "id-1"))
                .add(new JsonObject().put("id", "id-2"));
        when(booksRepository.findAll(1, 10)).thenReturn(Uni.createFrom().item(jsonArray));

        BookingResponse result = bookingService.getAllBooks(request).await().indefinitely();

        assertEquals(jsonArray.encode(), result.getPayload());
    }

    @Test
    void getAllBooks_whenNotFound_throwsNotFoundStatus() {
        BookingRequestPaged request = BookingRequestPaged.newBuilder()
                .setUuid("uuid-1").setPage("5").build();
        when(booksRepository.findAll(5, 10))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException("No books found on page: 5")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> bookingService.getAllBooks(request).await().indefinitely());

        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode());
        assertEquals("No books found on page: 5", ex.getStatus().getDescription());
    }

    @Test
    void getAllBooks_whenUnexpectedError_throwsInternalStatus() {
        BookingRequestPaged request = BookingRequestPaged.newBuilder()
                .setUuid("uuid-1").setPage("1").build();
        when(booksRepository.findAll(1, 10))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB timeout")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> bookingService.getAllBooks(request).await().indefinitely());

        assertEquals(Status.INTERNAL.getCode(), ex.getStatus().getCode());
    }

    // ── createBook ───────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void createBook_returnsBookCreatedPayload() {
        BookingRequest request = BookingRequest.newBuilder()
                .setUuid("uuid-1").setName("Clean Code").setAuthor("Robert C. Martin")
                .setPrice(29.99).setLanguage(BookLanguage.ENGLISH).setPages(431).setFormat(BookFormat.HARDCOVER)
                .build();
        when(booksRepository.save(request)).thenReturn(Uni.createFrom().item(mock(RowSet.class)));

        BookingResponse result = bookingService.createBook(request).await().indefinitely();

        assertEquals("Book Created", result.getPayload());
    }

    @Test
    void createBook_whenUnexpectedError_throwsInternalStatus() {
        BookingRequest request = BookingRequest.newBuilder()
                .setUuid("uuid-1").setName("Clean Code").setAuthor("Robert C. Martin")
                .setPrice(29.99).setLanguage(BookLanguage.ENGLISH).setPages(431).setFormat(BookFormat.HARDCOVER)
                .build();
        when(booksRepository.save(request))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("constraint violation")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> bookingService.createBook(request).await().indefinitely());

        assertEquals(Status.INTERNAL.getCode(), ex.getStatus().getCode());
    }

    // ── updateBook ───────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void updateBook_returnsBookUpdatedPayload() {
        BookingRequest request = BookingRequest.newBuilder()
                .setUuid("uuid-1").setId("id-1").setName("Clean Code 2nd Ed").setAuthor("Robert C. Martin")
                .setPrice(34.99).setLanguage(BookLanguage.SPANISH).setPages(450).setFormat(BookFormat.PAPERBACK)
                .build();
        when(booksRepository.update(request)).thenReturn(Uni.createFrom().item(mock(RowSet.class)));

        BookingResponse result = bookingService.updateBook(request).await().indefinitely();

        assertEquals("Book Updated", result.getPayload());
    }

    @Test
    void updateBook_whenUnexpectedError_throwsInternalStatus() {
        BookingRequest request = BookingRequest.newBuilder()
                .setUuid("uuid-1").setId("id-1").setName("Clean Code 2nd Ed").setAuthor("Robert C. Martin")
                .setPrice(34.99).setLanguage(BookLanguage.SPANISH).setPages(450).setFormat(BookFormat.PAPERBACK)
                .build();
        when(booksRepository.update(request))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("lock timeout")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> bookingService.updateBook(request).await().indefinitely());

        assertEquals(Status.INTERNAL.getCode(), ex.getStatus().getCode());
    }

    // ── deleteBook ───────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void deleteBook_returnsBookDeletedPayload() {
        BookingRequestId request = BookingRequestId.newBuilder()
                .setUuid("uuid-1").setId("id-1").build();
        when(booksRepository.deleteById("id-1")).thenReturn(Uni.createFrom().item(mock(RowSet.class)));

        BookingResponse result = bookingService.deleteBook(request).await().indefinitely();

        assertEquals("Book Deleted", result.getPayload());
    }

    @Test
    void deleteBook_whenUnexpectedError_throwsInternalStatus() {
        BookingRequestId request = BookingRequestId.newBuilder()
                .setUuid("uuid-1").setId("id-1").build();
        when(booksRepository.deleteById("id-1"))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("connection reset")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> bookingService.deleteBook(request).await().indefinitely());

        assertEquals(Status.INTERNAL.getCode(), ex.getStatus().getCode());
    }
}