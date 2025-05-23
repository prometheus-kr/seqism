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
        return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.IN_PROGRESS, null);
    }

    SeqismMessageHeader toSuccess() {
        return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.SUCCESS, null);
    }

    SeqismMessageHeader toFailure(ErrorInfo errorInfo) {
        return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.FAILURE, errorInfo);
    }
}