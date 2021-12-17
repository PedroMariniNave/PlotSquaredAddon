package com.zpedroo.plotsquared.listeners;

import com.zpedroo.plotsquared.utils.config.Settings;
import com.zpedroo.plotsquared.utils.menus.Menus;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] split = event.getMessage().split(" ");
        String command = StringUtils.replace(split[0], "/", "");
        if (!Settings.COMMANDS.contains(command.toLowerCase())) return;

        if (split.length < 2) {
            event.setCancelled(true);

            Menus.getInstance().openMainMenu(player);
            return;
        }

        String arg = split[1].toUpperCase();
        String[] argSplit = arg.split(":");

        if (StringUtils.equals(argSplit[0], "H") || StringUtils.equals(argSplit[0], "HOME")) {
            if (split.length < 3) return;

            event.setCancelled(true);

            String target = split[2];
            String plotId = argSplit.length <= 1 ? "" : argSplit[1];
            player.chat("/plotme visit " + target + " " + plotId);
        }
    }
}