package com.test;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RelicCommand implements CommandExecutor, TabCompleter {

    private final NightreignRelic plugin;

    public RelicCommand(NightreignRelic plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (args.length >= 1) {
            String sub = args[0].toLowerCase();
            if (sub.equals("relics")) {
                RelicMenu.openRelicMain(player);
                return true;
            } else if (sub.equals("reload")) {
                if (!player.hasPermission("nightreign.admin")) {
                    player.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getRelicManager().loadGachaCosts();
                player.sendMessage("§aNightreignRelics configuration reloaded!");
                return true;
            } else if (sub.equals("view") && args.length == 2) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                RelicMenu.openSharedMenu(player, target, plugin.getRelicManager());
                return true;
            } else if (sub.equals("edit") && args.length == 2) {
                if (!player.isOp()) {
                    player.sendMessage("§cOnly OPs can use the relic editor.");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                RelicMenu.openAdminTargetEquip(player, target, plugin.getRelicManager());
                return true;
            } else if (sub.equals("admin")) {
                if (!player.isOp()) {
                    player.sendMessage("§cOnly OPs can use the admin menu.");
                    return true;
                }
                RelicMenu.openAdminMain(player);
                return true;
            }
        }

        player.sendMessage("§6§lNightreign Relics Commands:");
        player.sendMessage(" §e/nr relics §7- Open main menu");
        if (player.hasPermission("nightreign.admin")) {
            player.sendMessage(" §e/nr reload §7- Reload configuration");
        }
        if (player.isOp()) {
            player.sendMessage("§e/nr admin §7- Open admin management menu");
            player.sendMessage(" §e/nr edit <player> §7- Edit player's relics");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("relics");
            if (sender.hasPermission("nightreign.admin")) {
                suggestions.add("reload");
            }
            if (sender.isOp()) {
                suggestions.add("edit");
                suggestions.add("admin");
            }
            suggestions.add("view");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return suggestions;
    }
}
