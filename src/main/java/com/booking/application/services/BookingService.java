package com.booking.application.services;

import com.booking.application.ports.IBookingService;
import com.booking.grpc.Booking;
import com.booking.infrastructure.contracts.IBooksRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.NoSuchElementException;

@ApplicationScoped
public class BookingService implements IBookingService {

    private static final String CREATED_COMMENT = "Book Created";
    private static final String UPDATED_COMMENT = "Book Updated";
    private static final String DELETED_COMMENT = "Book Deleted";

    @Inject
    IBooksRepository booksRepository;

    @Override
    public Uni<Booking.BookingResponse> getBook(Booking.BookingRequestId request) {
        return booksRepository.findById(request.getId())
                .onItem().transform(row -> Booking.BookingResponse.newBuilder()
                        .setPayload(row.toJson().encode())
                        .build())
                .onFailure(NoSuchElementException.class).transform(throwable ->
                        new StatusRuntimeException(Status.NOT_FOUND.withDescription(throwable.getMessage())))
                .onFailure(t -> !(t instanceof StatusRuntimeException)).transform(throwable ->
                        new StatusRuntimeException(Status.INTERNAL.withDescription(throwable.getMessage())));
    }

    @Override
    public Uni<Booking.BookingResponse> getAllBooks(Booking.BookingRequestPaged request) {
        return booksRepository.findAll(Integer.parseInt(request.getPage()), 10)
                .onItem().transform(res -> Booking.BookingResponse.newBuilder()
                        .setPayload(res.encode())
                        .build())
                .onFailure(NoSuchElementException.class).transform(throwable ->
                        new StatusRuntimeException(Status.NOT_FOUND.withDescription(throwable.getMessage())))
                .onFailure(t -> !(t instanceof StatusRuntimeException)).transform(throwable ->
                        new StatusRuntimeException(Status.INTERNAL.withDescription(throwable.getMessage())));
    }

    @Override
    public Uni<Booking.BookingResponse> createBook(Booking.BookingRequest request) {
        return booksRepository.save(request)
                .onItem().transform(res -> Booking.BookingResponse.newBuilder()
                        .setPayload(CREATED_COMMENT)
                        .build())
                .onFailure().transform(throwable ->
                        new StatusRuntimeException(Status.INTERNAL.withDescription(throwable.getMessage())));
    }

    @Override
    public Uni<Booking.BookingResponse> updateBook(Booking.BookingRequest request) {
        return booksRepository.update(request)
                .onItem().transform(res -> Booking.BookingResponse.newBuilder()
                        .setPayload(UPDATED_COMMENT)
                        .build())
                .onFailure().transform(throwable ->
                        new StatusRuntimeException(Status.INTERNAL.withDescription(throwable.getMessage())));
    }

    @Override
    public Uni<Booking.BookingResponse> deleteBook(Booking.BookingRequestId request) {
        return booksRepository.deleteById(request.getId())
                .onItem().transform(res -> Booking.BookingResponse.newBuilder()
                        .setPayload(DELETED_COMMENT)
                        .build())
                .onFailure().transform(throwable ->
                        new StatusRuntimeException(Status.INTERNAL.withDescription(throwable.getMessage())));
    }

}
