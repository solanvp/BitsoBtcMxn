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
public class DiffOrder {
    
    private String o;  // Order id
    private Long d;    // Timestamp
    private Double r;  // Rate (mxn)
    private Integer t; // 0: Buy, 1:Sell
    private Double a;  // Ammount (btc)  
    private Double v;  // Value (mxn)  
    private String s;  // type

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    public Long getD() {
        return d;
    }

    public void setD(Long d) {
        this.d = d;
    }

    public Double getR() {
        return r;
    }

    public void setR(Double r) {
        this.r = r;
    }

    public Integer getT() {
        return t;
    }

    public void setT(Integer t) {
        this.t = t;
    }

    public Double getA() {
        return a;
    }

    public void setA(Double a) {
        this.a = a;
    }

    public Double getV() {
        return v;
    }

    public void setV(Double v) {
        this.v = v;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return "DiffOrder{" + "rate=" + r + ", t=" + t + ", amount=" + a + '}';
    }
    
    
    
}
