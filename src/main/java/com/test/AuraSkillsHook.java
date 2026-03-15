package com.test;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AuraSkillsHook {

    private final boolean enabled;

    public AuraSkillsHook() {
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("AuraSkills");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void applyFPModifier(Player player, double percent) {
        if (!enabled) return;
        SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
        if (user != null && user.isLoaded()) {
            user.addStatModifier(new StatModifier("RelicFP", Stats.WISDOM, percent * 100.0, AuraSkillsModifier.Operation.ADD_PERCENT));
        }
    }

    public void removeFPModifier(Player player) {
        if (!enabled) return;
        SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
        if (user != null && user.isLoaded()) {
            user.removeStatModifier("RelicFP");
        }
    }

    public void applyManaModifier(Player player, double amount) {
        if (!enabled) return;
        SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
        if (user != null && user.isLoaded()) {
            // Stats.WISDOM scales mana in AuraSkills. We apply a modifier to emulate flat mana.
            user.addStatModifier(new StatModifier("RelicMana", Stats.WISDOM, amount / 2.0, AuraSkillsModifier.Operation.ADD));
        }
    }

    public void removeManaModifier(Player player) {
        if (!enabled) return;
        SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
        if (user != null && user.isLoaded()) {
            user.removeStatModifier("RelicMana");
        }
    }
}
