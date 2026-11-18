package demo.javacore.network;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Calls the TicketBox REST API with the JDK's own HTTP client (java.net.http, Java 11+).
 *
 * <p>Against the running ticketbox-api: {@code new EventApiClient(URI.create("http://localhost:8080"))}.
 * The frontend does the same thing in JavaScript with axios.
 */
public class EventApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    private final URI baseUri;

    public EventApiClient(URI baseUri) {
        this.baseUri = baseUri;
    }

    /** GET /api/v1/events?size=..., returns the raw JSON body. */
    public String fetchEvents(int size) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/api/v1/events?size=" + size))
                .header("Accept", "application/json")
                .timeout(TIMEOUT)
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Unexpected HTTP status " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }
}
