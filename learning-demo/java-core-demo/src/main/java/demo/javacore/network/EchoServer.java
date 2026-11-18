package demo.javacore.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A tiny TCP server: for every line a client sends, it answers "ECHO: line".
 *
 * <ul>
 *   <li>ServerSocket.accept() blocks until a client connects and returns a Socket for that client.</li>
 *   <li>Each client is handled on its own thread, so one slow client does not block the others.</li>
 *   <li>Socket streams carry bytes; InputStreamReader/PrintWriter turn them into text lines.</li>
 * </ul>
 * Under Spring Boot, the embedded Tomcat does this for us (at a much larger scale) and speaks HTTP.
 */
public class EchoServer implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final ExecutorService clientThreads = Executors.newCachedThreadPool();

    /** Port 0 = let the operating system pick a free port (handy for tests). */
    public EchoServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void start() {
        Thread acceptThread = new Thread(this::acceptClients, "echo-accept");
        acceptThread.setDaemon(true); // a daemon thread does not keep the JVM alive
        acceptThread.start();
    }

    public int port() {
        return serverSocket.getLocalPort();
    }

    private void acceptClients() {
        while (!serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept(); // blocks until a client connects
                clientThreads.submit(() -> handle(client));
            } catch (SocketException e) {
                return; // close() was called: a blocked accept() ends with a SocketException
            } catch (IOException e) {
                System.err.println("Accept failed: " + e.getMessage());
            }
        }
    }

    private void handle(Socket client) {
        try (client;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true, StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) { // null = the client closed the connection
                out.println("ECHO: " + line);       // autoFlush=true sends the line immediately
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
        clientThreads.shutdownNow();
    }
}
