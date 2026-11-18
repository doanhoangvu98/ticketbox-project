package demo.javacore.jdbc;

/**
 * Unchecked wrapper around {@link java.sql.SQLException}.
 *
 * <p>SQLException is checked: without wrapping, every caller up the stack would need
 * "throws SQLException", leaking the storage technology into business code.
 * The original exception is kept as the cause. Spring does the same with DataAccessException.
 */
public class JdbcException extends RuntimeException {

    public JdbcException(String message, Throwable cause) {
        super(message, cause);
    }
}
