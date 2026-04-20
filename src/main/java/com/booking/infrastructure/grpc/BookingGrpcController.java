package com.booking.infrastructure.grpc;

import com.booking.application.ports.IBookingService;
import io.quarkus.grpc.GrpcService;
import com.booking.grpc.MutinyBookingServiceGrpc.BookingServiceImplBase;
import com.booking.grpc.Booking.BookingRequestId;
import com.booking.grpc.Booking.BookingRequest;
import com.booking.grpc.Booking.BookingRequestPaged;
import com.booking.grpc.Booking.BookingResponse;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@GrpcService
public class BookingGrpcController extends BookingServiceImplBase {

    @Inject
    IBookingService bookingService;

    @Override
    public Uni<BookingResponse> get(BookingRequestId request) {
        return bookingService.getBook(request);
    }

    @Override
    public Uni<BookingResponse> getAll(BookingRequestPaged request) {
        return bookingService.getAllBooks(request);
    }

    @Override
    public Uni<BookingResponse> post(BookingRequest request) {
        return bookingService.createBook(request);
    }

    @Override
    public Uni<BookingResponse> put(BookingRequest request) {
        return bookingService.updateBook(request);
    }

    @Override
    public Uni<BookingResponse> delete(BookingRequestId request) {
        return bookingService.deleteBook(request);
    }
}
