package demo.javacore.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * TCP client: connects to a server, sends each line and reads one answer per line.
 */
public final class EchoClient {

    private static final int READ_TIMEOUT_MILLIS = 5_000;

    private EchoClient() {
    }

    public static List<String> send(String host, int port, List<String> lines) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            socket.setSoTimeout(READ_TIMEOUT_MILLIS); // never wait forever on the network
            List<String> answers = new ArrayList<>();
            for (String line : lines) {
                out.println(line);
                answers.add(in.readLine()); // blocks until the server answers
            }
            return answers;
        }
    }
}
