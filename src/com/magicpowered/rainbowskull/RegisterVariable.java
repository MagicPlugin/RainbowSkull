package com.magicpowered.rainbowskull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class RegisterVariable extends PlaceholderExpansion {

    private Plugin plugin;
    private VariableStorage variableStorage;

    public RegisterVariable(Plugin plugin, VariableStorage variableStorage) {
        this.plugin = plugin;
        this.variableStorage = variableStorage;
    }

    @Override
    public boolean persist() {
        // 这个方法返回 true，确保占位符不会因插件重载而失效
        return true;
    }

    @Override
    public String getIdentifier() {
        return "rainbowskull";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        // 检查玩家是否在线
        // TODO: 兼容查询未在线玩家
        if (player == null) {
            return "";
        }

        // 处理斩首特定实体的数量占位符
        if (identifier.startsWith("num_")) {
            String entityName = identifier.split("_")[1];
            // 获取和返回斩首特定实体的次数
            return getDecapitationsOfEntity(player.getUniqueId(), entityName);
        }

        // 处理总斩首数量的占位符
        if (identifier.equals("num_total")) {
            // 获取和返回总斩首次数
            return getTotalDecapitations(player.getUniqueId());
        }

        return null;
    }

    private String getDecapitationsOfEntity(UUID playerUUID, String entityName) {
        int decapitations = variableStorage.getDecapitations(playerUUID, entityName);
        return String.valueOf(decapitations);
    }

    private String getTotalDecapitations(UUID playerUUID) {
        int totalDecapitations = variableStorage.getDecapitations(playerUUID, "total");
        return String.valueOf(totalDecapitations);
    }

}
