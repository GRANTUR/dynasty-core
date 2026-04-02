package com.highpalace.dynastycore.listener;

import com.highpalace.dynastycore.economy.CurrencyItem;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class VillagerTradeListener implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final double coinTradeChance;

    public VillagerTradeListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.coinTradeChance = plugin.getConfig().getDouble("economy.villager-coin-trade-chance", 0.20);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;

        // Don't modify money changer villagers
        if (villager.getProfession() == Villager.Profession.NITWIT && villager.isInvulnerable()) return;

        if (random.nextDouble() < coinTradeChance) {
            MerchantRecipe original = event.getRecipe();
            ItemStack result = original.getResult();

            // Create a new recipe that accepts copper coins as an additional ingredient
            int coinCost = 4 + random.nextInt(13); // 4-16 coins
            MerchantRecipe modified = new MerchantRecipe(
                    result,
                    original.getUses(),
                    original.getMaxUses(),
                    original.hasExperienceReward(),
                    original.getVillagerExperience(),
                    original.getPriceMultiplier()
            );

            // Keep first ingredient, add copper coins as second
            modified.addIngredient(original.getIngredients().get(0));
            modified.addIngredient(CurrencyItem.createTongqian(coinCost));

            event.setRecipe(modified);
        }
    }
}
