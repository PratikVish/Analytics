package com.upstox.model;

import lombok.Data;

@Data
public class UserSubscription {

    private String event;
    private String symbol;
    private String interval;

    @Override
    public String toString() {
        return "UserSubscription{" +
                "event='" + event + '\'' +
                ", symbol='" + symbol + '\'' +
                ", interval='" + interval + '\'' +
                '}';
    }
}
