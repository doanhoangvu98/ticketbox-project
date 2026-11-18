package demo.javacore.io;

import demo.javacore.model.SampleSeats;
import demo.javacore.model.Seat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeatCsvTest {

    @TempDir // JUnit creates an empty folder for each test and deletes it afterwards
    Path dir;

    @Test
    void writeThenRead_givesBackTheSameSeats() throws Exception {
        Path file = dir.resolve("seats.csv");
        List<Seat> seats = SampleSeats.all();

        SeatCsv.write(file, seats);

        assertThat(Files.readAllLines(file)).first().isEqualTo(SeatCsv.HEADER);
        assertThat(SeatCsv.read(file)).isEqualTo(seats);
    }

    @Test
    void badNumber_isReportedWithItsLineNumber_andKeepsTheCause() throws IOException {
        Path file = dir.resolve("bad.csv");
        Files.writeString(file, """
                code,type,price
                A1,VIP,150000
                B1,STANDARD,abc
                """);

        assertThatThrownBy(() -> SeatCsv.read(file))
                .isInstanceOf(CsvFormatException.class)
                .hasMessageStartingWith("Line 3")
                .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    void wrongColumnCount_isReported() throws IOException {
        Path file = dir.resolve("columns.csv");
        Files.writeString(file, "code,type,price\nA1,VIP\n");

        assertThatThrownBy(() -> SeatCsv.read(file))
                .isInstanceOfSatisfying(CsvFormatException.class, e -> assertThat(e.lineNumber()).isEqualTo(2));
    }

    @Test
    void missingFile_throwsAnIOException() {
        assertThatThrownBy(() -> SeatCsv.read(dir.resolve("does-not-exist.csv")))
                .isInstanceOf(NoSuchFileException.class)  // a subclass of IOException
                .isInstanceOf(IOException.class);
    }
}
