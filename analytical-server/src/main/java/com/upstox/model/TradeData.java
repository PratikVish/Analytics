package com.upstox.model;

import lombok.Data;
import lombok.SneakyThrows;

import java.util.Date;

@Data
public class TradeData implements Comparable {

    public TradeData() {
    }

    private String sym;
    private String t;
    private double p;
    private double q;
    private String ts;
    private String ts2;
    private String side;

    @SneakyThrows
    @Override
    public int compareTo(Object o) {
        TradeData obj = (TradeData) o;
        long objTs2 = Long.parseLong(obj.ts2);
        long currTs2 = Long.parseLong(this.ts2);

        Date d1 = new Date(currTs2 * 1000L);
        Date d2 = new Date(objTs2 * 1000L);

        return d1.compareTo(d2);
    }

}

