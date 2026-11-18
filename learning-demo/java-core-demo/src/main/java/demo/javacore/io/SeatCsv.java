package demo.javacore.io;

import demo.javacore.model.Seat;
import demo.javacore.model.SeatType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes seats to a CSV file and reads them back.
 *
 * <ul>
 *   <li>try-with-resources: the reader/writer is closed automatically, even when an exception is thrown.</li>
 *   <li>Buffered streams: data goes to disk in large blocks instead of one system call per character.</li>
 *   <li>Checked exceptions: {@link IOException} must be declared or caught; the compiler enforces it.</li>
 *   <li>Exception chaining: a bad value becomes a {@link CsvFormatException} that keeps the original cause.</li>
 * </ul>
 */
public final class SeatCsv {

    static final String HEADER = "code,type,price";

    private SeatCsv() {
    }

    public static void write(Path file, List<Seat> seats) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Seat seat : seats) {
                writer.write(seat.code() + "," + seat.type() + "," + seat.price().toPlainString());
                writer.newLine();
            }
        } // writer.close() runs here and flushes the buffer to disk
    }

    public static List<Seat> read(Path file) throws IOException, CsvFormatException {
        List<Seat> seats = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            reader.readLine(); // skip the header line
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) { // null = end of file
                lineNumber++;
                if (!line.isBlank()) {
                    seats.add(parseLine(line, lineNumber));
                }
            }
        }
        return seats;
    }

    private static Seat parseLine(String line, int lineNumber) throws CsvFormatException {
        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new CsvFormatException(lineNumber, "expected 3 columns but got " + parts.length, null);
        }
        try {
            return new Seat(parts[0].trim(), SeatType.valueOf(parts[1].trim()), new BigDecimal(parts[2].trim()));
        } catch (IllegalArgumentException e) { // also catches NumberFormatException, a subclass
            throw new CsvFormatException(lineNumber, e.getMessage(), e);
        }
    }
}
