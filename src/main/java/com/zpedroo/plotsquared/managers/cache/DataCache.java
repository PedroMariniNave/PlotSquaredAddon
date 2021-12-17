package com.zpedroo.plotsquared.managers.cache;

import com.zpedroo.plotsquared.objects.SellingPlot;

import java.util.LinkedList;
import java.util.List;

public class DataCache {

    private List<SellingPlot> sellingPlots;
    private List<SellingPlot> finishedSellings;

    public DataCache() {
        this.finishedSellings = new LinkedList<>();
    }

    public List<SellingPlot> getSellingPlots() {
        return sellingPlots;
    }

    public List<SellingPlot> getFinishedSellings() {
        return finishedSellings;
    }

    public void setSellingPlots(List<SellingPlot> sellingPlots) {
        this.sellingPlots = sellingPlots;
    }
}