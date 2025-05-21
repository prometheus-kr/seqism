package dev.seqism.common.vo;

public class SeqismException extends RuntimeException {
    private final ErrorInfo errorInfo;

    public SeqismException(ErrorInfo errorInfo) {
        super(errorInfo != null ? errorInfo.getErrorMessage() : null);
        this.errorInfo = errorInfo;
    }

    public SeqismException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo != null ? errorInfo.getErrorMessage() : null, cause);
        this.errorInfo = errorInfo;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}