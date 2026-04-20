package com.booking.application.services;

import com.booking.application.ports.IBookingService;
import com.booking.grpc.Booking;
import com.booking.infrastructure.contracts.IBooksRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Row;
import java.util.NoSuchElementException;

@ApplicationScoped
public class BookingService implements IBookingService {

    @Inject
    IBooksRepository booksRepository;

    @Override
    public Uni<Booking.BookingResponse> getBook(Booking.BookingRequestId request) {
        System.out.println("Acá");
        return booksRepository.findById(request.getId())
                .onItem().transform(row -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.OK.code())
                        .setPayload(row.toJson().encode())
                        .build())
                .onFailure(NoSuchElementException.class).recoverWithItem(throwable ->
                        Booking.BookingResponse.newBuilder()
                                .setStatus(HttpResponseStatus.NOT_FOUND.code())
                                .setPayload(throwable.getMessage())
                                .build())
                .onFailure().recoverWithItem(throwable -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .setPayload(throwable.getMessage())
                        .build());
    }

    @Override
    public Uni<Booking.BookingResponse> getAllBooks(Booking.BookingRequestPaged request) {
        System.out.println("Acá");
        return booksRepository.findAll(Integer.parseInt(request.getPage()), 10)
                .onItem().transform(res -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.OK.code())
                        .setPayload(res.encode())
                        .build())
                .onFailure(NoSuchElementException.class).recoverWithItem(throwable ->
                        Booking.BookingResponse.newBuilder()
                                .setStatus(HttpResponseStatus.NOT_FOUND.code())
                                .setPayload(throwable.getMessage())
                                .build())
                .onFailure().recoverWithItem(throwable -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .setPayload(throwable.getMessage())
                        .build());

    }

    @Override
    public Uni<Booking.BookingResponse> createBook(Booking.BookingRequest request) {
        return booksRepository.save(request)
                .onItem().transform(res -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.OK.code())
                        .setPayload(toJson(res))
                        .build())
                .onFailure().recoverWithItem(throwable -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .setPayload(throwable.getMessage())
                        .build());
    }

    @Override
    public Uni<Booking.BookingResponse> updateBook(Booking.BookingRequest request) {
        return booksRepository.update(request)
                .onItem().transform(res -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.OK.code())
                        .setPayload(toJson(res))
                        .build())
                .onFailure().recoverWithItem(throwable -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .setPayload(throwable.getMessage())
                        .build());
    }

    @Override
    public Uni<Booking.BookingResponse> deleteBook(Booking.BookingRequestId request) {
        return booksRepository.deleteById(request.getId())
                .onItem().transform(res -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.OK.code())
                        .setPayload(toJson(res))
                        .build())
                .onFailure().recoverWithItem(throwable -> Booking.BookingResponse.newBuilder()
                        .setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .setPayload(throwable.getMessage())
                        .build());
    }

    private String toJson(RowSet<Row> rowSet) {
        Row row = rowSet.iterator().next();
        return row.toJson().encode();
    }
}
