package com.upstox.model;

import lombok.Data;

@Data
public class BarChartResponse implements Cloneable{

    private String event;
    private String symbol;
    private String bar_num;
    private String o;
    private double h;
    private double l;
    private double c;
    private double volume;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "BarChartResponse{" +
                "event='" + event + '\'' +
                ", symbol='" + symbol + '\'' +
                ", bar_num='" + bar_num + '\'' +
                ", o='" + o + '\'' +
                ", h=" + h +
                ", l=" + l +
                ", c=" + c +
                ", volume=" + volume +
                '}';
    }
}
