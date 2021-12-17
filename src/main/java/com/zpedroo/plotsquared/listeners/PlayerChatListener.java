package com.zpedroo.plotsquared.listeners;

import com.intellectualcrafters.plot.object.Plot;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.plotsquared.PlotSquaredAddon;
import com.zpedroo.plotsquared.managers.DataManager;
import com.zpedroo.plotsquared.objects.SellingPlot;
import com.zpedroo.plotsquared.utils.config.Messages;
import com.zpedroo.plotsquared.utils.menus.Menus;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.math.BigInteger;
import java.util.*;

public class PlayerChatListener implements Listener {

    private static Map<Player, PlayerChat> playerChat;

    static {
        playerChat = new HashMap<>(8);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!playerChat.containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        PlayerChat playerChat = getPlayerChat().remove(player);

        ChatAction action = playerChat.getAction();
        Plot plot = playerChat.getPlot();
        if (plot != null && !plot.getOwners().contains(player.getUniqueId())) return;

        OfflinePlayer target = null;

        BigInteger price = null;
        SellingPlot sellingPlot = null;
        switch (action) {
            case EDIT_PRICE:
                price = NumberFormatter.getInstance().filter(event.getMessage());
                if (price.signum() <= 0) {
                    player.sendMessage(Messages.INVALID_PRICE);
                    return;
                }

                sellingPlot = playerChat.getSellingPlot();
                sellingPlot.setPrice(price);

                final SellingPlot finalSellingPlot1 = sellingPlot;
                PlotSquaredAddon.get().getServer().getScheduler().runTaskLater(PlotSquaredAddon.get(), () -> Menus.getInstance().openSellingManagerMenu(player, finalSellingPlot1), 0L);
                break;
            case SELECT_PRICE:
                price = NumberFormatter.getInstance().filter(event.getMessage());
                if (price.signum() <= 0) {
                    player.sendMessage(Messages.INVALID_PRICE);
                    return;
                }

                Currency currency = playerChat.getCurrency();
                sellingPlot = new SellingPlot(plot, price, currency);
                sellingPlot.setUpdate(true);

                DataManager.getInstance().getCache().getSellingPlots().add(sellingPlot);

                final SellingPlot finalSellingPlot = sellingPlot;
                PlotSquaredAddon.get().getServer().getScheduler().runTaskLater(PlotSquaredAddon.get(), () -> Menus.getInstance().openSellingManagerMenu(player, finalSellingPlot), 0L);
                break;
            case ADD_MEMBER:
                target = Bukkit.getOfflinePlayer(event.getMessage());
                if (!target.hasPlayedBefore()) {
                    player.sendMessage(Messages.NEVER_SEEN);
                    return;
                }

                if (plot.getMembers().contains(target.getUniqueId())) {
                    player.sendMessage(Messages.ALREADY_ADDED);
                    return;
                }

                plot.addMember(target.getUniqueId());
                PlotSquaredAddon.get().getServer().getScheduler().runTaskLater(PlotSquaredAddon.get(), () -> Menus.getInstance().openMembersMenu(player, plot), 0L);
                break;
            case DENY_PLAYER:
                target = Bukkit.getOfflinePlayer(event.getMessage());
                if (!target.hasPlayedBefore()) {
                    player.sendMessage(Messages.NEVER_SEEN);
                    return;
                }

                if (plot.getDenied().contains(target.getUniqueId())) {
                    player.sendMessage(Messages.ALREADY_DENIED);
                    return;
                }

                plot.addDenied(target.getUniqueId());
                PlotSquaredAddon.get().getServer().getScheduler().runTaskLater(PlotSquaredAddon.get(), () -> Menus.getInstance().openDeniedMenu(player, plot), 0L);
                break;
        }
    }

    public static Map<Player, PlayerChat> getPlayerChat() {
        return playerChat;
    }

    public static class PlayerChat {

        private ChatAction action;
        private Plot plot;
        private SellingPlot sellingPlot;
        private Currency currency;

        public PlayerChat(ChatAction action, Plot plot, SellingPlot sellingPlot, Currency currency) {
            this.action = action;
            this.plot = plot;
            this.sellingPlot = sellingPlot;
            this.currency = currency;
        }

        public ChatAction getAction() {
            return action;
        }

        public Plot getPlot() {
            return plot;
        }

        public SellingPlot getSellingPlot() {
            return sellingPlot;
        }

        public Currency getCurrency() {
            return currency;
        }
    }

    public enum ChatAction {
        ADD_MEMBER,
        DENY_PLAYER,
        SELECT_PRICE,
        EDIT_PRICE
    }
}