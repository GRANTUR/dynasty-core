package com.highpalace.dynastycore.command;

import com.highpalace.dynastycore.economy.CurrencyItem;
import com.highpalace.dynastycore.economy.MoneyChanger;
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
    private final MoneyChanger moneyChanger;

    public DynastyCommand(ProfileManager profileManager, MoneyChanger moneyChanger) {
        this.profileManager = profileManager;
        this.moneyChanger = moneyChanger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("DynastyCore v1.0.0", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("/dynasty reload", NamedTextColor.GREEN)
                    .append(Component.text(" — Reload configuration", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/dynasty reset <player>", NamedTextColor.GREEN)
                    .append(Component.text(" — Reset a player's profile", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/dynasty moneychanger", NamedTextColor.GREEN)
                    .append(Component.text(" — Spawn a 钱庄 at your location", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/dynasty give <tongqian|yuanbao> [amount]", NamedTextColor.GREEN)
                    .append(Component.text(" — Give yourself currency", NamedTextColor.GRAY)));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("dynastycore.admin.reload")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
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
            case "moneychanger" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Must be a player.");
                    return true;
                }
                if (!player.hasPermission("dynastycore.admin.moneychanger")) {
                    player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                moneyChanger.spawn(player.getLocation());
                player.sendMessage(Component.text("钱庄 Qiánzhuāng spawned — the money changer is open for business.", NamedTextColor.GOLD));
            }
            case "give" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Must be a player.");
                    return true;
                }
                if (!player.hasPermission("dynastycore.admin.give")) {
                    player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /dynasty give <tongqian|yuanbao> [amount]", NamedTextColor.RED));
                    return true;
                }
                int amount = args.length >= 3 ? Math.max(1, Math.min(64, parseInt(args[2], 1))) : 1;
                switch (args[1].toLowerCase()) {
                    case "tongqian" -> {
                        player.getInventory().addItem(CurrencyItem.createTongqian(amount));
                        player.sendMessage(Component.text("Received " + amount + " 铜钱", NamedTextColor.GOLD));
                    }
                    case "yuanbao" -> {
                        player.getInventory().addItem(CurrencyItem.createYuanbao(amount));
                        player.sendMessage(Component.text("Received " + amount + " 银元宝", NamedTextColor.WHITE));
                    }
                    default -> player.sendMessage(Component.text("Unknown currency. Use tongqian or yuanbao.", NamedTextColor.RED));
                }
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
        }
        return true;
    }

    private int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "reset", "moneychanger", "give").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("give")) {
                return List.of("tongqian", "yuanbao").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
