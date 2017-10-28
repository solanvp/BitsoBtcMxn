/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.solanvpnetworkers.bitso.entity;

/**
 *
 * @author solanvp
 */
public class OrderBookResponse {
    
    private boolean success;
    private OrderBook payload;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public OrderBook getPayload() {
        return payload;
    }

    public void setPayload(OrderBook payload) {
        this.payload = payload;
    }
    
}
