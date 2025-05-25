package dev.seqism.common.vo;

/**
 * Custom runtime exception for the Seqism application.
 * <p>
 * This exception encapsulates an {@link ErrorInfo} object, providing
 * additional context about the error that occurred.
 *
 * <p>
 * Typical usage involves throwing this exception when a specific error
 * condition, represented by an {@code ErrorInfo} instance, is encountered.
 *
 * @see ErrorInfo
 */
public class SeqismException extends RuntimeException {
    /**
     * Holds detailed information about the specific error associated with this exception.
     */
    private final ErrorInfo errorInfo;

    /**
     * Constructs a new {@code SeqismException} with the specified {@link ErrorInfo}.
     * The exception message is set to the error message provided by the {@code ErrorInfo} object.
     *
     * @param errorInfo
     *            the {@code ErrorInfo} object containing details about the error;
     *            if {@code null}, the exception message will also be {@code null}
     */
    public SeqismException(ErrorInfo errorInfo) {
        super(errorInfo != null ? errorInfo.getErrorMessage() : null);
        this.errorInfo = errorInfo;
    }

    /**
     * Constructs a new {@code SeqismException} with the specified {@link ErrorInfo} and cause.
     *
     * @param errorInfo
     *            the error information associated with this exception; may be {@code null}
     * @param cause
     *            the cause of this exception; may be {@code null}
     */
    public SeqismException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo != null ? errorInfo.getErrorMessage() : null, cause);
        this.errorInfo = errorInfo;
    }

    /**
     * Returns the {@link ErrorInfo} associated with this exception.
     *
     * @return the error information encapsulated in this exception
     */
    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}