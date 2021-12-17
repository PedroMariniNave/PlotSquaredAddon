package com.zpedroo.plotsquared.utils.menus;

import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.plotsquared.listeners.PlayerChatListener;
import com.zpedroo.plotsquared.managers.DataManager;
import com.zpedroo.plotsquared.objects.SellingPlot;
import com.zpedroo.plotsquared.utils.FileUtils;
import com.zpedroo.plotsquared.utils.builder.InventoryBuilder;
import com.zpedroo.plotsquared.utils.builder.InventoryUtils;
import com.zpedroo.plotsquared.utils.builder.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.zpedroo.plotsquared.utils.config.Messages.*;

public class Menus extends InventoryUtils {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private ItemStack nextPageItem;
    private ItemStack previousPageItem;

    public Menus() {
        instance = this;
        this.nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();
        this.previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();
    }

    public void openMainMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{plotmes}",
                    "{selling_plotmes}"
            }, new String[]{
                    String.valueOf(PlotPlayer.wrap(player).getPlots().size()),
                    String.valueOf(DataManager.getInstance().getCache().getSellingPlots().size())
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "PLAYER_PLOTMES":
                        openPlayerPlotmesMenu(player);
                        break;
                    case "SELLING_PLOTMES":
                        openSellingPlotmesMenu(player);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openPlayerPlotmesMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.PLAYER_PLOTMES;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        int i = -1;
        int id = 0;
        String[] slots = FileUtils.get().getString(file, "Inventory.item-slots").replace(" ", "").split(",");

        List<Plot> playerPlots = new ArrayList<>(PlotPlayer.wrap(player).getPlots());
        playerPlots.sort(Comparator.comparing(Plot::getTimestamp));

        if (playerPlots.isEmpty()) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Nothing").build();
            int slot = FileUtils.get().getInt(file, "Nothing.slot");

            inventory.addItem(item, slot);
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (Plot plot : playerPlots) {
                if (plot.getConnectedPlots().size() > 1) {
                    if (!plot.getConnectedPlots().iterator().next().getId().equals(plot.getId())) continue; // only show first plot
                }
                if (++i >= slots.length) i = 0;

                SellingPlot sellingPlot = DataManager.getInstance().getSellingPlot(plot);
                String toGet = sellingPlot == null ? "Item" : "Selling-Item";
                ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), toGet, new String[]{
                        "{id}",
                        "{members}",
                        "{denied}",
                        "{merge}",
                        "{timestamp}",
                        "{price}"
                }, new String[]{
                        String.valueOf(++id),
                        String.valueOf(plot.getMembers().size()),
                        String.valueOf(plot.getDenied().size()),
                        String.valueOf(plot.getConnectedPlots().size()),
                        formatter.format(plot.getTimestamp()),
                        sellingPlot == null ? "0" :
                                StringUtils.replaceEach(sellingPlot.getCurrency().getAmountDisplay(), new String[]{ "{amount}" }, new String[]{ NumberFormatter.getInstance().format(sellingPlot.getPrice()) })
                }).build();
                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot, () -> {
                    plot.teleportPlayer(PlotPlayer.wrap(player));
                }, ActionType.LEFT_CLICK);

                inventory.addAction(slot, () -> {
                    openPlotmeManagerMenu(player, plot);
                }, ActionType.RIGHT_CLICK);

                if (sellingPlot != null) {
                    inventory.addAction(slot, () -> {
                        openSellingManagerMenu(player, sellingPlot);
                    }, ActionType.SCROLL);
                }
            }
        }

        ItemStack claimPlotmeItem = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Claim-Plotme").build();
        int claimPlotmeSlot = FileUtils.get().getInt(file, "Claim-Plotme.slot");

        inventory.addDefaultItem(claimPlotmeItem, claimPlotmeSlot, () -> {
            player.chat("/plotme auto");
        }, ActionType.ALL_CLICKS);

        inventory.open(player);
    }

    public void openPlotmeManagerMenu(Player player, Plot plot) {
        FileUtils.Files file = FileUtils.Files.PLOTME_MANAGER;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{members}",
                    "{denied}"
            }, new String[]{
                    String.valueOf(plot.getMembers().size()),
                    String.valueOf(plot.getDenied().size())
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "MEMBERS":
                        openMembersMenu(player, plot);
                        break;
                    case "DENIED":
                        openDeniedMenu(player, plot);
                        break;
                    case "SELL_PLOTME":
                        if (DataManager.getInstance().isSelling(plot)) {
                            SellingPlot sellingPlot = DataManager.getInstance().getSellingPlot(plot);

                            openSellingManagerMenu(player, sellingPlot);
                            return;
                        }

                        openSelectCurrencyMenu(player, plot, null);
                        break;
                    case "SET_HOME":
                        PlotPlayer plotPlayer = PlotPlayer.wrap(player);
                        Plot playerPlot = plotPlayer.getCurrentPlot();
                        if (playerPlot == null || playerPlot.getId() != plot.getId()) {
                            player.sendMessage(OUTSIDE_PLOT);
                            return;
                        }

                        Location bottomAbs = playerPlot.getBottomAbs();
                        Location playerLocation = plotPlayer.getLocationFull();
                        BlockLoc blockLoc = new BlockLoc(playerLocation.getX() - bottomAbs.getX(), playerLocation.getY(), playerLocation.getZ() - bottomAbs.getZ(), playerLocation.getYaw(), playerLocation.getPitch());

                        playerPlot.setHome(blockLoc);

                        player.sendMessage(SUCCESSFUL_SET_HOME);
                        inventory.close(player);
                        break;
                    case "DISPOSE":
                        openConfirmDispose(player, plot);
                        break;
                    case "CLEAR":
                        openConfirmClear(player, plot);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openSelectCurrencyMenu(Player player, Plot plot, SellingPlot sellingPlot) {
        FileUtils.Files file = FileUtils.Files.SELECT_CURRENCY;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            String currencyName = FileUtils.get().getString(file, "Inventory.items." + str + ".currency");
            Currency currency = CurrencyAPI.getCurrency(currencyName);

            inventory.addItem(item, slot, () -> {
                if (currency == null) return;
                if (sellingPlot != null) {
                    sellingPlot.setCurrency(currency);
                    openSellingManagerMenu(player, sellingPlot);
                    return;
                }

                PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(PlayerChatListener.ChatAction.SELECT_PRICE, plot, null, currency));

                for (int i = 0; i < 25; ++i) {
                    player.sendMessage("");
                }

                for (String msg : SELECT_PRICE) {
                    player.sendMessage(msg);
                }

                inventory.close(player);
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openMembersMenu(Player player, Plot plot) {
        FileUtils.Files file = FileUtils.Files.MEMBERS;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        HashSet<UUID> members = plot.getMembers();

        int i = -1;
        String[] slots = FileUtils.get().getString(file, "Inventory.item-slots").replace(" ", "").split(",");
        if (members.isEmpty()) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Nothing").build();
            int slot = FileUtils.get().getInt(file, "Nothing.slot");

            inventory.addItem(item, slot);
        } else {
            for (UUID uuid : members) {
                if (++i >= slots.length) i = 0;

                OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
                ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Item", new String[]{
                        "{player}",
                        "{status}",
                        "{trusted}"
                }, new String[]{
                        member.getName(),
                        member.isOnline() ? ONLINE : OFFLINE,
                        plot.getTrusted().contains(uuid) ? TRUSTED : UNTRUSTED
                }).build();
                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot, () -> {
                    plot.removeMember(uuid);
                    openMembersMenu(player, plot);
                }, ActionType.LEFT_CLICK);

                inventory.addAction(slot, () -> {
                    if (plot.getTrusted().contains(uuid)) {
                        plot.removeTrusted(uuid);
                    } else {
                        plot.addTrusted(uuid);
                    }

                    openMembersMenu(player, plot);
                }, ActionType.RIGHT_CLICK);
            }
        }

        ItemStack addMemberItem = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Add-Member").build();
        int addMemberSlot = FileUtils.get().getInt(file, "Add-Member.slot");

        inventory.addDefaultItem(addMemberItem, addMemberSlot, () -> {
            PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(PlayerChatListener.ChatAction.ADD_MEMBER, plot, null, null));

            for (int j = 0; j < 25; ++j) {
                player.sendMessage("");
            }

            for (String msg : TYPE_NICK) {
                player.sendMessage(msg);
            }

            player.closeInventory();
        }, ActionType.ALL_CLICKS);

        inventory.open(player);
    }

    public void openDeniedMenu(Player player, Plot plot) {
        FileUtils.Files file = FileUtils.Files.DENIED;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        HashSet<UUID> denied = plot.getDenied();

        int i = -1;
        String[] slots = FileUtils.get().getString(file, "Inventory.item-slots").replace(" ", "").split(",");
        if (denied.isEmpty()) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Nothing").build();
            int slot = FileUtils.get().getInt(file, "Nothing.slot");

            inventory.addItem(item, slot);
        } else {
            for (UUID uuid : denied) {
                if (++i >= slots.length) i = 0;

                OfflinePlayer deniedPlayer = Bukkit.getOfflinePlayer(uuid);
                ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Item", new String[]{
                        "{player}",
                        "{status}"
                }, new String[]{
                        deniedPlayer.getName(),
                        deniedPlayer.isOnline() ? ONLINE : OFFLINE
                }).build();
                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot, () -> {
                    plot.removeDenied(uuid);
                    openDeniedMenu(player, plot);
                }, ActionType.ALL_CLICKS);
            }
        }

        ItemStack denyPlayerItem = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Deny-Player").build();
        int denyPlayerSlot = FileUtils.get().getInt(file, "Deny-Player.slot");

        inventory.addDefaultItem(denyPlayerItem, denyPlayerSlot, () -> {
            PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(PlayerChatListener.ChatAction.DENY_PLAYER, plot, null, null));

            for (int j = 0; j < 25; ++j) {
                player.sendMessage("");
            }

            for (String msg : TYPE_NICK) {
                player.sendMessage(msg);
            }

            player.closeInventory();
        }, ActionType.ALL_CLICKS);

        inventory.open(player);
    }

    public void openSellingPlotmesMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.SELLING_PLOTMES;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        int i = -1;
        int id = 0;
        String[] slots = FileUtils.get().getString(file, "Inventory.item-slots").replace(" ", "").split(",");

        List<SellingPlot> sellingPlots = DataManager.getInstance().getCache().getSellingPlots();
        if (sellingPlots.isEmpty()) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Nothing").build();
            int slot = FileUtils.get().getInt(file, "Nothing.slot");

            inventory.addItem(item, slot);
        } else {
            for (SellingPlot sellingPlot : sellingPlots) {
                if (sellingPlot.getPlot() == null || sellingPlot.getPlot().getOwners() == null || sellingPlot.getPlot().getOwners().isEmpty()) {
                    DataManager.getInstance().cancelSelling(sellingPlot);
                    continue;
                }
                if (++i >= slots.length) i = 0;

                String toGet = sellingPlot.getPlot().getOwners().contains(player.getUniqueId()) ? "Owner-Item" : "Item";
                ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), toGet, new String[]{
                        "{id}",
                        "{merge}",
                        "{owner}",
                        "{price}"
                }, new String[]{
                        String.valueOf(++id),
                        String.valueOf(sellingPlot.getPlot().getConnectedPlots().size()),
                        Bukkit.getOfflinePlayer(sellingPlot.getPlot().getOwners().stream().findFirst().get()).getName(),
                        StringUtils.replaceEach(sellingPlot.getCurrency().getAmountDisplay(), new String[]{ "{amount}" }, new String[]{ NumberFormatter.getInstance().format(sellingPlot.getPrice()) })
                }).build();
                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot);

                if (StringUtils.equals(toGet, "Owner-Item")) {
                    inventory.addAction(slot, () -> {
                        openSellingManagerMenu(player, sellingPlot);
                    }, ActionType.ALL_CLICKS);
                } else {
                    inventory.addAction(slot, () -> {
                        if (!DataManager.getInstance().isSelling(sellingPlot.getPlot())) {
                            inventory.close(player);
                            player.sendMessage(INVALID_SELLING);
                            return;
                        }

                        openConfirmPurchase(player, sellingPlot);
                    }, ActionType.LEFT_CLICK);

                    inventory.addAction(slot, () -> {
                        sellingPlot.getPlot().teleportPlayer(PlotPlayer.wrap(player));
                    }, ActionType.RIGHT_CLICK);
                }
            }
        }

        inventory.open(player);
    }

    public void openSellingManagerMenu(Player player, SellingPlot sellingPlot) {
        FileUtils.Files file = FileUtils.Files.SELLING_MANAGER;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{price}",
                    "{currency}"
            }, new String[]{
                    StringUtils.replaceEach(sellingPlot.getCurrency().getAmountDisplay(), new String[]{"{amount}"}, new String[]{NumberFormatter.getInstance().format(sellingPlot.getPrice())}),
                    sellingPlot.getCurrency().getDisplay()
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "EDIT_PRICE":
                        PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(PlayerChatListener.ChatAction.EDIT_PRICE, sellingPlot.getPlot(), sellingPlot, null));

                        for (int i = 0; i < 25; ++i) {
                            player.sendMessage("");
                        }

                        for (String msg : SELECT_PRICE) {
                            player.sendMessage(msg);
                        }

                        inventory.close(player);
                        break;
                    case "EDIT_CURRENCY":
                        openSelectCurrencyMenu(player, sellingPlot.getPlot(), sellingPlot);
                        break;
                    case "CANCEL_SELLING":
                        inventory.close(player);
                        DataManager.getInstance().cancelSelling(sellingPlot);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openConfirmPurchase(Player player, SellingPlot sellingPlot) {
        FileUtils.Files file = FileUtils.Files.CONFIRM_PURCHASE;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{merge}",
                    "{owner}",
                    "{price}"
            }, new String[]{
                    String.valueOf(sellingPlot.getPlot().getConnectedPlots().size()),
                    Bukkit.getOfflinePlayer(sellingPlot.getPlot().getOwners().stream().findFirst().get()).getName(),
                    StringUtils.replaceEach(sellingPlot.getCurrency().getAmountDisplay(), new String[]{ "{amount}" }, new String[]{ NumberFormatter.getInstance().format(sellingPlot.getPrice()) })
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "CONFIRM":
                        inventory.close(player);
                        if (!DataManager.getInstance().isSelling(sellingPlot.getPlot())) {
                            player.sendMessage(INVALID_SELLING);
                            return;
                        }

                        Currency currency = sellingPlot.getCurrency();
                        BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(player, currency);
                        BigInteger price = sellingPlot.getPrice();

                        if (currencyAmount.compareTo(price) < 0) {
                            player.sendMessage(StringUtils.replaceEach(INSUFFICIENT_CURRENCY, new String[]{
                                    "{has}",
                                    "{need}"
                            }, new String[]{
                                    NumberFormatter.getInstance().format(currencyAmount),
                                    NumberFormatter.getInstance().format(price)
                            }));
                            return;
                        }

                        Plot plot = sellingPlot.getPlot();
                        PlotPlayer plotPlayer = PlotPlayer.wrap(player);
                        int allowedPlots = plotPlayer.getAllowedPlots();
                        int currentPlots = plotPlayer.getPlotCount();
                        int availablePlots = allowedPlots - currentPlots;

                        if (availablePlots < plot.getConnectedPlots().size()) {
                            player.sendMessage(PLOT_LIMIT);
                            return;
                        }

                        CurrencyAPI.removeCurrencyAmount(player, currency, price);
                        CurrencyAPI.addCurrencyAmount(Bukkit.getOfflinePlayer(plot.getOwners().stream().findFirst().get()), currency, price);

                        plot.setOwner(player.getUniqueId());
                        plot.getMembers().clear();
                        plot.getDenied().clear();

                        plot.teleportPlayer(plotPlayer);

                        DataManager.getInstance().cancelSelling(sellingPlot);
                        break;
                    case "CANCEL":
                        inventory.close(player);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openConfirmDispose(Player player, Plot plot) {
        FileUtils.Files file = FileUtils.Files.CONFIRM_DISPOSE;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{members}",
                    "{denied}",
                    "{merge}",
                    "{timestamp}"
            }, new String[]{
                    String.valueOf(plot.getMembers().size()),
                    String.valueOf(plot.getDenied().size()),
                    String.valueOf(plot.getConnectedPlots().size()),
                    formatter.format(plot.getTimestamp())
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "CONFIRM":
                        inventory.close(player);
                        if (!plot.getOwners().contains(player.getUniqueId())) return;
                        if (DataManager.getInstance().isSelling(plot)) {
                            player.sendMessage(SELLING_PLOT);
                            return;
                        }

                        plot.deletePlot(() -> {});
                        break;
                    case "CANCEL":
                        inventory.close(player);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openConfirmClear(Player player, Plot plot) {
        FileUtils.Files file = FileUtils.Files.CONFIRM_CLEAR;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{members}",
                    "{denied}",
                    "{merge}",
                    "{timestamp}"
            }, new String[]{
                    String.valueOf(plot.getMembers().size()),
                    String.valueOf(plot.getDenied().size()),
                    String.valueOf(plot.getConnectedPlots().size()),
                    formatter.format(plot.getTimestamp())
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "CONFIRM":
                        inventory.close(player);
                        if (!plot.getOwners().contains(player.getUniqueId())) return;
                        if (DataManager.getInstance().isSelling(plot)) {
                            player.sendMessage(SELLING_PLOT);
                            return;
                        }

                        plot.clear(() -> {});
                        break;
                    case "CANCEL":
                        inventory.close(player);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }
}