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
        this(new SeqismMessageHeader(), null);
    }

    public SeqismMessage(T errorBody) {
        this(new SeqismMessageHeader(), errorBody);
    }

    @Data
    @AllArgsConstructor
    public static class SeqismMessageHeader implements Serializable {
        private final String bizCode;
        private final String tranId;
        private final SeqismMessageStatus status;

        public SeqismMessageHeader() {
            this(null, null, SeqismMessageStatus.FAILURE);
        }

        public SeqismMessageHeader(String bizCode, String tranId) {
            this(bizCode, tranId, SeqismMessageStatus.IN_PROGRESS);
        }

        public SeqismMessageHeader toInProgress() {
            return inProgress(bizCode, tranId);
        }

        public SeqismMessageHeader toSuccess() {
            return success(bizCode, tranId);
        }

        public SeqismMessageHeader toFailure() {
            return failure(bizCode, tranId);
        }

        public static SeqismMessageHeader inProgress(String bizCode, String tranId) {
            return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.IN_PROGRESS);
        }

        public static SeqismMessageHeader success(String bizCode, String tranId) {
            return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.SUCCESS);
        }

        public static SeqismMessageHeader failure(String bizCode, String tranId) {
            return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.FAILURE);
        }
    }

    public enum SeqismMessageStatus {
        IN_PROGRESS,   // 진행중
        SUCCESS,       // 완료_성공
        FAILURE        // 완료_실패
    }
}