package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class LoadDuringMigrationApiTest {

    private static final int READERS = 4;
    private static final int HTTP_OK = 200;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookstore_api_migration")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @Test
    void oldNameApiKeepsServingRequestsDuringExpandAndBackfill() throws Exception {
        PGSimpleDataSource dataSource = createDataSource();
        migrateTo(dataSource, "2");
        seedCustomer(dataSource);

        HttpServer server = createLegacyNameApi(dataSource);
        server.start();
        List<String> failures = new CopyOnWriteArrayList<>();
        AtomicBoolean keepReading = new AtomicBoolean(true);
        AtomicInteger completedRequests = new AtomicInteger();
        CountDownLatch readersStarted = new CountDownLatch(READERS);
        ExecutorService readers = Executors.newFixedThreadPool(READERS);
        submitReaders(server, readers, readersStarted, keepReading, completedRequests, failures);

        try {
            assertThat(readersStarted.await(5, TimeUnit.SECONDS)).isTrue();
            migrateTo(dataSource, "4");
        } finally {
            keepReading.set(false);
            readers.shutdownNow();
            readers.awaitTermination(5, TimeUnit.SECONDS);
            server.stop(0);
        }

        assertThat(failures).isEmpty();
        assertThat(completedRequests.get()).isPositive();
        assertTriggerWritesBothNameVersions(dataSource);
    }

    private PGSimpleDataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        return dataSource;
    }

    private void migrateTo(PGSimpleDataSource dataSource, String version) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target(version)
                .load()
                .migrate();
    }

    private void seedCustomer(PGSimpleDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO customers (full_name) VALUES ('Ivan Petrov')");
        }
    }

    private HttpServer createLegacyNameApi(PGSimpleDataSource dataSource) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/customers/name", exchange -> {
            int status = HTTP_OK;
            byte[] body;
            try (Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement();
                    var result = statement.executeQuery("SELECT full_name FROM customers LIMIT 1")) {
                body = result.next()
                        ? result.getString(1).getBytes(StandardCharsets.UTF_8)
                        : new byte[0];
            } catch (SQLException exception) {
                status = 500;
                body = exception.getMessage().getBytes(StandardCharsets.UTF_8);
            }
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        return server;
    }

    private void assertTriggerWritesBothNameVersions(PGSimpleDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "INSERT INTO customers (full_name) VALUES ('Jane Doe')");
            try (var legacyWrite = statement.executeQuery(
                    "SELECT first_name, last_name FROM customers WHERE full_name = 'Jane Doe'")) {
                assertThat(legacyWrite.next()).isTrue();
                assertThat(legacyWrite.getString("first_name")).isEqualTo("Jane");
                assertThat(legacyWrite.getString("last_name")).isEqualTo("Doe");
            }
            statement.executeUpdate(
                    "UPDATE customers SET first_name = 'John', last_name = 'Smith'"
                            + " WHERE full_name = 'Jane Doe'");
            try (var newWrite = statement.executeQuery(
                    "SELECT full_name FROM customers WHERE first_name = 'John'")) {
                assertThat(newWrite.next()).isTrue();
                assertThat(newWrite.getString("full_name")).isEqualTo("John Smith");
            }
        }
    }

    private void submitReaders(
            HttpServer server,
            ExecutorService readers,
            CountDownLatch readersStarted,
            AtomicBoolean keepReading,
            AtomicInteger completedRequests,
            List<String> failures) {
        URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/customers/name");
        HttpClient client = HttpClient.newHttpClient();
        for (int i = 0; i < READERS; i++) {
            readers.submit(() -> readUntilStopped(
                    client, uri, readersStarted, keepReading, completedRequests, failures));
        }
    }

    private void readUntilStopped(
            HttpClient client,
            URI uri,
            CountDownLatch readersStarted,
            AtomicBoolean keepReading,
            AtomicInteger completedRequests,
            List<String> failures) {
        readersStarted.countDown();
        while (keepReading.get()) {
            try {
                HttpResponse<String> response = client.send(
                        HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != HTTP_OK) {
                    failures.add("HTTP " + response.statusCode());
                } else {
                    completedRequests.incrementAndGet();
                }
            } catch (IOException exception) {
                failures.add(exception.getMessage());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
