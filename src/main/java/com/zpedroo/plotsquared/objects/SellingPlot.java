package com.zpedroo.plotsquared.objects;

import com.intellectualcrafters.plot.object.Plot;
import com.zpedroo.multieconomy.objects.Currency;

import java.math.BigInteger;

public class SellingPlot {

    private Plot plot;
    private BigInteger price;
    private Currency currency;
    private Boolean update;

    public SellingPlot(Plot plot, BigInteger price, Currency currency) {
        this.plot = plot;
        this.price = price;
        this.currency = currency;
        this.update = false;
    }

    public Plot getPlot() {
        return plot;
    }

    public BigInteger getPrice() {
        return price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Boolean isQueueUpdate() {
        return update;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
        this.update = true;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
        this.update = true;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }
}