package com.booking.application.ports;

import com.booking.grpc.Booking;
import io.smallrye.mutiny.Uni;

public interface IBookingService {

    Uni<Booking.BookingResponse> getBook(Booking.BookingRequestId request);
    Uni<Booking.BookingResponse> getAllBooks(Booking.BookingRequestPaged request);
    Uni<Booking.BookingResponse> createBook(Booking.BookingRequest request);
    Uni<Booking.BookingResponse> updateBook(Booking.BookingRequest request);
    Uni<Booking.BookingResponse> deleteBook(Booking.BookingRequestId request);

}
