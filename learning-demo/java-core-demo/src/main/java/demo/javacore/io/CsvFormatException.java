package demo.javacore.io;

/**
 * A CHECKED exception (extends Exception, not RuntimeException).
 *
 * <p>A malformed file is an expected, recoverable situation: the caller should show the line number
 * to the user so they can fix the file. Making it checked forces every caller of
 * {@link SeatCsv#read} to handle it; the compiler will not let them forget.
 */
public class CsvFormatException extends Exception {

    private final int lineNumber;

    public CsvFormatException(int lineNumber, String message, Throwable cause) {
        super("Line " + lineNumber + ": " + message, cause); // keep the original cause (exception chaining)
        this.lineNumber = lineNumber;
    }

    public int lineNumber() {
        return lineNumber;
    }
}
