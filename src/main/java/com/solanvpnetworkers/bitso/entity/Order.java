/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.solanvpnetworkers.bitso.entity;

import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author solanvp
 */
public class Order {
    
    private String book;
    private String oid;
    private Double price;
    private Double amount;

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
    
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public String fomattedPrice(){
        Locale mx = new Locale("es", "MX");
        NumberFormat mxFormat = NumberFormat.getCurrencyInstance(mx);
        return mxFormat.format(price);
    }

    @Override
    public String toString() {
        return "Order{" + "book=" + book + ", oid=" + oid + ", price=" + price + ", amount=" + amount + '}';
    }
    
}

