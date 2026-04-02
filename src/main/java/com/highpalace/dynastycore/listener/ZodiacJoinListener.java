package com.highpalace.dynastycore.listener;

import com.highpalace.dynastycore.zodiac.ProfileManager;
import com.highpalace.dynastycore.zodiac.ZodiacProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ZodiacJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final ProfileManager profileManager;
    private final boolean promptOnJoin;

    public ZodiacJoinListener(JavaPlugin plugin, ProfileManager profileManager) {
        this.plugin = plugin;
        this.profileManager = profileManager;
        this.promptOnJoin = plugin.getConfig().getBoolean("zodiac.prompt-on-join", true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ZodiacProfile profile = profileManager.getProfile(player.getUniqueId(), player.getName());

        if (!profile.hasChosen() && promptOnJoin) {
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
            player.sendMessage(Component.text("  天命 — The Mandate of Heaven", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
            player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("  The stars await your answer.", NamedTextColor.GRAY));
            player.sendMessage(Component.text("  Choose your zodiac with ", NamedTextColor.GRAY)
                    .append(Component.text("/zodiac choose", NamedTextColor.GREEN)));
            player.sendMessage(Component.empty());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        profileManager.removeFromCache(event.getPlayer().getUniqueId());
    }
}
