package com.magicpowered.rainbowskull;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;

public class FileManager {
    private final RainbowSkull plugin;
    private FileConfiguration config;
    private FileConfiguration database;
    private FileConfiguration skull;
    private FileConfiguration variable;

    private File configFile;
    private File databaseFile;
    private File variableFile;
    private File skullFile;

    public FileManager(RainbowSkull plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            config.setDefaults(defaultConfig);
        }

        if (databaseFile == null) {
            databaseFile = new File(plugin.getDataFolder(), "database.yml");
        }
        database = YamlConfiguration.loadConfiguration(databaseFile);

        if (skullFile == null) {
            skullFile = new File(plugin.getDataFolder(), "skull.yml");
        }
        skull = YamlConfiguration.loadConfiguration(skullFile);

        if (variableDatabaseYAML()) {
            if (variableFile == null) {
                variableFile = new File(plugin.getDataFolder(), "variableData.yml");
            }
            variable = YamlConfiguration.loadConfiguration(variableFile);
        }

    }

    public boolean variableDatabaseYAML() {
        String database = getConfig().getString("Variable.databaseSettings", "YAML");
        if (database.equals("YAML")) return true;
        return false;
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public FileConfiguration getDatabase() {
        if (database == null) {
            reloadConfig();
        }
        return database;
    }

    public FileConfiguration getSkull() {
        if (skull == null) {
            reloadConfig();
        }
        return skull;
    }

    public FileConfiguration getVariable() {
        if (variable == null) {
            reloadConfig();
        }
        return variable;
    }


    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void saveDatabase() {
        if (database == null || databaseFile == null) {
            return;
        }
        try {
            getDatabase().save(databaseFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + databaseFile, ex);
        }
    }

    public void saveSkull() {
        if (skull == null || skullFile == null) {
            return;
        }
        try {
            getSkull().save(skullFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save skull to " + skullFile, ex);
        }
    }

    public void saveVariable() {
        if (variable == null || variableFile == null) {
            return;
        }
        try {
            getVariable().save(variableFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save skull to " + variableFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        if (databaseFile == null) {
            databaseFile = new File(plugin.getDataFolder(), "database.yml");
        }
        if (!databaseFile.exists()) {
            plugin.saveResource("database.yml", false);
        }
        if (skullFile == null) {
            skullFile = new File(plugin.getDataFolder(), "skull.yml");
        }
        if (!skullFile.exists()) {
            plugin.saveResource("skull.yml", false);
        }

        if (variableDatabaseYAML()) {
            if (variableFile == null) {
                variableFile = new File(plugin.getDataFolder(), "variableData.yml");
            }
            if (!variableFile.exists()) {
                plugin.saveResource("variableData.yml", false);
            }
        }
    }



    // ########################################################################################################################
                                               // 文件初始化、重载和存储部分已结束
    // ########################################################################################################################

    public void switchPlayerinfoMode(Player player, String mode) {
        if (!mode.equals("chat") && !mode.equals("title") && !mode.equals("subtitle") && !mode.equals("actionbar") && !mode.equals("complete")) {
            return;
        }
        FileConfiguration db = getDatabase();
        db.set("Player." + player.getUniqueId() + ".info", mode);
        player.sendMessage(getMessage("infoModeSwitched").replace("%rainbowskull_info_mode%", mode));
        saveDatabase();
    }

    public String getInfoMode(Player player) {
        return getDatabase().getString("Player." + player.getUniqueId() + ".info", "subtitle");
    }

    public void switchPlayerCheckMode(Player player, Boolean mode) {
        FileConfiguration db = getDatabase();
        db.set("Player." + player.getUniqueId() + ".checkMode", mode);
        String state = mode ? "开启" : "关闭";
        player.sendMessage(getMessage("checkModeSwitched").replace("%rainbowskull_check_mode%", state));
        saveDatabase();
    }

    public void switchPlayerBeheadMode(Player player, Boolean mode) {
        FileConfiguration db = getDatabase();
        db.set("Player." + player.getUniqueId() + ".beheadMode", mode);
        String state = mode ? "会" : "不会";
        player.sendMessage(getMessage("beheadModeSwitched").replace("%rainbowskull_behead_mode%", state));
        saveDatabase();
    }

    public Boolean getCheckMode(Player player) {
        return getDatabase().getBoolean("Player." + player.getUniqueId() + ".checkMode", false);
    }

    public boolean getBeheadMode(Player player) {
        return getDatabase().getBoolean("Player." + player.getUniqueId() + ".beheadMode", true);
    }


    // ########################################################################################################################
                                             // 玩家设置获取部分已结束
    // ########################################################################################################################

    public String getWorldName(World world) {
            // 使用默认设置或直接获取世界名
            String worldName = getConfig().getString("World.alias." + world.getName());
            if (worldName == null) worldName = world.getName();
            return worldName;
    }


    // ########################################################################################################################
                                             // 插件设置获取部分已结束
    // ########################################################################################################################

    public String getMessage(String key) {
        String rawPrefix = getConfig().getString("Message.prefix", "§7[§c彩虹头颅§7]");
        String rawMessage = getConfig().getString("Message." + key, "对于 " + key + " 事件的消息未设置");
        String message = (rawPrefix + " " + rawMessage).replace("&", "§");
        return message;
    }

    public String getPlaceAndBreakMessage(String key) {
        String rawPrefix = getConfig().getString("PlaceAndBreak.prefix", "§7[§c彩虹头颅§7]");
        String rawMessage = getConfig().getString("PlaceAndBreak." + key, "对于 " + key + " 事件的消息未设置");
        String message = (rawPrefix + " " + rawMessage).replace("&", "§");
        return message;
    }
    public void sendMessage(Player player, String key) {
        player.sendMessage(getMessage(key));
    }

    public void sendPABMessage(Player player, String key) {
        if (getConfig().getBoolean("PlaceAndBreak.state", false)){
            player.sendMessage(getPlaceAndBreakMessage(key));
        }
    }

    private static final List<String> MODES = Arrays.asList("chat", "title", "subtitle", "actionbar");
    private static final String DEFAULT_ERROR_MESSAGE = "§7[§c彩虹头颅§7] 错误: 插件内部配置文件错误，请联系服务器管理员，并告知他们原因: config.yml:Info";

    public String replacePlaceholders(String original, String entity) {
        return original.replace("%rainbowskull_killed_name%", entity).replace("&", "§");
    }

    public Map<String, String> getCompleteMessages(String entity) {
        String title = replacePlaceholders(getConfig().getString("Info.complete.title", "未被配置"), entity);
        String subTitle = replacePlaceholders(getConfig().getString("Info.complete.subtitle", "联系管理员并报告: config.yml:Info.complete"), entity);
        Map<String, String> messages = new HashMap<>();
        messages.put("title", title);
        messages.put("subtitle", subTitle);
        return messages;
    }

    public void sendInfo(Player player, String entity) {
        String mode = getInfoMode(player);

        if ("complete".equals(mode)) {
            Map<String, String> messages = getCompleteMessages(entity);
            player.sendTitle(messages.get("title"), messages.get("subtitle"), getTitleFadeinTime(mode), getTitleStayTime(mode), getTitleFadeOutTime(mode));
            return;
        }

        String message = replacePlaceholders(getConfig().getString("Info." + mode, DEFAULT_ERROR_MESSAGE), entity);

        switch (mode) {
            case "chat":
                player.sendMessage(message);
                break;
            case "title":
                player.sendTitle(message, " ", getTitleFadeinTime(mode), getTitleStayTime(mode), getTitleFadeOutTime(mode));
                break;
            case "subtitle":
                player.sendTitle(" ", message, getTitleFadeinTime(mode), getTitleStayTime(mode), getTitleFadeOutTime(mode));
                break;
            case "actionbar":
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                break;
        }
    }


    public void sendBroadCase(String player, String killed, String world) {
        List<String> epicEntities = getConfig().getStringList("Behead.Broadcast.epicMessage.entities");
        List<String> rawMessage;
        boolean whiteList = getConfig().getBoolean("Behead.Broadcast.epicMessage.whiteList", true);


        if (whiteList && epicEntities.contains(killed)) {
            rawMessage = getConfig().getStringList("Behead.Broadcast.epicMessage.message");
        } else if (!whiteList && !epicEntities.contains(killed)) {
            rawMessage = getConfig().getStringList("Behead.Broadcast.epicMessage.message");
        } else {
            rawMessage = getConfig().getStringList("Behead.Broadcast.message");
        }

        String name = getSkull().getString("Skulls." + killed + ".name", killed).replace("&", "§");

        for (String str : rawMessage) {
            str = str.replace("%rainbowskull_killed_name%", name)
                    .replace("%rainbowskull_killed_world%", world)
                    .replace("%rainbowskull_killed_player%", player)
                    .replace("&", "§");
            Bukkit.broadcastMessage(str);
        }
    }

    public void executeScript(String player, String killed, String world) {
        List<String> epicEntities = getConfig().getStringList("Behead.Script.epicCommand.entities");
        List<String> rawCommand;
        boolean whiteList = getConfig().getBoolean("Behead.Script.epicCommand.whiteList", true);

        if (whiteList && epicEntities.contains(killed)) {
            // 如果 whiteList 为 true 且 List 包含 killed
            rawCommand = getConfig().getStringList("Behead.Script.epicCommand.command");
        } else if (!whiteList && !epicEntities.contains(killed)) {
            // 如果 whiteList 为 false 且 List 不包含 killed
            rawCommand = getConfig().getStringList("Behead.Script.epicCommand.command");
        } else {
            rawCommand = getConfig().getStringList("Behead.Script.command");
        }


        String name = getSkull().getString("Skulls." + killed + ".name", killed).replace("&", "§");

        for (String str : rawCommand) {
            str = str.replace("%rainbowskull_killed_name%", name)
                    .replace("%rainbowskull_killed_world%", world)
                    .replace("%rainbowskull_killed_player%", player)
                    .replace("&", "§");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str);
        }

    }


    public void sendCheckMessage(Player player, EntityType entity, String key) {
        if (getCheckMode(player)) {
            String rawName = getSkull().getString("Skulls." + entity.name().toUpperCase() + ".name");
            String name = (rawName != null) ? rawName.replace("&", "§") : entity.name();
            player.sendMessage(getMessage(key).replace("%rainbowskull_check_entity%", name));
        }
    }



    public int getTitleFadeinTime(String type) {
        return getConfig().getInt("InfoTitleSettings." + type + ".fadeIn", 10);
    }

    public int getTitleStayTime(String type) {
        return getConfig().getInt("InfoTitleSettings." + type + ".stay", 70);
    }

    public int getTitleFadeOutTime(String type) {
        return getConfig().getInt("InfoTitleSettings." + type + ".fadeOut", 20);
    }

    // ########################################################################################################################
                                            // 消息获取、发送部分已结束
    // ########################################################################################################################

    public void saveSkullData(String locationKey, String displayName, List<String> lore) {
        FileConfiguration database = getDatabase();

        // 保存 DisplayName 和 Lore 到指定的位置键
        database.set("Skulls." + locationKey + ".DisplayName", displayName);
        database.set("Skulls." + locationKey + ".Lore", lore);

        // 保存修改后的数据库
        saveDatabase();
    }

    public SkullData getSkullData(String locationKey) {
        FileConfiguration database = getDatabase();

        // 从数据库中获取 DisplayName 和 Lore
        String displayName = database.getString("Skulls." + locationKey + ".DisplayName");
        List<String> lore = database.getStringList("Skulls." + locationKey + ".Lore");

        return new SkullData(displayName, lore);
    }

    public void removeSkullData(String locationKey) {
        FileConfiguration database = getDatabase();

        // 从数据库中删除指定位置的头颅数据
        database.set("Skulls." + locationKey, null);

        // 保存修改后的数据库
        saveDatabase();
    }

    public boolean shouldDecapitate(Player player, EntityType entityType, World world) {
        double probability1 = getSkull().getDouble("Skulls." + entityType.name() + ".probability");
        double probability2 = getConfig().getDouble("World.additional." + world.getName());
        double probability = probability1 + probability2;

        // 检查玩家权限并增加额外的几率
        for (String permission : getConfig().getConfigurationSection("AdditionalPermission").getKeys(false)) {
            if (player.hasPermission("rainbowskull.additional." + permission)) {
                probability += getConfig().getDouble("AdditionalPermission." + permission);
            }
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon != null && weapon.hasItemMeta() && weapon.getItemMeta().hasEnchants()) {
            Map<Enchantment, Integer> enchants = weapon.getItemMeta().getEnchants();
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                probability += getConfig().getDouble("AdditionalEnchant." + enchantment.getKey().getKey() + "." + level, 0);
            }
        }

        probability /= 100.0;

        if (probability > 1) {
            probability = 1;
            Bukkit.getServer().getLogger().warning("[彩虹头颅] 错误, 生物 " + entityType + " 的斩首概率大于 100% (100)，本次已处理为 100，请注意");
        }

        return new Random().nextDouble() < probability;
    }


}