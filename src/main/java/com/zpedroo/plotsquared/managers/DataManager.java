package com.zpedroo.plotsquared.managers;

import com.intellectualcrafters.plot.object.Plot;
import com.zpedroo.plotsquared.PlotSquaredAddon;
import com.zpedroo.plotsquared.managers.cache.DataCache;
import com.zpedroo.plotsquared.mysql.DBConnection;
import com.zpedroo.plotsquared.objects.SellingPlot;

import java.util.HashSet;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private DataCache dataCache;

    public DataManager() {
        instance = this;
        this.dataCache = new DataCache();
        PlotSquaredAddon.get().getServer().getScheduler().runTaskLaterAsynchronously(PlotSquaredAddon.get(), this::loadSellingPlots, 20L);
    }

    public SellingPlot getSellingPlot(Plot plot) {
        for (SellingPlot sellingPlot : dataCache.getSellingPlots()) {
            if (!sellingPlot.getPlot().getId().equals(plot.getId())) continue;

            return sellingPlot;
        }

        return null;
    }

    public Boolean isSelling(Plot plot) {
        for (SellingPlot sellingPlot : dataCache.getSellingPlots()) {
            if (!sellingPlot.getPlot().getId().equals(plot.getId())) continue;

            return true;
        }

        return false;
    }

    public void cancelSelling(SellingPlot sellingPlot) {
        dataCache.getSellingPlots().remove(sellingPlot);
        dataCache.getFinishedSellings().add(sellingPlot);
    }

    public void delete(SellingPlot sellingPlot) {
        DBConnection.getInstance().getDBManager().deleteSellingPlot(sellingPlot);
    }

    public void save(SellingPlot sellingPlot) {
        if (!sellingPlot.isQueueUpdate()) return;

        DBConnection.getInstance().getDBManager().saveSellingPlot(sellingPlot);
        sellingPlot.setUpdate(false);
    }

    public void saveAll() {
        new HashSet<>(dataCache.getFinishedSellings()).forEach(this::delete);

        dataCache.getFinishedSellings().clear();

        new HashSet<>(dataCache.getSellingPlots()).forEach(this::save);
    }

    private void loadSellingPlots() {
        dataCache.setSellingPlots(DBConnection.getInstance().getDBManager().getSellingPlots());
    }

    public DataCache getCache() {
        return dataCache;
    }
}