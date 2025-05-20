package dev.seqism.common.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeqismMessage implements Serializable {
    private final SeqismMessageHeader header;
    private final String message;

    public SeqismMessage() {
        this(new SeqismMessageHeader(), null);
    }

    public SeqismMessage(String errorMessage) {
        this(new SeqismMessageHeader(), errorMessage);
    }

    public boolean isInProgress() {
        return this.header.status == SeqismMessageStatus.IN_PROGRESS;
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

        public SeqismMessageHeader toSuccess() {
            return success(bizCode, tranId);
        }

        public SeqismMessageHeader toFailure() {
            return failure(bizCode, tranId);
        }

        // 진행중 Header 생성
        public static SeqismMessageHeader inProgress(String bizCode, String tranId) {
            return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.IN_PROGRESS);
        }

        // 성공 Header 생성
        public static SeqismMessageHeader success(String bizCode, String tranId) {
            return new SeqismMessageHeader(bizCode, tranId, SeqismMessageStatus.SUCCESS);
        }

        // 실패 Header 생성
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