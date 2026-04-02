package com.highpalace.dynastycore.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CurrencyItem {
    public static final int TONGQIAN_MODEL_DATA = 1001;
    public static final int YUANBAO_MODEL_DATA = 1002;

    /**
     * Create a 铜钱 (copper coin) — reskinned copper nugget
     */
    public static ItemStack createTongqian(int amount) {
        ItemStack item = new ItemStack(Material.COPPER_INGOT, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("铜钱 Tóngqián", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("A copper coin with a square hole.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("The common currency of the realm.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setCustomModelData(TONGQIAN_MODEL_DATA);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a 银元宝 (silver yuanbao) — reskinned iron nugget
     */
    public static ItemStack createYuanbao(int amount) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("银元宝 Yuánbǎo", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("A silver sycee ingot.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Worth a stack of copper coins.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setCustomModelData(YUANBAO_MODEL_DATA);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Check if an ItemStack is a 铜钱
     */
    public static boolean isTongqian(ItemStack item) {
        if (item == null || item.getType() != Material.COPPER_INGOT) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == TONGQIAN_MODEL_DATA;
    }

    /**
     * Check if an ItemStack is a 银元宝
     */
    public static boolean isYuanbao(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_NUGGET) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == YUANBAO_MODEL_DATA;
    }
}
