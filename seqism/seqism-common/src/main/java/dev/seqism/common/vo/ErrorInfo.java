package dev.seqism.common.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents error information with a code and message.
 * <p>
 * This class is used to encapsulate error details, including a unique error
 * code and a descriptive message.
 * It provides predefined static instances for common gateway and business
 * processor errors.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * ErrorInfo error = ErrorInfo.ERROR_0001_0001.addMessage("Additional details");
 * </pre>
 *
 * <ul>
 * <li>errorCode - Unique identifier for the error.</li>
 * <li>errorMessage - Human-readable description of the error.</li>
 * </ul>
 *
 * <p>
 * Predefined errors include:
 * <ul>
 * <li>Gateway Errors (error codes starting with 0001)</li>
 * <li>BizProcessor Errors (error codes starting with 0002)</li>
 * </ul>
 *
 * <p>
 * This class is immutable and serializable.
 *
 * @since 0.0.1
 */
@Data
@AllArgsConstructor
public class ErrorInfo implements Serializable {
    /**
     * A unique code representing the specific error that occurred.
     */
    private final String errorCode;
    /**
     * A descriptive message providing details about the error that occurred.
     */
    private final String errorMessage;

    /**
     * Default constructor for ErrorInfo.
     * Initializes an instance with null values for its fields.
     */
    ErrorInfo() {
        this(null, null);
    }

    /**
     * Returns a new {@code ErrorInfo} instance with the specified message appended
     * to the existing error message.
     *
     * @param message
     *            the message to append to the current error message
     * @return a new {@code ErrorInfo} object with the updated error message
     */
    public ErrorInfo addMessage(String message) {
        return new ErrorInfo(this.errorCode, this.errorMessage + " : " + message);
    }

    /**
     * Represents a gateway error with code "00010001".
     * This error indicates a general gateway (GW) error condition.
     */
    public static ErrorInfo ERROR_0001_0001 = new ErrorInfo("00010001",
            "GW Error");
    /**
     * Represents an error indicating that there was no response from MQ (timeout)
     * or the request was invalid.
     * <p>
     * Error Code: 00010002<br>
     * Description: GW Error : No response from MQ (timeout) or invalid request
     */
    public static ErrorInfo ERROR_0001_0002 = new ErrorInfo("00010002",
            "GW Error : No response from MQ (timeout) or invalid request");
    /**
     * Represents an error indicating that the queue does not exist.
     * <p>
     * Error Code: 00010003<br>
     * Message: "GW Error : Queue does not exist"
     */
    public static ErrorInfo ERROR_0001_0003 = new ErrorInfo("00010003",
            "GW Error : Queue does not exist");
    /**
     * Represents an error indicating a failure to send a message to the message
     * queue (MQ) via the gateway (GW).
     * <p>
     * Error Code: 00010004<br>
     * Description: GW Error : Failed to send message to MQ
     */
    public static ErrorInfo ERROR_0001_0004 = new ErrorInfo("00010004",
            "GW Error : Failed to send message to MQ");
    /**
     * Represents an error indicating a failure to receive a message from MQ via the
     * gateway.
     * <p>
     * Error Code: 00010005<br>
     * Message: "GW Error : Failed to receive message from MQ"
     */
    public static ErrorInfo ERROR_0001_0005 = new ErrorInfo("00010005",
            "GW Error : Failed to receive message from MQ");

    // BizProcessor Error
    /**
     * ErrorInfo instance representing a business process error where no processor
     * is found for the specified bizCode.
     * <p>
     * Error Code: 00020001<br>
     * Message: BP Error : No processor found for bizCode
     */
    public static ErrorInfo ERROR_0002_0001 = new ErrorInfo("00020001",
            "BP Error : No processor found for bizCode");
    /**
     * Represents a BP (Business Process) error indicating that an exception
     * occurred during processing.
     * <p>
     * Error Code: 00020002
     * <p>
     * Message: "BP Error : An exception occurred while processing"
     */
    public static ErrorInfo ERROR_0002_0002 = new ErrorInfo("00020002",
            "BP Error : An exception occurred while processing");
    /**
     * Represents an error indicating that a timeout occurred while waiting for a
     * response from the queue.
     * <p>
     * Error Code: 00020003<br>
     * Message: "BP Error : Timeout occurred while waiting for response from queue"
     */
    public static ErrorInfo ERROR_0002_0003 = new ErrorInfo("00020003",
            "BP Error : Timeout occurred while waiting for response from queue");
    /**
     * ErrorInfo instance representing a BP Error when failing to send a message to
     * MQ.
     * <p>
     * Error Code: 00020004<br>
     * Description: BP Error : Failed to send message to MQ
     */
    public static ErrorInfo ERROR_0002_0004 = new ErrorInfo("00020004",
            "BP Error : Failed to send message to MQ");
    /**
     * Represents an error indicating a failure to receive a message from MQ
     * (Message Queue).
     * <p>
     * Error Code: 00020005<br>
     * Description: BP Error : Failed to receive message from MQ
     */
    public static ErrorInfo ERROR_0002_0005 = new ErrorInfo("00020005",
            "BP Error : Failed to receive message from MQ");
}
