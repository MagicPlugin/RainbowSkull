package com.magicpowered.rainbowskull;

import java.util.List;

// 头颅被放置后的数据类
public class SkullData {
    private String displayName;
    private List<String> lore;

    public SkullData(String displayName, List<String> lore) {
        this.displayName = displayName;
        this.lore = lore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }
}
