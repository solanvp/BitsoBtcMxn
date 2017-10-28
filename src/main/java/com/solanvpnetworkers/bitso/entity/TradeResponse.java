package com.solanvpnetworkers.bitso.entity;

import java.util.List;

public class TradeResponse {

    private Boolean success;
    private List<Trade> payload;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<Trade> getPayload() {
        return payload;
    }

    public void setPayload(List<Trade> payload) {
        this.payload = payload;
    }

}
