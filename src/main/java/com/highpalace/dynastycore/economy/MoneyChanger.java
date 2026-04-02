package com.highpalace.dynastycore.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MoneyChanger {
    private final JavaPlugin plugin;

    public MoneyChanger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawn a 钱庄 (money changer) villager at the given location.
     * Trades: 64 铜钱 → 1 银元宝
     */
    public Villager spawn(Location location) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);

        villager.customName(Component.text("钱庄 Qiánzhuāng", NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD));
        villager.setCustomNameVisible(true);
        villager.setProfession(Villager.Profession.NITWIT);
        villager.setVillagerLevel(5);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);

        // 64 copper coins → 1 yuanbao
        MerchantRecipe coinToYuanbao = new MerchantRecipe(CurrencyItem.createYuanbao(1), 0, Integer.MAX_VALUE, false);
        coinToYuanbao.addIngredient(CurrencyItem.createTongqian(64));

        // 1 yuanbao → 64 copper coins (reverse exchange)
        MerchantRecipe yuanbaoToCoin = new MerchantRecipe(CurrencyItem.createTongqian(64), 0, Integer.MAX_VALUE, false);
        yuanbaoToCoin.addIngredient(CurrencyItem.createYuanbao(1));

        villager.setRecipes(List.of(coinToYuanbao, yuanbaoToCoin));

        return villager;
    }
}
