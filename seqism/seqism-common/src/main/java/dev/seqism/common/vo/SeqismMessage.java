package dev.seqism.common.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeqismMessage implements Serializable {
    private final String tranId;
    private final SeqismMessageType type;
    private final String message;

    public SeqismMessage() {
        this(null, null);
    }

    public SeqismMessage(String errorMessage) {
        this(null, SeqismMessageType.FAILURE, errorMessage);
    }

    public SeqismMessage(String tranId, String message) {
        this(tranId, SeqismMessageType.IN_PROGRESS, message);
    }

    public enum SeqismMessageType {
        IN_PROGRESS,   // 진행중
        SUCCESS,       // 완료_성공
        FAILURE        // 완료_실패
    }
}