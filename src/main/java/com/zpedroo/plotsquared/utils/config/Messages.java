package com.zpedroo.plotsquared.utils.config;

import com.zpedroo.plotsquared.utils.FileUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    public static final String ONLINE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.online"));

    public static final String OFFLINE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.offline"));

    public static final String TRUSTED = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.trusted"));

    public static final String UNTRUSTED = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.untrusted"));

    public static final String SELLING_PLOT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.selling-plot"));

    public static final String OUTSIDE_PLOT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.outside-plot"));

    public static final String SUCCESSFUL_SET_HOME = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.successful-set-home"));

    public static final String NEVER_SEEN = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.never-seen"));

    public static final String ALREADY_ADDED = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.already-added"));

    public static final String ALREADY_DENIED = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.already-denied"));

    public static final String INVALID_PRICE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-price"));

    public static final String INVALID_SELLING = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-selling"));

    public static final String INSUFFICIENT_CURRENCY = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.insufficient-currency"));

    public static final String PLOT_LIMIT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.plot-limit"));

    public static final List<String> SELECT_PRICE = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.select-price"));

    public static final List<String> TYPE_NICK = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.type-nick"));

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private static List<String> getColored(List<String> list) {
        List<String> colored = new ArrayList<>(list.size());
        for (String str : list) {
            colored.add(getColored(str));
        }

        return colored;
    }
}