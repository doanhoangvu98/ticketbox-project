package demo.javacore.network;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Uses the JDK's built-in HttpServer as a fake API, so the test does not need ticketbox-api to be running.
 */
class EventApiClientTest {

    private HttpServer server;
    private final AtomicReference<String> receivedQuery = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0); // any free port, all network interfaces
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void fetchEvents_returnsTheJsonBody_andSendsTheSizeParameter() throws Exception {
        respondWith(200, "{\"items\":[{\"id\":1,\"name\":\"Rock Night\"}]}");

        String json = new EventApiClient(baseUri()).fetchEvents(2);

        assertThat(json).contains("Rock Night");
        assertThat(receivedQuery.get()).isEqualTo("size=2");
    }

    @Test
    void fetchEvents_failsOnAnErrorStatus() {
        respondWith(500, "{\"errorCode\":\"INTERNAL\"}");

        assertThatThrownBy(() -> new EventApiClient(baseUri()).fetchEvents(2))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("500");
    }

    private void respondWith(int status, String body) {
        server.createContext("/api/v1/events", exchange -> {
            receivedQuery.set(exchange.getRequestURI().getQuery());
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    private URI baseUri() {
        return URI.create("http://localhost:" + server.getAddress().getPort());
    }
}
