package dev.seqism.common.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorInfo implements Serializable {
    private final String errorCode;
    private final String errorMessage;

    ErrorInfo() {
        this(null, null);
    }

    public ErrorInfo addMessage(String message) {
        return new ErrorInfo(this.errorCode, this.errorMessage + " : " + message);
    }

    // Gateway Error
    public static ErrorInfo ERROR_0001_0001 = new ErrorInfo("00010001", "GW Error");
    public static ErrorInfo ERROR_0001_0002 = new ErrorInfo("00010002", "GW Error : No response from MQ (timeout) or invalid request");
    public static ErrorInfo ERROR_0001_0003 = new ErrorInfo("00010003", "GW Error : Queue does not exist");
    public static ErrorInfo ERROR_0001_0004 = new ErrorInfo("00010004", "GW Error : Failed to send message to MQ");
    public static ErrorInfo ERROR_0001_0005 = new ErrorInfo("00010005", "GW Error : Failed to receive message from MQ");
    
    // BizProcessor Error
    public static ErrorInfo ERROR_0002_0001 = new ErrorInfo("00020001", "BP Error : No processor found for bizCode");
    public static ErrorInfo ERROR_0002_0002 = new ErrorInfo("00020002", "BP Error : An exception occurred while processing");
    public static ErrorInfo ERROR_0002_0003 = new ErrorInfo("00010002", "BP Error : Timeout occurred while waiting for response from queue");
}
