package com.magicpowered.rainbowskull;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandListener implements CommandExecutor, TabCompleter {

    private RainbowSkull plugin;
    private FileManager fileManager;
    private Mobs mobs;

    public CommandListener(RainbowSkull plugin, FileManager fileManager, Mobs mobs) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.mobs = mobs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7[§c彩虹头颅§7] 帮助:");
            sender.sendMessage("§7  |- 必要参数: <?>, 可选必要参数: <?/?>, 非必要参数: [?], 可选非必要参数: [?/?]");
            sender.sendMessage("§7  |- §c/rs info <complete/chat/title/subtitle/actionbar> §7- 设置斩首提示的位置");
            sender.sendMessage("§7  |- §c/rs check <true/false> §7- 开启 或 关闭 可以触发灵魂斩首的生物检查");
            sender.sendMessage("§7  |- §c/rs behead <true/false> §7- 开启 或 关闭 灵魂斩首");
            sender.sendMessage("§7  |- §c/rs world §7- 查看各个世界的别名、额外概率和是否启用灵魂斩首");
            sender.sendMessage("§7  |- §c/rs give <playerName> entity <killerName> <entityName> [value] §7- 给予玩家指定数量的生物头颅 (管理员)");
            sender.sendMessage("§7  |- §c/rs give <playerName> player <killerName> <playerName> [value] §7- 给予玩家指定数量的玩家头颅 (管理员)");
            sender.sendMessage("§7  |- §c/rs reload §7- 重新载入配置文件 (管理员)");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                if (!(sender instanceof Player)) {
                    Bukkit.getServer().getLogger().info("[彩虹头颅] 此命令必须由一个玩家执行");
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 参数过多或缺少必要参数");
                    sender.sendMessage("§7  |用法- §c/rs info <complete/chat/title/subtitle/actionbar>");
                    sender.sendMessage("§7  |示例- §c/rs info complete");
                    return true;
                }
                if (!args[1].equals("title") && !args[1].equals("chat") && !args[1].equals("actionbar") && !args[1].equals("complete") && !args[1].equals("subtitle")) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 参数 " + args[1] + "不在可选区间内: <complete/title/subtitle/chat/actionbar>");
                    return true;
                }

                fileManager.switchPlayerinfoMode(((Player) sender).getPlayer(), args[1]);
                break;

            case "check":
                if (!(sender instanceof Player)) {
                    Bukkit.getServer().getLogger().info("[彩虹头颅] 此命令必须由一个玩家执行");
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 参数过多或缺少必要参数");
                    sender.sendMessage("§7  |用法- §c/rs check <true/false>");
                    sender.sendMessage("§7  |示例- §c/rs check true");
                    return true;
                }
                if (!sender.hasPermission("rainbowskull.check")) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 您没有执行此命令的权限");
                    return true;
                }

                if (!"true".equalsIgnoreCase(args[1]) && !"false".equalsIgnoreCase(args[1])) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 参数 " + args[1] + "不是布尔值: <true/false>");
                    return true;
                }

                boolean checkMode = Boolean.parseBoolean(args[1]);
                fileManager.switchPlayerCheckMode((Player) sender, checkMode);
                break;

            case "behead":
                if (!(sender instanceof Player)) {
                    Bukkit.getServer().getLogger().info("[彩虹头颅] 此命令必须由一个玩家执行");
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 参数过多或缺少必要参数");
                    sender.sendMessage("§7  |用法- §c/rs behead <true/false>");
                    sender.sendMessage("§7  |示例- §c/rs behead true");
                    return true;
                }
                if (!sender.hasPermission("rainbowskull.behead")) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 您没有执行此命令的权限");
                    return true;
                }

                if (!args[1].equalsIgnoreCase("true") && !args[1].equalsIgnoreCase("false")) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 参数 " + args[1] + "不是布尔值: <true/false>");
                    return true;
                }

                boolean beheadMode = Boolean.parseBoolean(args[1]);
                fileManager.switchPlayerBeheadMode((Player) sender, beheadMode);
                break;

            case "give":
                if (!(sender instanceof Player)) {
                    Bukkit.getServer().getLogger().info("[彩虹头颅] 此命令必须由一个玩家执行");
                    return true;
                }

                if (!sender.hasPermission("rainbowskull.give")) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 您没有执行此命令的权限");
                    return true;
                }

                if (args.length < 5) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 缺少必要参数: <playerName> <entity/player> <killerPlayerName> <entityName/playerName>");
                    return true;
                }

                String recipientName = args[1];
                Player recipient = Bukkit.getPlayer(args[1]);
                if (recipient == null) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 玩家 " + recipientName + " 不在线或不存在。");
                    return true;
                }

                String targetType = args[2];
                String killerPlayerName = args[3].equals("<此参数为击杀者名称>") ? sender.getName() : args[3];
                String skullOrPlayerName = ("entity".equals(targetType) && args[4].equals("<此参数为被击杀实体或玩家名称>")) ? "ALLAY" : args[4];
                int value = 1;

                if (args.length >= 6 && !args[5].equals("[此参数为头颅的数量]")) {
                    try {
                        value = Integer.parseInt(args[5]);
                        if (value <= 0) {
                            sender.sendMessage("§7[§c彩虹头颅§7] 错误, 参数 " + value + " 不在区间内: [1 ~ ∞]");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§7[§c彩虹头颅§7] 错误, 参数 " + args[5] + " 不是数字");
                        return true;
                    }
                }

                ConfigurationSection entitiesSection = fileManager.getSkull().getConfigurationSection("Skulls");
                if ("entity".equals(targetType) && (entitiesSection == null || !entitiesSection.getKeys(false).contains(skullOrPlayerName.toUpperCase()))) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 该实体 " + skullOrPlayerName + " 没有受到支持, 在 skull.yml:Skulls 中添加以继续");
                    return true;
                }
                else if (!"entity".equals(targetType) && !"player".equals(targetType)) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误，该类型不存在或拼写错误: " + targetType + " <entity/player>");
                    return true;
                }

                mobs.giveHeadByCommand((Player) sender, recipient, targetType, killerPlayerName, skullOrPlayerName, value);
                break;


            case "world":
                if (!sender.hasPermission("rainbowskull.world")) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 您没有执行此命令的权限");
                    return true;
                }

                ConfigurationSection allowSection = fileManager.getConfig().getConfigurationSection("World.allow");
                ConfigurationSection additionalSection = fileManager.getConfig().getConfigurationSection("World.additional");
                ConfigurationSection aliasSection = fileManager.getConfig().getConfigurationSection("World.alias");


                if (allowSection == null || additionalSection == null || aliasSection == null) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 错误, 请联系服务器管理员, 并告知他们原因: config.yml: World");
                    return true;
                }

                Set<String> allowedWorlds = allowSection.getKeys(false);

                sender.sendMessage("§7[§c彩虹头颅§7] 世界:");
                for (String world : allowedWorlds) {
                    StringBuilder message = new StringBuilder("  §7|-§f ");

                    // 添加世界名
                    message.append(world);

                    // 添加别名
                    if (aliasSection != null && aliasSection.contains(world)) {
                        String alias = aliasSection.getString(world).replace("&", "§");
                        message.append(" ").append(alias);
                    }

                    // 检查是否在additional部分
                    if (additionalSection != null && additionalSection.contains(world)) {
                        double additionalChance = additionalSection.getDouble(world);
                        message.append(" §7额外概率: ").append(additionalChance);
                    }

                    // 添加是否支持
                    boolean isSupported = additionalSection != null && (additionalSection.contains(world) || aliasSection.contains(world));
                    String supportStatus = isSupported ? "§a启用" : "§c未启用";
                    message.append(" ").append(supportStatus);

                    sender.sendMessage(message.toString());
                }
                return true;

            case "reload":
                if (!sender.hasPermission("rainbowskull.reload")) {
                    sender.sendMessage("§7[§c彩虹头颅§7] 您没有执行此命令的权限");
                    return true;
                }

                fileManager.reloadConfig();

                sender.sendMessage("§7[§c彩虹头颅§7] 配置文件已重新加载");
                return true;

            case "help":
                sender.sendMessage("§7[§c彩虹头颅§7] 帮助:");
                sender.sendMessage("§7  |- 必要参数: <?>, 可选必要参数: <?/?>, 非必要参数: [?], 可选非必要参数: [?/?]");
                sender.sendMessage("§7  |- §c/rs info <complete/chat/title/subtitle/actionbar> §7- 设置斩首提示的位置");
                sender.sendMessage("§7  |- §c/rs check <true/false> §7- 开启 或 关闭 可以触发灵魂斩首的生物检查");
                sender.sendMessage("§7  |- §c/rs behead <true/false> §7- 开启 或 关闭 灵魂斩首");
                sender.sendMessage("§7  |- §c/rs world §7- 查看各个世界的别名、额外概率和是否启用灵魂斩首");
                sender.sendMessage("§7  |- §c/rs give <playerName> entity <killerName> <entityName> [value] §7- 给予玩家指定数量的生物头颅 (管理员)");
                sender.sendMessage("§7  |- §c/rs give <playerName> player <killerName> <playerName> [value] §7- 给予玩家指定数量的玩家头颅 (管理员)");
                sender.sendMessage("§7  |- §c/rs reload §7- 重新载入配置文件 (管理员)");
                break;

            default:
                sender.sendMessage("§7[§c彩虹头颅§7] 这是一个不存在的命令或拼写错误: " + args[0]);
                sender.sendMessage("§7  |- 输入 §c/rs help §7查看帮助");
                break;
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!cmd.getName().equalsIgnoreCase("rs")) {
            return completions;
        }

        switch (args.length) {
            case 1:
                completions.addAll(Arrays.asList("info", "give", "reload", "behead", "help", "check", "world"));
                break;
            case 2:
                if (args[0].equalsIgnoreCase("info")) {
                    completions.addAll(Arrays.asList("complete", "chat", "title", "subtitle", "actionbar"));
                } else if (args[0].equalsIgnoreCase("give")) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                }
                if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("behead")) {
                    completions.addAll(Arrays.asList("true", "false"));
                }
                break;
            case 3:
                if (args[0].equalsIgnoreCase("give")) {
                    completions.addAll(Arrays.asList("entity", "player"));
                }
                break;
            case 4:
                if (args[0].equalsIgnoreCase("give")) {
                    completions.add("<此参数为击杀者名称>");
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase())).toList());
                }
                break;
            case 5:
                if (args[0].equalsIgnoreCase("give")) {
                    if (args[2].equalsIgnoreCase("entity")) {
                        ConfigurationSection entitiesSection = fileManager.getSkull().getConfigurationSection("Skulls");
                        completions.add("<此参数为被击杀实体或玩家名称>");
                        completions.addAll(entitiesSection.getKeys(false)
                                .stream()
                                .filter(entity -> entity.toLowerCase().startsWith(args[4].toLowerCase())).toList());
                    } else if (args[2].equalsIgnoreCase("player")) {
                        completions.add("<此参数为被击杀实体或玩家名称>");
                        completions.addAll(Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(args[4].toLowerCase())).toList());
                    }
                }
                break;

            case 6:
                if (args[0].equalsIgnoreCase("give")) {
                    completions.add("[此参数为头颅的数量]");
                }
                break;
        }

        return completions;
    }


}
