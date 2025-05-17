package dev.seqism.common.vo;

import java.io.Serializable;

public class SeqismMessage implements Serializable {
    private String tranId;
    private String message;

    public SeqismMessage() {}

    public SeqismMessage(String tranId, String message) {
        this.tranId = tranId;
        this.message = message;
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}