package com.zpedroo.plotsquared.utils.config;

import com.zpedroo.plotsquared.utils.FileUtils;

import java.util.List;

public class Settings {

    public static final List<String> COMMANDS = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.commands");
}