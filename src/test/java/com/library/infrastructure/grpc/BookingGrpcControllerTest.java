package com.library.infrastructure.grpc;

import com.booking.application.ports.IBookingService;
import com.booking.grpc.Booking.BookFormat;
import com.booking.grpc.Booking.BookLanguage;
import com.booking.grpc.Booking.BookingRequest;
import com.booking.grpc.Booking.BookingRequestId;
import com.booking.grpc.Booking.BookingRequestPaged;
import com.booking.grpc.Booking.BookingResponse;
import com.booking.grpc.BookingServiceGrpc.BookingServiceBlockingStub;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BookingGrpcControllerTest {

    @GrpcClient("booking")
    BookingServiceBlockingStub bookingClient;

    @InjectMock
    IBookingService bookingService;

    @Test
    void get_delegatesRequestToServiceAndReturnsResponse() {
        BookingRequestId request = BookingRequestId.newBuilder()
                .setUuid("uuid-1")
                .setId("id-1")
                .build();
        BookingResponse expected = BookingResponse.newBuilder()
                .setPayload("{\"id\":\"id-1\",\"name\":\"Clean Code\"}")
                .build();
        when(bookingService.getBook(request)).thenReturn(Uni.createFrom().item(expected));

        BookingResponse result = bookingClient.get(request);

        assertEquals(expected.getPayload(), result.getPayload());
        verify(bookingService).getBook(request);
    }

    @Test
    void getAll_delegatesRequestToServiceAndReturnsPagedResponse() {
        BookingRequestPaged request = BookingRequestPaged.newBuilder()
                .setUuid("uuid-1")
                .setPage("1")
                .build();
        BookingResponse expected = BookingResponse.newBuilder()
                .setPayload("[{\"id\":\"id-1\"},{\"id\":\"id-2\"}]")
                .build();
        when(bookingService.getAllBooks(request)).thenReturn(Uni.createFrom().item(expected));

        BookingResponse result = bookingClient.getAll(request);

        assertEquals(expected.getPayload(), result.getPayload());
        verify(bookingService).getAllBooks(request);
    }

    @Test
    void post_delegatesRequestToServiceAndReturnsCreatedResponse() {
        BookingRequest request = BookingRequest.newBuilder()
                .setUuid("uuid-1")
                .setName("Clean Code")
                .setAuthor("Robert C. Martin")
                .setPrice(29.99)
                .setLanguage(BookLanguage.ENGLISH)
                .setPages(431)
                .setFormat(BookFormat.HARDCOVER)
                .build();
        BookingResponse expected = BookingResponse.newBuilder()
                .setPayload("Book Created")
                .build();
        when(bookingService.createBook(request)).thenReturn(Uni.createFrom().item(expected));

        BookingResponse result = bookingClient.post(request);

        assertEquals("Book Created", result.getPayload());
        verify(bookingService).createBook(request);
    }

    @Test
    void put_delegatesRequestToServiceAndReturnsUpdatedResponse() {
        BookingRequest request = BookingRequest.newBuilder()
                .setUuid("uuid-1")
                .setId("id-1")
                .setName("Clean Code Updated")
                .setAuthor("Robert C. Martin")
                .setPrice(34.99)
                .setLanguage(BookLanguage.SPANISH)
                .setPages(431)
                .setFormat(BookFormat.PAPERBACK)
                .build();
        BookingResponse expected = BookingResponse.newBuilder()
                .setPayload("Book Updated")
                .build();
        when(bookingService.updateBook(request)).thenReturn(Uni.createFrom().item(expected));

        BookingResponse result = bookingClient.put(request);

        assertEquals("Book Updated", result.getPayload());
        verify(bookingService).updateBook(request);
    }

    @Test
    void delete_delegatesRequestToServiceAndReturnsDeletedResponse() {
        BookingRequestId request = BookingRequestId.newBuilder()
                .setUuid("uuid-1")
                .setId("id-1")
                .build();
        BookingResponse expected = BookingResponse.newBuilder()
                .setPayload("Book Deleted")
                .build();
        when(bookingService.deleteBook(request)).thenReturn(Uni.createFrom().item(expected));

        BookingResponse result = bookingClient.delete(request);

        assertEquals("Book Deleted", result.getPayload());
        verify(bookingService).deleteBook(request);
    }

}
