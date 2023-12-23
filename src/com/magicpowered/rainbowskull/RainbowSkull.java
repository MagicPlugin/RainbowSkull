package com.magicpowered.rainbowskull;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RainbowSkull extends JavaPlugin implements Listener {

    private FileManager fileManager;
    private CommandListener commandListener;
    private EventListener eventListener;
    private Mobs mobs;
    private VariableStorage variableStorage;
    private RegisterVariable registerVariable;

    public boolean isPlaceholderLoaded = false;

    @Override
    public void onEnable() {
        try {
            isPlaceholderLoaded = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

            fileManager = new FileManager(this);
            mobs = new Mobs(this, fileManager);
            commandListener = new CommandListener(this, fileManager, mobs);
            eventListener = new EventListener(this, fileManager, mobs);
            if (isPlaceholderLoaded && getConfig().getBoolean("Variable.state", false)) {
                variableStorage = new VariableStorage(fileManager);
                if (variableStorage.connectionSuccess) {
                    registerVariable = new RegisterVariable(this, variableStorage);
                    registerVariable.register();
                    Bukkit.getLogger().info("[彩虹头颅] 成功注册变量模块, 并且挂钩 PlaceHolder-API, 您应该看到此消息旁的 PlaceHolder-API 提示");
                } else {
                    Bukkit.getLogger().warning("[彩虹头颅] 连接 MySQL 数据库失败，因此，变量模块已暂时禁用");
                }
            }

            fileManager.reloadConfig();

            getServer().getPluginManager().registerEvents(eventListener, this);
            getServer().getPluginManager().registerEvents(this, this);
            getCommand("rs").setExecutor(commandListener);
            getCommand("rs").setTabCompleter(commandListener);

            Bukkit.getServer().getLogger().info(" ");
            Bukkit.getServer().getLogger().info("  '||    ||' '||''|.         '||''|.    .|'''.|      妙控动力 MagicPowered");
            Bukkit.getServer().getLogger().info("   |||  |||   ||   ||   ||    ||   ||   ||..  '      彩虹系列 RainbowSeries");
            Bukkit.getServer().getLogger().info("   |'|..'||   ||...|'         || ''|'     ''|||.     彩虹头颅 RainbowSkull v23.0.3.5");
            Bukkit.getServer().getLogger().info("   | '|' ||   ||        ||    ||   |.   .     '||    由 JLING 制作");
            Bukkit.getServer().getLogger().info("  .|. | .||. .||.            .||.  '|'  |'....|'     https://magicpowered.cn");
            Bukkit.getServer().getLogger().info(" ");
            if (!isPlaceholderLoaded)
                Bukkit.getServer().getLogger().info("  软依赖 PlaceHolder-API 插件未找到或未启用, 因此 RainbowSkull 的 Variable 模块将被强制关闭");
        } catch (
                Exception e) {
            Bukkit.getServer().getLogger().info("[彩虹头颅] 启动失败!");
            e.printStackTrace();
        }

    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("RainbowSkull")) {
            Bukkit.getServer().getLogger().info("[彩虹头颅] 彩虹光照，世界依然，再会!");
        }
    }

    @Override
    public void onDisable() {
        // 保存配置文件
        fileManager.saveConfig();
        fileManager.saveDatabase();
        fileManager.saveSkull();
        fileManager.saveVariable();
    }

}
