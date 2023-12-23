package com.magicpowered.rainbowskull;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VariableStorage {
    private FileManager fileManager;
    private String storageType;
    private Map<String, Map<String, Integer>> data; // 玩家UUID -> (实体类型 -> 斩首数量)
    private Connection connection;
    private FileConfiguration config;
    public boolean connectionSuccess = true;

    public VariableStorage(FileManager fileManager) {
        this.fileManager = fileManager;
        this.storageType = fileManager.getConfig().getString("Variable.databaseSetting", "YAML");
        this.config = fileManager.getConfig();
        this.data = new HashMap<>();

        if (storageType.equalsIgnoreCase("MySQL")) {
            initializeMySQL();
        }
    }

    private void initializeMySQL() {
        String host = config.getString("Variable.MySQL.host");
        int port = config.getInt("Variable.MySQL.port");
        String database = config.getString("Variable.MySQL.database");
        String username = config.getString("Variable.MySQL.username");
        String password = config.getString("Variable.MySQL.password");

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

            // 创建表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS player_decapitations (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "entity_type VARCHAR(255) NOT NULL, " +
                    "count INT NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (uuid, entity_type))";
            Statement statement = connection.createStatement();
            statement.execute(createTableSQL);
            Bukkit.getLogger().info("[彩虹头颅] 成功连接至 MySQL 数据库");
        } catch (SQLException e) {
            e.printStackTrace();
            connectionSuccess = false;
        }
    }

    public void incrementDecapitation(String playerUUID, String entityType) {
        Map<String, Integer> playerData = data.getOrDefault(playerUUID, new HashMap<>());
        int count = playerData.getOrDefault(entityType, 0) + 1;
        int totalCount = playerData.getOrDefault("total", 0) + 1;
        playerData.put(entityType, count);
        data.put(playerUUID, playerData);

        if (storageType.equalsIgnoreCase("YAML")) {
            FileConfiguration yamlFile = fileManager.getVariable();
            // 构建路径
            String path = playerUUID + "." + entityType;
            String totalPath = playerUUID + ".total";
            fileManager.getVariable().set(path, count);
            fileManager.getVariable().set(totalPath, totalCount);
            fileManager.saveVariable();

        } else if (storageType.equalsIgnoreCase("MySQL") && connectionSuccess) {
            // 检查并更新或插入数据
            try {
                String query = "SELECT count FROM player_decapitations WHERE uuid = ? AND entity_type = ?";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, playerUUID);
                    ps.setString(2, entityType);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        // 更新现有记录
                        String updateSQL = "UPDATE player_decapitations SET count = ? WHERE uuid = ? AND entity_type = ?";
                        try (PreparedStatement updatePs = connection.prepareStatement(updateSQL)) {
                            updatePs.setInt(1, count);
                            updatePs.setString(2, playerUUID);
                            updatePs.setString(3, entityType);
                            updatePs.executeUpdate();
                        }
                    } else {
                        // 插入新记录
                        String insertSQL = "INSERT INTO player_decapitations (uuid, entity_type, count) VALUES (?, ?, ?)";
                        try (PreparedStatement insertPs = connection.prepareStatement(insertSQL)) {
                            insertPs.setString(1, playerUUID);
                            insertPs.setString(2, entityType);
                            insertPs.setInt(3, count);
                            insertPs.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // 处理数据库错误
            }

            try {
                // 检查是否存在总斩首记录
                String totalQuery = "SELECT count FROM player_decapitations WHERE uuid = ? AND entity_type = 'total'";
                try (PreparedStatement totalPs = connection.prepareStatement(totalQuery)) {
                    totalPs.setString(1, playerUUID);
                    ResultSet totalRs = totalPs.executeQuery();

                    if (totalRs.next()) {
                        // 更新现有总斩首记录
                        int updateTotal = totalRs.getInt(1) + 1;
                        String updateTotalSQL = "UPDATE player_decapitations SET count = ? WHERE uuid = ? AND entity_type = 'total'";
                        try (PreparedStatement updateTotalPs = connection.prepareStatement(updateTotalSQL)) {
                            updateTotalPs.setInt(1, updateTotal);
                            updateTotalPs.setString(2, playerUUID);
                            updateTotalPs.executeUpdate();
                        }
                    } else {
                        // 插入新的总斩首记录
                        String insertTotalSQL = "INSERT INTO player_decapitations (uuid, entity_type, count) VALUES (?, 'total', 1)";
                        try (PreparedStatement insertTotalPs = connection.prepareStatement(insertTotalSQL)) {
                            insertTotalPs.setString(1, playerUUID);
                            insertTotalPs.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // 处理数据库错误
            }
        }
    }



    public int getDecapitations(UUID playerUUID, String entityName) {
        String storageMethod = fileManager.getConfig().getString("Variable.databaseSetting", "YAML");
        if ("YAML".equalsIgnoreCase(storageMethod)) {
            return getDecapitationsFromYAML(playerUUID, entityName);
        } else if ("MySQL".equalsIgnoreCase(storageMethod)) {
            return getDecapitationsFromMySQL(playerUUID, entityName);
        } else {
            // 默认或错误的存储方式处理
            Bukkit.getLogger().warning("未知的存储方式: " + storageMethod);
            return 0;
        }
    }

    private int getDecapitationsFromYAML(UUID playerUUID, String entityName) {
        // YAML 存储逻辑
        FileConfiguration config = fileManager.getVariable();
        return config.getInt(playerUUID.toString() + "." + entityName, 0);
    }

    private int getDecapitationsFromMySQL(UUID playerUUID, String entityName) {
        // MySQL 存储逻辑
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT count FROM player_decapitations WHERE uuid = ? AND entity_type = ?");
            ps.setString(1, playerUUID.toString());
            ps.setString(2, entityName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
