package com.solanvpnetworkers.bitso.entity;

import java.security.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Trade {

    private String book;
    private Date created_at;
    private Double amount;     // btc
    private String maker_side; // buy | sell
    private Double price;      // mxn
    private Long tid;          // trade id
    
    private Boolean isMockTrade = false;
    
    public Trade(){}
    
    public Trade(boolean isMockSellTrade, double price){   // Mock trade construtor
        this.book = "btc_mxn";
        this.created_at = new Date();
        this.amount = 1.0d;
        this.maker_side = isMockSellTrade ? "sell" : "buy";
        this.price = price;
        this.tid = 0L;
        this.isMockTrade = true;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMaker_side() {
        return maker_side;
    }

    public void setMaker_side(String maker_side) {
        this.maker_side = maker_side;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    @Override
    public String toString() {
        String timeStr = new SimpleDateFormat("HH:mm:ss").format(created_at);
        Locale mx = new Locale("es", "MX");
        NumberFormat mxFormat = NumberFormat.getCurrencyInstance(mx);
        String priceStr = mxFormat.format(price);
        String typeStr = maker_side.equals("sell") ? "Sell" : "Buy ";
        String mock = isMockTrade ? " MOCK" : "";
        return timeStr + " :: Trade ["+ typeStr + "] ::  " + String.format("%.8f", amount) + " btc @ " + priceStr + " mxn" + mock;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.tid);
        hash = 79 * hash + Objects.hashCode(this.isMockTrade);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Trade other = (Trade) obj;
        if (!Objects.equals(this.tid, other.tid)) {
            return false;
        }
        return Objects.equals(this.isMockTrade, other.isMockTrade);
    }

}
