package dev.seqism.common.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeqismMessageHeader implements Serializable {
    private final String bizCode;
    private final String tranId;
    private final SeqismMessageStatus status;
    private final ErrorInfo error;

    public SeqismMessageHeader() {
        this(null, null, SeqismMessageStatus.FAILURE, null);
    }

    SeqismMessageHeader toInProgress() {
        return toInProgress(tranId);
    }

    SeqismMessageHeader toInProgress(String tranId) {
        return of(bizCode, tranId, SeqismMessageStatus.IN_PROGRESS);
    }

    SeqismMessageHeader toSuccess() {
        return of(bizCode, tranId, SeqismMessageStatus.SUCCESS);
    }

    SeqismMessageHeader toFailure(ErrorInfo errorInfo) {
        return of(bizCode, tranId, SeqismMessageStatus.FAILURE, errorInfo);
    }

    static SeqismMessageHeader of(String bizCode, String tranId, SeqismMessageStatus status) {
        return of(bizCode, tranId, status, null);
    }

    static SeqismMessageHeader of(String bizCode, String tranId, SeqismMessageStatus status, ErrorInfo errorInfo) {
        return new SeqismMessageHeader(bizCode, tranId, status, errorInfo);
    }
}