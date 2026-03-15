package com.test;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final NightreignRelic plugin;
    private Economy vaultEconomy = null;

    public EconomyManager(NightreignRelic plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found. Using XP as currency.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().info("No Economy provider found in Vault. Using XP as currency.");
            return;
        }
        vaultEconomy = rsp.getProvider();
        plugin.getLogger().info("Vault Economy hooked successfully!");
    }

    public boolean hasVault() {
        return vaultEconomy != null;
    }

    public boolean hasEnough(Player player, double amount) {
        if (hasVault()) {
            return vaultEconomy.has(player, amount);
        } else {
            return player.getLevel() >= (int) amount;
        }
    }

    public boolean withdraw(Player player, double amount) {
        if (hasVault()) {
            return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
        } else {
            if (player.getLevel() >= (int) amount) {
                player.setLevel(player.getLevel() - (int) amount);
                return true;
            }
            return false;
        }
    }

    public boolean deposit(Player player, double amount) {
        return deposit((org.bukkit.OfflinePlayer) player, amount);
    }

    public boolean deposit(java.util.UUID uuid, double amount) {
        return deposit(Bukkit.getOfflinePlayer(uuid), amount);
    }

    public boolean deposit(org.bukkit.OfflinePlayer player, double amount) {
        double finalAmount = amount;
        if (player.isOnline() && player.getPlayer() != null) {
            Player p = player.getPlayer();
            RelicManager manager = plugin.getRelicManager();
            if (manager.getAllEquippedEffects(p.getUniqueId()).contains(RelicEffect.RUNE_ACQUISITION)) {
                finalAmount *= 1.035; // 3.5% bonus
            }
        }

        if (hasVault()) {
            return vaultEconomy.depositPlayer(player, finalAmount).transactionSuccess();
        } else {
            if (player.isOnline() && player.getPlayer() != null) {
                Player p = player.getPlayer();
                p.setLevel(p.getLevel() + (int) finalAmount);
                return true;
            }
            return false;
        }
    }

    public String getCurrencyName(double amount) {
        if (hasVault()) {
            return vaultEconomy.format(amount);
        } else {
            return (int) amount + " XP Levels";
        }
    }
}
