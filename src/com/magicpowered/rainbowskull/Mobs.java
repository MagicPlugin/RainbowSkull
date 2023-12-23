package com.magicpowered.rainbowskull;
import de.tr7zw.nbtapi.*;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Mobs {

    private RainbowSkull plugin;
    private FileManager fileManager;

    public Mobs(RainbowSkull plugin, FileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;
    }

    public String getSkinData(String playerName) {
        try {
            // 获取 UUID
            HttpURLConnection uuidConnection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName).openConnection();
            InputStream uuidStream = uuidConnection.getInputStream();
            String uuidResponse = new String(uuidStream.readAllBytes());
            JSONObject uuidJson = new JSONObject(uuidResponse);
            String uuid = uuidJson.getString("id");
            uuidStream.close();

            // 获取皮肤数据
            HttpURLConnection skinConnection = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).openConnection();
            InputStream skinStream = skinConnection.getInputStream();
            String skinResponse = new String(skinStream.readAllBytes());
            JSONObject skinJson = new JSONObject(skinResponse);
            String skinData = skinJson.getJSONArray("properties").getJSONObject(0).getString("value");
            skinStream.close();

            return skinData;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 使用 NBTAPI 官方办法
    public ItemStack applySkinToSkull(ItemStack skull, String textureValue) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        NBT.modify(item, nbt -> {
            final ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");

            // 从 textureValue 生成一个固定的 UUID
            UUID textureUUID = UUID.nameUUIDFromBytes(textureValue.getBytes(StandardCharsets.UTF_8));
            skullOwnerCompound.setUUID("Id", textureUUID);

            skullOwnerCompound.getOrCreateCompound("Properties")
                    .getCompoundList("textures")
                    .addCompound()
                    .setString("Value", textureValue);
        });

        return item;
    }



    // 给玩家一个生物的头颅
    public void giveEntityHeadToPlayer(Player player, EntityType entityType, Player killedPlayer, World world) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        

        // 如果有自定义的皮肤数据, 设置头颅的皮肤
        if (entityType != EntityType.PLAYER) {
            String skinData = fileManager.getSkull().getString("Skulls." + entityType.name().toUpperCase() + ".skin");
            if (skinData != null && !skinData.isEmpty()) {
                skull = applySkinToSkull(skull, skinData);  // 接收修改后的头颅
            }
        } else {
            String skinData = getSkinData(killedPlayer.getName());
            if (skinData != null && !skinData.isEmpty()) {
                skull = applySkinToSkull(skull, skinData);  // 接收修改后的头颅
            }
        }

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();


        // 设置头颅的DisplayName和Lore
        String displayName = fileManager.getSkull().getString("Skulls." + entityType.name().toUpperCase() + ".displayName", "灵魂之首").replace("&", "§").replace("%rainbowskull_killedPlayer%", killedPlayer.getDisplayName());

        skullMeta.setDisplayName(displayName);

        List<String> loreList = fileManager.getSkull().getStringList("Skulls." + entityType.name().toUpperCase() + ".lore");

        // 获取时间格式
        String timeFormat = fileManager.getSkull().getString("Time");
        if (timeFormat == null || timeFormat.isEmpty()) {
            timeFormat = "yyyy-MM-dd HH:mm:ss";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);

        for (int i = 0; i < loreList.size(); i++) {
            String lore = loreList.get(i);
            lore = lore.replace("%rainbowskull_killed_player%", player.getName());
            lore = lore.replace("%rainbowskull_killed_time%", sdf.format(new Date()));
            lore = lore.replace("%rainbowskull_killed_world%", fileManager.getWorldName(world));
            lore = lore.replace("&", "§");
            loreList.set(i, lore);
        }


        skullMeta.setLore(loreList);
        skull.setItemMeta(skullMeta);

        if (fileManager.getConfig().getBoolean("Behead.Broadcast.state")) {
            fileManager.sendBroadCase(player.getName(), entityType.name().toUpperCase(), fileManager.getWorldName(world));
        }
        if (fileManager.getConfig().getBoolean("Behead.Script.state")) {
            fileManager.executeScript(player.getName(), entityType.name().toUpperCase(), fileManager.getWorldName(world));
        }

        fileManager.sendInfo(player, displayName);

        if (addItemToPlayerOrDrop(player, skull)) {
            fileManager.sendMessage(player, "errorGetMagicHead");
            return;
        }

    }


    public void giveHeadByCommand(Player sender, Player receiver, String type, String killerName, String entityOrPlayerName, int amount) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta skullMeta = null;
        String worldName = fileManager.getWorldName(sender.getWorld());

        if (type.equalsIgnoreCase("entity")) {

            String skinData = fileManager.getSkull().getString("Skulls." + entityOrPlayerName.toUpperCase() + ".skin");
            skull = applySkinToSkull(skull, skinData);

            skullMeta = (SkullMeta) skull.getItemMeta();

            String displayName = fileManager.getSkull().getString("Skulls." + entityOrPlayerName.toUpperCase() + ".displayName", "灵魂之首").replace("&", "§");

            assert skullMeta != null;
            skullMeta.setDisplayName(displayName);

            List<String> loreList = fileManager.getSkull().getStringList("Skulls." + entityOrPlayerName.toUpperCase() + ".lore");

            // 获取时间格式
            String timeFormat = fileManager.getSkull().getString("Time");
            if (timeFormat == null || timeFormat.isEmpty()) {
                timeFormat = "yyyy-MM-dd HH:mm:ss";
            }

            SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);

            for (int i = 0; i < loreList.size(); i++) {
                String lore = loreList.get(i);
                lore = lore.replace("%rainbowskull_killed_player%", killerName);
                lore = lore.replace("%rainbowskull_killed_time%", sdf.format(new Date()));
                lore = lore.replace("%rainbowskull_killed_world%", worldName);
                lore = lore.replace("&", "§");
                loreList.set(i, lore);
            }

            skullMeta.setLore(loreList);

        } else if (type.equalsIgnoreCase("player")) {

            String skinData = getSkinData(entityOrPlayerName);
            if (skinData == null) {
                sender.sendMessage("§7[§c彩虹头颅§7] 错误, 无法获取 " + entityOrPlayerName + " 的皮肤, 因此使用默认 Player_HEAD");
            } else {
                skull = applySkinToSkull(skull, skinData);
            }

            skullMeta = (SkullMeta) skull.getItemMeta();

            String displayName = fileManager.getSkull().getString("Player.displayName").replace("&", "§").replace("%rainbowskull_killedPlayer%", entityOrPlayerName);
            if (displayName == null || displayName.isEmpty()) {
                displayName = "灵魂之首";
            }
            assert skullMeta != null;
            skullMeta.setDisplayName(displayName);

            List<String> loreList = fileManager.getSkull().getStringList("Player.lore");

            for (int i = 0; i < loreList.size(); i++) {
                String lore = loreList.get(i);
                lore = lore.replace("%rainbowskull_killed_player%", killerName);
                lore = lore.replace("%rainbowskull_killed_time%", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                lore = lore.replace("%rainbowskull_killed_world%", worldName);
                lore = lore.replace("&", "§");
                loreList.set(i, lore);
            }
            skullMeta.setLore(loreList);
        }

        if (skullMeta == null) {
            throw new IllegalStateException("skullMeta should have been initialized!");
        }

        skull.setItemMeta(skullMeta);

        if (addItemToPlayerOrDrop(receiver, skull)) {
            fileManager.sendMessage(receiver, "errorReceivedMagicHead");
            return;
        }

        fileManager.sendMessage(receiver, "successReceivedMagicHead");
        sender.sendMessage("§7[§c彩虹头颅§7] 成功给予 " + receiver.getName() + " " + amount + " 个 " + skull.getItemMeta().getDisplayName() + " §7的灵魂之首");

    }

    /*
     * 将物品添加到玩家的背包中。如果背包满了, 物品会掉落到玩家的身边。
     */
    public boolean addItemToPlayerOrDrop(Player player, ItemStack item) {

        HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);
        if (!remainingItems.isEmpty()) {
            Location dropLocation = player.getLocation().add(0, 1, 0); // 在玩家头上的位置掉落
            for (ItemStack remainingItem : remainingItems.values()) {
                player.getWorld().dropItemNaturally(dropLocation, remainingItem);
            }
            return true;
        }
        return false;
    }



}
