package com.highpalace.dynastycore;

import com.highpalace.dynastycore.command.DynastyCommand;
import com.highpalace.dynastycore.command.ZodiacCommand;
import com.highpalace.dynastycore.listener.ZodiacJoinListener;
import com.highpalace.dynastycore.listener.ZodiacPerkListener;
import com.highpalace.dynastycore.metrics.MetricsServer;
import com.highpalace.dynastycore.zodiac.ProfileManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DynastyCore extends JavaPlugin {
    private ProfileManager profileManager;
    private ZodiacPerkListener perkListener;
    private MetricsServer metricsServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Core systems
        profileManager = new ProfileManager(this);
        perkListener = new ZodiacPerkListener(this, profileManager);

        // Register listeners
        getServer().getPluginManager().registerEvents(perkListener, this);
        getServer().getPluginManager().registerEvents(new ZodiacJoinListener(this, profileManager), this);

        // Register commands
        boolean allowChange = getConfig().getBoolean("zodiac.allow-change", false);
        getCommand("zodiac").setExecutor(new ZodiacCommand(profileManager, perkListener, allowChange));
        ZodiacCommand zodiacCmd = new ZodiacCommand(profileManager, perkListener, allowChange);
        getCommand("zodiac").setExecutor(zodiacCmd);
        getCommand("zodiac").setTabCompleter(zodiacCmd);

        DynastyCommand dynastyCmd = new DynastyCommand(profileManager);
        getCommand("dynasty").setExecutor(dynastyCmd);
        getCommand("dynasty").setTabCompleter(dynastyCmd);

        // Scheduled tasks
        // Auto-save profiles every 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                profileManager.saveAll();
            }
        }.runTaskTimerAsynchronously(this, 6000L, 6000L);

        // Dog aura tick every 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                perkListener.tickDogAura();
            }
        }.runTaskTimer(this, 60L, 60L);

        // Apply goat knockback to already-online players (reload scenario)
        for (Player player : getServer().getOnlinePlayers()) {
            perkListener.applyGoatKnockback(player);
        }

        // Metrics
        if (getConfig().getBoolean("metrics.enabled", true)) {
            int port = getConfig().getInt("metrics.port", 9230);
            metricsServer = new MetricsServer(this, profileManager);
            metricsServer.start(port);
        }

        getLogger().info("DynastyCore enabled — 天命已定");
    }

    @Override
    public void onDisable() {
        if (metricsServer != null) {
            metricsServer.stop();
        }
        if (profileManager != null) {
            profileManager.saveAll();
        }
        getLogger().info("DynastyCore disabled — 再见");
    }
}
