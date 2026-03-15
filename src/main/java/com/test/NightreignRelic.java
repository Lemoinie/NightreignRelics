package com.test;

import org.bukkit.plugin.java.JavaPlugin;

public class NightreignRelic extends JavaPlugin {

    private RelicManager relicManager;
    private EconomyManager economyManager;
    private AuraSkillsHook auraSkillsHook;
    private RelicEffectListener relicEffectListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.auraSkillsHook = new AuraSkillsHook();
        this.relicEffectListener = new RelicEffectListener(this);
        this.relicManager = new RelicManager(this);
        this.economyManager = new EconomyManager(this);
        
        getServer().getPluginManager().registerEvents(relicEffectListener, this);

        RelicCommand relicCmd = new RelicCommand(this);
        getCommand("nr").setExecutor(relicCmd);
        getCommand("nr").setTabCompleter(relicCmd);
        getServer().getPluginManager().registerEvents(new RelicListener(this), this);

        getLogger().info("NightreignRelic with Relic System & Economy has been enabled!");
    }

    public RelicManager getRelicManager() {
        return relicManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public AuraSkillsHook getAuraSkillsHook() {
        return auraSkillsHook;
    }

    public RelicEffectListener getRelicEffectListener() {
        // Need to store the listener instance
        return relicEffectListener;
    }

    @Override
    public void onDisable() {
        if (relicManager != null) {
            relicManager.saveData();
        }
        getLogger().info("NightreignRelic has been disabled!");
    }
}
