package com.zpedroo.plotsquared.listeners;

import com.intellectualcrafters.plot.object.Plot;
import com.plotsquared.bukkit.events.PlotClearEvent;
import com.plotsquared.bukkit.events.PlotDeleteEvent;
import com.zpedroo.plotsquared.managers.DataManager;
import com.zpedroo.plotsquared.utils.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlotListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlotClear(PlotClearEvent event) {
        Plot plot = event.getPlot();
        if (!DataManager.getInstance().isSelling(plot)) return;

        event.setCancelled(true);

        Player player = Bukkit.getPlayer(plot.getOwners().stream().findFirst().get());
        if (player == null) return;

        player.sendMessage(Messages.SELLING_PLOT);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlotDelete(PlotDeleteEvent event) {
        Plot plot = event.getPlot();
        if (!DataManager.getInstance().isSelling(plot)) return;

        event.setCancelled(true);

        Player player = Bukkit.getPlayer(plot.getOwners().stream().findFirst().get());
        if (player == null) return;

        player.sendMessage(Messages.SELLING_PLOT);
    }
}