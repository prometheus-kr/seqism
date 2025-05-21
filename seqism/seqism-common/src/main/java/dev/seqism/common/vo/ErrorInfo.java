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
        return new ErrorInfo(this.errorCode, this.errorMessage + " - " + message);
    }

    // Common Error
    public static ErrorInfo ERROR_0000_0001 = new ErrorInfo("00000001", "Common Error");

    // MQ Error
    public static ErrorInfo ERROR_0001_0001 = new ErrorInfo("00010001", "MQ Error");

    // BizCode Error
    public static ErrorInfo ERROR_0002_0001 = new ErrorInfo("00020001", "BizError : No processor found for bizCode");
    public static ErrorInfo ERROR_0002_0002 = new ErrorInfo("00020002", "BizError : An exception occurred while processing");
}
