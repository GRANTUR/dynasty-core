package com.highpalace.dynastycore.command;

import com.highpalace.dynastycore.zodiac.ProfileManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class DynastyCommand implements CommandExecutor, TabCompleter {
    private final ProfileManager profileManager;

    public DynastyCommand(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("DynastyCore v1.0.0", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("/dynasty reload", NamedTextColor.GREEN)
                    .append(Component.text(" — Reload configuration", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/dynasty reset <player>", NamedTextColor.GREEN)
                    .append(Component.text(" — Reset a player's profile", NamedTextColor.GRAY)));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("dynastycore.admin.reload")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                // Reload will be handled by main plugin class
                sender.sendMessage(Component.text("Configuration reloaded.", NamedTextColor.GREEN));
            }
            case "reset" -> {
                if (!sender.hasPermission("dynastycore.admin.reset")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /dynasty reset <player>", NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
                    return true;
                }
                profileManager.resetProfile(target.getUniqueId());
                sender.sendMessage(Component.text("Reset profile for " + target.getName(), NamedTextColor.GREEN));
                target.sendMessage(Component.text("Your DynastyCore profile has been reset by an admin.", NamedTextColor.YELLOW));
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "reset").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
