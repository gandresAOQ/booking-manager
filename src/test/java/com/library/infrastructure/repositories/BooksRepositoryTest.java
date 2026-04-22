package com.library.infrastructure.repositories;

import com.booking.grpc.Booking.BookFormat;
import com.booking.grpc.Booking.BookLanguage;
import com.booking.grpc.Booking.BookingRequest;
import com.booking.infrastructure.contracts.IBooksRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class BooksRepositoryTest {

    @InjectMock
    PgPool client;

    @Inject
    IBooksRepository booksRepository;

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void findById_returnsRowWhenFound() {
        Row row = mock(Row.class);
        RowIterator<Row> iterator = mock(RowIterator.class);
        when(iterator.hasNext()).thenReturn(true);
        when(iterator.next()).thenReturn(row);

        RowSet<Row> rowSet = mock(RowSet.class);
        when(rowSet.iterator()).thenReturn(iterator);

        PreparedQuery<RowSet<Row>> preparedQuery = mock(PreparedQuery.class);
        when(client.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Uni.createFrom().item(rowSet));

        Row result = booksRepository.findById("id-1").await().indefinitely();

        assertEquals(row, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findById_whenNoRowExists_throwsNoSuchElementException() {
        RowIterator<Row> iterator = mock(RowIterator.class);
        when(iterator.hasNext()).thenReturn(false);

        RowSet<Row> rowSet = mock(RowSet.class);
        when(rowSet.iterator()).thenReturn(iterator);

        PreparedQuery<RowSet<Row>> preparedQuery = mock(PreparedQuery.class);
        when(client.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Uni.createFrom().item(rowSet));

        assertThrows(NoSuchElementException.class,
                () -> booksRepository.findById("missing-id").await().indefinitely());
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void findAll_returnsJsonArrayWithAllRows() {
        JsonObject json1 = new JsonObject().put("id", "id-1").put("name", "Clean Code");
        JsonObject json2 = new JsonObject().put("id", "id-2").put("name", "Refactoring");
        Row row1 = mock(Row.class);
        Row row2 = mock(Row.class);
        when(row1.toJson()).thenReturn(json1);
        when(row2.toJson()).thenReturn(json2);

        RowIterator<Row> iterator = mock(RowIterator.class);
        when(iterator.hasNext()).thenReturn(true);

        RowSet<Row> rowSet = mock(RowSet.class);
        when(rowSet.iterator()).thenReturn(iterator);
        doAnswer(inv -> {
            Consumer<Row> consumer = inv.getArgument(0);
            consumer.accept(row1);
            consumer.accept(row2);
            return null;
        }).when(rowSet).forEach(any());

        PreparedQuery<RowSet<Row>> preparedQuery = mock(PreparedQuery.class);
        when(client.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Uni.createFrom().item(rowSet));

        JsonArray result = booksRepository.findAll(0, 10).await().indefinitely();

        assertEquals(2, result.size());
        assertEquals(json1, result.getJsonObject(0));
        assertEquals(json2, result.getJsonObject(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_whenNoRowsExist_throwsNoSuchElementException() {
        RowIterator<Row> iterator = mock(RowIterator.class);
        when(iterator.hasNext()).thenReturn(false);

        RowSet<Row> rowSet = mock(RowSet.class);
        when(rowSet.iterator()).thenReturn(iterator);

        PreparedQuery<RowSet<Row>> preparedQuery = mock(PreparedQuery.class);
        when(client.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Uni.createFrom().item(rowSet));

        assertThrows(NoSuchElementException.class,
                () -> booksRepository.findAll(99, 10).await().indefinitely());
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void save_executesInsertAndReturnsRowSet() {
        RowSet<Row> rowSet = mock(RowSet.class);

        PreparedQuery<RowSet<Row>> preparedQuery = mock(PreparedQuery.class);
        when(client.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Uni.createFrom().item(rowSet));

        BookingRequest request = BookingRequest.newBuilder()
                .setId("id-1").setName("Clean Code").setAuthor("Robert C. Martin")
                .setPrice(29.99).setLanguage(BookLanguage.ENGLISH).setPages(431).setFormat(BookFormat.HARDCOVER)
                .build();

        RowSet<Row> result = booksRepository.save(request).await().indefinitely();

        assertEquals(rowSet, result);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void update_executesUpdateAndReturnsRowSet() {
        RowSet<Row> rowSet = mock(RowSet.class);

        PreparedQuery<RowSet<Row>> preparedQuery = mock(PreparedQuery.class);
        when(client.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Uni.createFrom().item(rowSet));

        BookingRequest request = BookingRequest.newBuilder()
                .setId("id-1").setName("Clean Code Updated").setAuthor("Robert C. Martin")
                .setPrice(34.99).setLanguage(BookLanguage.SPANISH).setPages(450).setFormat(BookFormat.PAPERBACK)
                .build();

        RowSet<Row> result = booksRepository.update(request).await().indefinitely();

        assertEquals(rowSet, result);
    }

    // ── deleteById ───────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void deleteById_executesDeleteAndReturnsRowSet() {
        RowSet<Row> rowSet = mock(RowSet.class);

        PreparedQuery<RowSet<Row>> preparedQuery = mock(PreparedQuery.class);
        when(client.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Uni.createFrom().item(rowSet));

        RowSet<Row> result = booksRepository.deleteById("id-1").await().indefinitely();

        assertEquals(rowSet, result);
    }
}