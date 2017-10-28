/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.solanvpnetworkers.bitso.entity;

import java.util.List;

/**
 *
 * @author solanvp
 */
public class DiffOrderResponse {
    
    private String type;
    private String book;
    private List<DiffOrder> payload;
    private Long sequence;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public List<DiffOrder> getPayload() {
        return payload;
    }

    public void setPayload(List<DiffOrder> payload) {
        this.payload = payload;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }
    
}
