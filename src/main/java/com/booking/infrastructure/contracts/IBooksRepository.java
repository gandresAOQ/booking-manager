package com.booking.infrastructure.contracts;

import com.booking.grpc.Booking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Row;

public interface IBooksRepository {
    Uni<Row> findById(String id);
    Uni<JsonArray> findAll(int page, int pageSize);
    Uni<RowSet<Row>> save(Booking.BookingRequest request);
    Uni<RowSet<Row>> update(Booking.BookingRequest request);
    Uni<RowSet<Row>> deleteById(String id);
}