package com.mcsimonflash.sponge.activetime.objects;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.managers.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigWrapper {

    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode node;

    public ConfigWrapper(Path path, boolean asset) throws IOException {
        loader = Util.getLoader(path, asset);
        node = loader.load();
    }

    public CommentedConfigurationNode getNode(Object... path) {
        return node.getNode(path);
    }

    public boolean save() {
        try {
            loader.save(node);
            return true;
        } catch (IOException e) {
            ActiveTime.getPlugin().getLogger().error("Unable to save config file! Error:");
            e.printStackTrace();
            return false;
        }
    }
}
