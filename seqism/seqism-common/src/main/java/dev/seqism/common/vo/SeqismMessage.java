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
        this.header = null;
        this.body = null;
    }

    public SeqismMessage(SeqismMessageHeader header) {
        this.header = header;
        this.body = null;
    }

    public SeqismMessage<T> toFailure(ErrorInfo errorInfo, String message) {
        return new SeqismMessage<T>(header.toFailure(errorInfo.addMessage(message)));
    }

    public <N> SeqismMessage<N> withBody(N body) {
        return new SeqismMessage<N>(header, body);
    }

    @Data
    @AllArgsConstructor
    public static class SeqismMessageHeader implements Serializable {
        private final String bizCode;
        private final String tranId;
        private final SeqismMessageStatus status;
        private final ErrorInfo error; // errorCode, errorMessage를 묶어서 사용

        public SeqismMessageHeader() {
            this(null, null, SeqismMessageStatus.FAILURE, null);
        }

        public SeqismMessageHeader(String bizCode, String tranId) {
            this(bizCode, tranId, SeqismMessageStatus.IN_PROGRESS, null);
        }

        public SeqismMessageHeader toInProgress() {
            return inProgress(bizCode, tranId);
        }

        public SeqismMessageHeader toSuccess() {
            return success(bizCode, tranId);
        }

        SeqismMessageHeader toFailure(ErrorInfo errorInfo) {
            return failure(bizCode, tranId, errorInfo);
        }

        public static SeqismMessageHeader inProgress(String bizCode, String tranId) {
            return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.IN_PROGRESS, null);
        }

        public static SeqismMessageHeader success(String bizCode, String tranId) {
            return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.SUCCESS, null);
        }

        public static SeqismMessageHeader failure(String bizCode, String tranId, ErrorInfo errorInfo) {
            return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.FAILURE, errorInfo);
        }
    }

    public enum SeqismMessageStatus {
        IN_PROGRESS,   // 진행중
        SUCCESS,       // 완료_성공
        FAILURE        // 완료_실패
    }
}