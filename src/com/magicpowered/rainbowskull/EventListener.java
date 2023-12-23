package com.magicpowered.rainbowskull;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class EventListener implements Listener {

    private RainbowSkull plugin;
    private Mobs mobs;
    private FileManager fileManager;
    private VariableStorage variableStorage;

    public EventListener(RainbowSkull plugin, FileManager fileManager, Mobs mobs) {
        this.plugin = plugin;
        this.mobs = mobs;
        this.fileManager = fileManager;
        this.variableStorage = new VariableStorage(fileManager);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        List<String> blackList = fileManager.getConfig().getStringList("BlackList");
        if (blackList.contains(killer.getName())) {
            return;
        }

        if (!fileManager.getBeheadMode(killer)) {
            return;
        }

        if (!fileManager.getConfig().getBoolean("World.allow." + entity.getWorld().getName())) {
            fileManager.sendMessage(killer, "isUnSupportWorld");
            return;
        }

        // 如果击杀的是玩家，进入判断是否该斩首步骤
        if (entity instanceof Player) {
            if (fileManager.shouldDecapitate(killer, entity.getType(), entity.getWorld())) {
                // 给予玩家头颅
                mobs.giveEntityHeadToPlayer(killer, entity.getType(), killer, entity.getWorld());
            }
            return;
        }

        if (fileManager.getSkull().getString("Skulls." + entity.getType()) == null) {
            fileManager.sendCheckMessage(killer, entity.getType(), "isUnSupportEntity");
            return;
        }

        fileManager.sendCheckMessage(killer, entity.getType(), "isSupportEntity");

        // 判断是否应该斩首
        if (fileManager.shouldDecapitate(killer, entity.getType(), entity.getWorld())) {
            mobs.giveEntityHeadToPlayer(killer, entity.getType(), killer, entity.getWorld());
            variableStorage.incrementDecapitation(killer.getUniqueId().toString(), entity.getType().name());

            // 检查此生物的子的生物，并判断是否应该斩首
            List<String> relatedEntities = fileManager.getSkull().getStringList("Skulls." + entity.getType() + ".relate");
            for (String relatedEntity : relatedEntities) {
                if (fileManager.getSkull().getString("Skulls." + relatedEntity) != null) {
                    if (fileManager.shouldDecapitate(killer, EntityType.valueOf(relatedEntity), entity.getWorld())) {
                        mobs.giveEntityHeadToPlayer(killer, EntityType.valueOf(relatedEntity), killer, entity.getWorld());
                        variableStorage.incrementDecapitation(killer.getUniqueId().toString(), entity.getType().name());
                    }
                } else {
                    fileManager.sendMessage(killer,"relateSkullNull");
                    Bukkit.getServer().getLogger().warning("[彩虹头颅] " + entity.getType() + " 的关联头颅 " + relatedEntity + " 出了问题，请检查配置文件");
                }
            }
        }
    }


    @EventHandler
    public void onSkullPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            fileManager.sendPABMessage(event.getPlayer(), "placeMagicHead");

            Skull skull = (Skull) block.getState();
            ItemMeta meta = event.getItemInHand().getItemMeta();

            // 存档到数据库
            String locationKey = block.getLocation().toVector().toString();
            fileManager.saveSkullData(locationKey, meta.getDisplayName(), meta.getLore());
        }
    }

    @EventHandler
    public void onSkullBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            // 原始的掉落物列表
            List<ItemStack> drops = (List<ItemStack>) event.getBlock().getDrops();

            // 寻找 ItemStack 中的头颅
            for (ItemStack item : drops) {
                fileManager.sendPABMessage(event.getPlayer(), "breakMagicHead");
                if (item.getType() == Material.PLAYER_HEAD || item.getType() == Material.PLAYER_WALL_HEAD) {
                    SkullMeta meta = (SkullMeta) item.getItemMeta();

                    // 从数据库恢复
                    String locationKey = block.getLocation().toVector().toString();
                    SkullData skullData = fileManager.getSkullData(locationKey);
                    meta.setDisplayName(skullData.getDisplayName());
                    meta.setLore(skullData.getLore());

                    item.setItemMeta(meta);
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                    event.setDropItems(false);
                }
            }

            // 清除已经使用的数据
            String locationKey = block.getLocation().toVector().toString();
            fileManager.removeSkullData(locationKey);
        }
    }

}
