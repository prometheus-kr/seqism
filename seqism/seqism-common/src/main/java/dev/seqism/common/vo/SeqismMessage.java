package dev.seqism.common.vo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeqismMessage<T> implements Serializable {
    private final SeqismMessageHeader header;
    private final T body;

    public SeqismMessage() {
        this(null, null);
    }

    public SeqismMessage<T> toSuccess() {
        return of(header.toSuccess(), body);
    }

    public SeqismMessage<T> toInProgress() {
        return of(header.toInProgress(), body);
    }

    public SeqismMessage<T> toInProgress(String tranId) {
        return of(header.toInProgress(tranId), body);
    }

    public SeqismMessage<T> toFailure(ErrorInfo errorInfo) {
        return of(header.toFailure(errorInfo), null);
    }

    public SeqismMessage<T> toFailure(ErrorInfo errorInfo, String message) {
        return of(header.toFailure(errorInfo.addMessage(message)), null);
    }

    public <N> SeqismMessage<N> withBody(N body) {
        return of(header, body);
    }

    static <T> SeqismMessage<T> of(SeqismMessageHeader header, T body) {
        return new SeqismMessage<T>(header, body);
    }

    @Data
    @AllArgsConstructor
    public static class SeqismMessageHeader implements Serializable {
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

    public enum SeqismMessageStatus {
        IN_PROGRESS,   // 진행중
        SUCCESS,       // 완료_성공
        FAILURE        // 완료_실패
    }
}