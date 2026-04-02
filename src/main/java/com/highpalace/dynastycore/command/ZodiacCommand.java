package com.highpalace.dynastycore.command;

import com.highpalace.dynastycore.listener.ZodiacPerkListener;
import com.highpalace.dynastycore.zodiac.ProfileManager;
import com.highpalace.dynastycore.zodiac.Zodiac;
import com.highpalace.dynastycore.zodiac.ZodiacProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ZodiacCommand implements CommandExecutor, TabCompleter {
    private final ProfileManager profileManager;
    private final ZodiacPerkListener perkListener;
    private final boolean allowChange;

    public ZodiacCommand(ProfileManager profileManager, ZodiacPerkListener perkListener, boolean allowChange) {
        this.profileManager = profileManager;
        this.perkListener = perkListener;
        this.allowChange = allowChange;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            showUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "choose" -> handleChoose(player, args);
            case "info" -> handleInfo(player, args);
            case "status" -> handleStatus(player);
            case "list" -> handleList(player);
            default -> showUsage(player);
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage(Component.text("═══ ", NamedTextColor.GOLD)
                .append(Component.text("Zodiac Commands", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(" ═══", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("/zodiac list", NamedTextColor.GREEN)
                .append(Component.text(" — View all zodiac signs", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/zodiac info <sign>", NamedTextColor.GREEN)
                .append(Component.text(" — Details about a sign", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/zodiac choose <sign>", NamedTextColor.GREEN)
                .append(Component.text(" — Choose your zodiac", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/zodiac status", NamedTextColor.GREEN)
                .append(Component.text(" — View your current zodiac", NamedTextColor.GRAY)));
    }

    private void handleList(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("  十二生肖 — The Twelve Zodiac Signs", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());

        for (Zodiac z : Zodiac.values()) {
            Component line = Component.text("  " + z.getHanzi() + " ", NamedTextColor.GOLD)
                    .append(Component.text(z.getDisplayName(), NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                    .append(Component.text(" — " + z.getPerkName(), NamedTextColor.GRAY))
                    .clickEvent(ClickEvent.runCommand("/zodiac info " + z.getDisplayName()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click for details", NamedTextColor.YELLOW)));
            player.sendMessage(line);
        }
        player.sendMessage(Component.empty());
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /zodiac info <sign>", NamedTextColor.RED));
            return;
        }

        Zodiac zodiac = Zodiac.fromString(args[1]);
        if (zodiac == null) {
            player.sendMessage(Component.text("Unknown zodiac sign: " + args[1], NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═══ ", NamedTextColor.GOLD)
                .append(Component.text(zodiac.getFullLabel(), NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(" ═══", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("  Perk: ", NamedTextColor.GRAY)
                .append(Component.text(zodiac.getPerkName(), NamedTextColor.GREEN).decorate(TextDecoration.BOLD)));
        player.sendMessage(Component.text("  " + zodiac.getPerkDescription(), NamedTextColor.WHITE));
        player.sendMessage(Component.empty());
    }

    private void handleChoose(Player player, String[] args) {
        ZodiacProfile profile = profileManager.getProfile(player.getUniqueId(), player.getName());

        if (profile.hasChosen() && !allowChange) {
            player.sendMessage(Component.text("Your zodiac is already set to ", NamedTextColor.RED)
                    .append(Component.text(profile.getZodiac().getFullLabel(), NamedTextColor.GOLD))
                    .append(Component.text(". This choice is permanent.", NamedTextColor.RED)));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /zodiac choose <sign>", NamedTextColor.RED));
            player.sendMessage(Component.text("Use /zodiac list to see all signs.", NamedTextColor.GRAY));
            return;
        }

        Zodiac zodiac = Zodiac.fromString(args[1]);
        if (zodiac == null) {
            player.sendMessage(Component.text("Unknown zodiac sign: " + args[1], NamedTextColor.RED));
            return;
        }

        // Remove old goat knockback if changing
        if (profile.hasChosen() && profile.getZodiac() == Zodiac.GOAT) {
            perkListener.removeGoatKnockback(player);
        }

        profile.setZodiac(zodiac);
        profileManager.saveProfile(profile);

        // Apply goat knockback if newly chosen
        if (zodiac == Zodiac.GOAT) {
            perkListener.applyGoatKnockback(player);
        }

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("  天命已定 — Your fate is sealed.", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  You are born under the sign of the ", NamedTextColor.WHITE)
                .append(Component.text(zodiac.getFullLabel(), NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));
        player.sendMessage(Component.text("  Perk: ", NamedTextColor.GRAY)
                .append(Component.text(zodiac.getPerkName(), NamedTextColor.GREEN))
                .append(Component.text(" — " + zodiac.getPerkDescription(), NamedTextColor.WHITE)));
        player.sendMessage(Component.empty());
    }

    private void handleStatus(Player player) {
        ZodiacProfile profile = profileManager.getProfile(player.getUniqueId(), player.getName());

        if (!profile.hasChosen()) {
            player.sendMessage(Component.text("You have not yet chosen a zodiac.", NamedTextColor.GRAY));
            player.sendMessage(Component.text("Use /zodiac choose <sign> to begin.", NamedTextColor.YELLOW));
            return;
        }

        Zodiac z = profile.getZodiac();
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═══ ", NamedTextColor.GOLD)
                .append(Component.text("Your Zodiac", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(" ═══", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("  Sign: ", NamedTextColor.GRAY)
                .append(Component.text(z.getFullLabel(), NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));
        player.sendMessage(Component.text("  Perk: ", NamedTextColor.GRAY)
                .append(Component.text(z.getPerkName(), NamedTextColor.GREEN)));
        player.sendMessage(Component.text("  " + z.getPerkDescription(), NamedTextColor.WHITE));
        player.sendMessage(Component.empty());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(List.of("choose", "info", "status", "list"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("choose") || args[0].equalsIgnoreCase("info"))) {
            return filterStartsWith(
                    Arrays.stream(Zodiac.values()).map(Zodiac::getDisplayName).collect(Collectors.toList()),
                    args[1]
            );
        }
        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
