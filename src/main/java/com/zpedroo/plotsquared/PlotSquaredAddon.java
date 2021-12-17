package com.zpedroo.plotsquared;

import com.zpedroo.plotsquared.listeners.CommandListeners;
import com.zpedroo.plotsquared.listeners.PlayerChatListener;
import com.zpedroo.plotsquared.listeners.PlotListeners;
import com.zpedroo.plotsquared.managers.DataManager;
import com.zpedroo.plotsquared.mysql.DBConnection;
import com.zpedroo.plotsquared.utils.FileUtils;
import com.zpedroo.plotsquared.utils.menus.Menus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PlotSquaredAddon extends JavaPlugin {

    private static PlotSquaredAddon instance;
    public static PlotSquaredAddon get() { return instance; }

    public void onEnable() {
        instance = this;
        new FileUtils(this);

        if (!isMySQLEnabled(getConfig())) {
            getLogger().log(Level.SEVERE, "MySQL are disabled! You need to enable it.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new DBConnection(getConfig());
        new DataManager();
        new Menus();

        registerListeners();
    }

    public void onDisable() {
        if (!isMySQLEnabled(getConfig())) return;

        try {
            DataManager.getInstance().saveAll();
            DBConnection.getInstance().closeConnection();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error occurred while trying to save data!");
            ex.printStackTrace();
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CommandListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlotListeners(), this);
    }

    private Boolean isMySQLEnabled(FileConfiguration file) {
        if (!file.contains("MySQL.enabled")) return false;

        return file.getBoolean("MySQL.enabled");
    }
}