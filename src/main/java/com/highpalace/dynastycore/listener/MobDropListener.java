package com.highpalace.dynastycore.listener;

import com.highpalace.dynastycore.economy.CurrencyItem;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Random;

public class MobDropListener implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();

    private final double dropChance;
    private final int minDrop;
    private final int maxDrop;

    // Boss-tier mobs drop more
    private static final Map<EntityType, Integer> BONUS_DROPS = Map.of(
            EntityType.WARDEN, 16,
            EntityType.ELDER_GUARDIAN, 8,
            EntityType.WITHER, 32,
            EntityType.ENDER_DRAGON, 64
    );

    public MobDropListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dropChance = plugin.getConfig().getDouble("economy.mob-drop-chance", 0.35);
        this.minDrop = plugin.getConfig().getInt("economy.mob-drop-min", 1);
        this.maxDrop = plugin.getConfig().getInt("economy.mob-drop-max", 3);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobDeath(EntityDeathEvent event) {
        // Only drop when killed by a player
        if (event.getEntity().getKiller() == null) return;

        // Boss drops — guaranteed
        if (BONUS_DROPS.containsKey(event.getEntityType())) {
            int amount = BONUS_DROPS.get(event.getEntityType());
            event.getDrops().add(CurrencyItem.createTongqian(amount));
            return;
        }

        // Regular hostile mobs — chance-based
        if (!(event.getEntity() instanceof Monster)) return;

        if (random.nextDouble() < dropChance) {
            int amount = minDrop + random.nextInt(maxDrop - minDrop + 1);
            event.getDrops().add(CurrencyItem.createTongqian(amount));
        }
    }
}
