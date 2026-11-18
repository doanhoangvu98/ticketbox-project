package demo.javacore.network;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EchoServerTest {

    @Test
    void serverAnswersEveryLine() throws IOException {
        try (EchoServer server = new EchoServer(0)) { // 0 = any free port
            server.start();

            List<String> answers = EchoClient.send("localhost", server.port(), List.of("hello", "seat A1"));

            assertThat(answers).containsExactly("ECHO: hello", "ECHO: seat A1");
        }
    }

    @Test
    void serverHandlesSeveralClients() throws IOException {
        try (EchoServer server = new EchoServer(0)) {
            server.start();

            assertThat(EchoClient.send("localhost", server.port(), List.of("alice"))).containsExactly("ECHO: alice");
            assertThat(EchoClient.send("localhost", server.port(), List.of("bob"))).containsExactly("ECHO: bob");
        }
    }
}
