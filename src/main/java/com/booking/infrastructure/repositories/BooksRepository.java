package com.booking.infrastructure.repositories;

import com.booking.grpc.Booking;
import com.booking.infrastructure.contracts.IBooksRepository;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.NoSuchElementException;

@ApplicationScoped
public class BooksRepository implements IBooksRepository {

    @Inject
    PgPool client;

    @Override
    public Uni<Row> findById(String id) {
        return client.preparedQuery(
                "SELECT * FROM books WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(rowSet -> {
                    if (!rowSet.iterator().hasNext()) {
                        throw new NoSuchElementException("Book not found with id: " + id);
                    }
                    return rowSet.iterator().next();
                });
    }

    @Override
    public Uni<JsonArray> findAll(int page, int pageSize) {
        int offset = page * pageSize;
        return client.preparedQuery(
                "SELECT * FROM books ORDER BY id LIMIT $1 OFFSET $2")
                .execute(Tuple.of(pageSize, offset))
                .onItem().transform(rowSet -> {
                    if (!rowSet.iterator().hasNext()) {
                        throw new NoSuchElementException("No books found on page: " + page);
                    }
                    JsonArray result = new JsonArray();
                    rowSet.forEach(row -> result.add(row.toJson()));
                    return result;
                });
    }

    @Override
    public Uni<RowSet<Row>> save(Booking.BookingRequest request) {
        return client.preparedQuery(
                "INSERT INTO books (id, name, author, price, book_language, pages, format) " +
                "VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *")
                .execute(Tuple.tuple(List.of(request.getId(),
                                        request.getName(),
                                        request.getAuthor(),
                                        request.getPrice(),
                                        request.getLanguage(),
                                        request.getPages(),
                                        request.getFormat())));
    }

    @Override
    public Uni<RowSet<Row>> update(Booking.BookingRequest request) {
        return client.preparedQuery(
                "UPDATE books SET name = $2, author = $3, price = $4, book_language = $5, " +
                "pages = $6, format = $7 WHERE id = $1 RETURNING *")
                .execute(Tuple.tuple(List.of(
                        request.getId(),
                        request.getName(),
                        request.getAuthor(),
                        request.getPrice(),
                        request.getLanguage(),
                        request.getPages(),
                        request.getFormat())));
    }

    @Override
    public Uni<RowSet<Row>> deleteById(String id) {
        return client.preparedQuery(
                "DELETE FROM books WHERE id = $1 RETURNING id")
                .execute(Tuple.of(id));
    }
}