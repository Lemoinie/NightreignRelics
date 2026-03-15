package com.test;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RelicManager {

    public static final int MAX_STORAGE = 162;
    public static final int MAX_EQUIPPED = 6;
    public static final NamespacedKey RELIC_ID_KEY = new NamespacedKey("nightreign", "relic_id");

    private final Map<UUID, List<String>> playerRelics = new HashMap<>(); // List of unique relic IDs
    private final Map<String, RelicData> relicRegistry = new HashMap<>(); // registry[id] -> data
    private final Map<UUID, Set<String>> equippedRelics = new HashMap<>(); // equipped ids
    private final Map<UUID, Long> mirageCooldowns = new HashMap<>();
    private final Map<UUID, Integer> hitStreaks = new HashMap<>();
    private final Map<UUID, Long> lastHitTime = new HashMap<>();
    private final NightreignRelic plugin;

    public static class RelicData {
        private final String id;
        private final int customModelData;
        private final List<RelicEffect> effects;

        public RelicData(String id, int customModelData, List<RelicEffect> effects) {
            this.id = id;
            this.customModelData = customModelData;
            this.effects = effects;
        }

        public String getId() {
            return id;
        }

        public int getCustomModelData() {
            return customModelData;
        }

        public List<RelicEffect> getEffects() {
            return effects;
        }

        public ItemStack createItem() {
            for (RelicType type : RelicType.values()) {
                if (type.getCustomModelData() == customModelData) {
                    ItemStack item = type.createItem();
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.getPersistentDataContainer().set(RELIC_ID_KEY, PersistentDataType.STRING, id);
                        item.setItemMeta(meta);
                    }
                    return item;
                }
            }
            return new ItemStack(Material.PAPER);
        }
    }

    public RelicManager(NightreignRelic plugin) {
        this.plugin = plugin;
        loadGachaCosts();
        loadData();
    }

    public enum GachaOption {
        TIER_1("tier1"),
        TIER_2("tier2"),
        TIER_3("tier3");

        private final String configKey;
        private String name;
        private double price;
        private double pDelicate, pPolished, pGrand;

        GachaOption(String configKey) {
            this.configKey = configKey;
        }

        public void load(org.bukkit.configuration.file.FileConfiguration config) {
            this.name = config.getString("gacha." + configKey + ".name", "Unknown");
            this.price = config.getDouble("gacha." + configKey + ".price", 100.0);

            // Probabilities remain fixed for now or could also be configured
            if (configKey.equals("tier1")) {
                this.pDelicate = 0.70;
                this.pPolished = 0.25;
                this.pGrand = 0.05;
            } else if (configKey.equals("tier2")) {
                this.pDelicate = 0.33;
                this.pPolished = 0.34;
                this.pGrand = 0.33;
            } else {
                this.pDelicate = 0.10;
                this.pPolished = 0.30;
                this.pGrand = 0.60;
            }
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }
    }

    public void loadGachaCosts() {
        for (GachaOption option : GachaOption.values()) {
            option.load(plugin.getConfig());
        }
    }

    public RelicType rollGacha(GachaOption option) {
        double roll = Math.random();
        String targetCategory;

        if (roll < option.pDelicate) {
            targetCategory = "DELICATE";
        } else if (roll < option.pDelicate + option.pPolished) {
            targetCategory = "POLISHED";
        } else {
            targetCategory = "GRAND";
        }

        List<RelicType> matches = new ArrayList<>();
        for (RelicType type : RelicType.values()) {
            if (type.name().startsWith(targetCategory)) {
                matches.add(type);
            }
        }

        if (matches.isEmpty())
            return RelicType.DELICATE_BURNING; // Fallback
        return matches.get(new Random().nextInt(matches.size()));
    }

    public enum RelicType {
        GRAND_BURNING_1(1001, "Grand Burning Scene 1", 1000),
        GRAND_BURNING_2(1002, "Grand Burning Scene 2", 1000),
        GRAND_BURNING_3(1003, "Grand Burning Scene 3", 1000),
        POLISHED_BURNING(1004, "Polished Burning Scene", 600),
        DELICATE_BURNING(1005, "Delicate Burning Scene", 300),

        GRAND_TRANQUIL_1(1006, "Grand Tranquil Scene 1", 1000),
        GRAND_TRANQUIL_2(1007, "Grand Tranquil Scene 2", 1000),
        GRAND_TRANQUIL_3(1008, "Grand Tranquil Scene 3", 1000),
        POLISHED_TRANQUIL(1009, "Polished Tranquil Scene", 600),
        DELICATE_TRANQUIL(1010, "Delicate Tranquil Scene", 300),

        GRAND_LUMINOUS_1(1011, "Grand Luminous Scene 1", 1000),
        GRAND_LUMINOUS_2(1012, "Grand Luminous Scene 2", 1000),
        GRAND_LUMINOUS_3(1013, "Grand Luminous Scene 3", 1000),
        POLISHED_LUMINOUS(1014, "Polished Luminous Scene", 600),
        DELICATE_LUMINOUS(1015, "Delicate Luminous Scene", 300),

        GRAND_DRIZZLY_1(1016, "Grand Drizzly Scene 1", 1000),
        GRAND_DRIZZLY_2(1017, "Grand Drizzly Scene 2", 1000),
        GRAND_DRIZZLY_3(1018, "Grand Drizzly Scene 3", 1000),
        POLISHED_DRIZZLY(1019, "Polished Drizzly Scene", 600),
        DELICATE_DRIZZLY(1020, "Delicate Drizzly Scene", 300);

        private final int customModelData;
        private final String displayName;
        private final int price;

        RelicType(int customModelData, String displayName, int price) {
            this.customModelData = customModelData;
            this.displayName = displayName;
            this.price = price;
        }

        public int getCustomModelData() {
            return customModelData;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getPrice() {
            return price;
        }

        public ItemStack createItem() {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // itemName() sets minecraft:item_name component (1.20.5+)
                // We remove default italics so it looks like a clean custom item
                meta.itemName(Component.text(displayName).decoration(TextDecoration.ITALIC, false));
                meta.setCustomModelData(customModelData);
                item.setItemMeta(meta);
            }
            return item;
        }
    }

    public List<String> getPlayerRelics(UUID uuid) {
        loadPlayerDataIfNeeded(uuid);
        return playerRelics.getOrDefault(uuid, new ArrayList<>());
    }

    private void loadPlayerDataIfNeeded(UUID uuid) {
        if (!playerRelics.containsKey(uuid)) {
            loadPlayerData(uuid);
        }
    }

    public void addRelic(UUID uuid, int customModelData) {
        loadPlayerDataIfNeeded(uuid);
        List<String> relics = playerRelics.computeIfAbsent(uuid, k -> new ArrayList<>());
        if (relics.size() >= MAX_STORAGE) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null)
                player.sendMessage("§cYour relic collection is full (max 162)!");
            return;
        }

        RelicType type = getRelicType(customModelData);
        if (type != null) {
            int count = 0;
            if (type.name().startsWith("DELICATE"))
                count = 1;
            else if (type.name().startsWith("POLISHED"))
                count = 2;
            else if (type.name().startsWith("GRAND"))
                count = 3;

            addCustomRelic(uuid, customModelData, getRandomEffectsWithWeights(count));
        }
    }

    public void addCustomRelic(UUID uuid, int customModelData, List<RelicEffect> effects) {
        loadPlayerDataIfNeeded(uuid);
        List<String> relics = playerRelics.computeIfAbsent(uuid, k -> new ArrayList<>());
        if (relics.size() >= MAX_STORAGE)
            return;

        String relicId = UUID.randomUUID().toString();
        RelicData data = new RelicData(relicId, customModelData, new ArrayList<>(effects));
        relicRegistry.put(relicId, data);
        relics.add(relicId);
        savePlayerData(uuid);
    }

    public void removeRelic(UUID uuid, String relicId) {
        loadPlayerDataIfNeeded(uuid);
        List<String> relics = playerRelics.get(uuid);
        if (relics != null && relics.remove(relicId)) {
            relicRegistry.remove(relicId);
            Set<String> equipped = equippedRelics.get(uuid);
            if (equipped != null)
                equipped.remove(relicId);
            savePlayerData(uuid);
        }
    }

    public boolean isEquipped(UUID uuid, String relicId) {
        loadPlayerDataIfNeeded(uuid);
        Set<String> equipped = equippedRelics.get(uuid);
        return equipped != null && equipped.contains(relicId);
    }

    public Set<String> getEquippedRelics(UUID uuid) {
        loadPlayerDataIfNeeded(uuid);
        return equippedRelics.getOrDefault(uuid, new HashSet<>());
    }

    public RelicData getRelicData(String relicId) {
        return relicRegistry.get(relicId);
    }

    public void sellAllPlayerRelics(UUID uuid, EconomyManager eco) {
        loadPlayerDataIfNeeded(uuid);
        List<String> relics = new ArrayList<>(getPlayerRelics(uuid));
        double totalPayout = 0;

        for (String id : relics) {
            RelicData data = getRelicData(id);
            if (data != null) {
                RelicType type = getRelicType(data.getCustomModelData());
                if (type != null) {
                    totalPayout += type.getPrice() * 0.3;
                }
                removeRelic(uuid, id);
            }
        }

        if (totalPayout > 0) {
            eco.deposit(uuid, totalPayout);
        }
    }

    public void sellAllRelics(EconomyManager eco) {
        List<UUID> players = new ArrayList<>(playerRelics.keySet());
        for (UUID uuid : players) {
            sellAllPlayerRelics(uuid, eco);
        }
        playerRelics.clear();
        relicRegistry.clear();
        equippedRelics.clear();
    }

    public void updateRelicEffects(String relicId, List<RelicEffect> effects) {
        RelicData data = relicRegistry.get(relicId);
        if (data != null) {
            relicRegistry.put(relicId, new RelicData(relicId, data.getCustomModelData(), effects));
            // Sync attributes for anyone who has this relic equipped
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isEquipped(p.getUniqueId(), relicId)) {
                    plugin.getRelicEffectListener().updatePlayerAttributes(p);
                }
            }
            // Find owner and save
            for (Map.Entry<UUID, List<String>> entry : playerRelics.entrySet()) {
                if (entry.getValue().contains(relicId)) {
                    savePlayerData(entry.getKey());
                    break;
                }
            }
        }
    }

    public boolean isOnMirageCooldown(UUID uuid) {
        return mirageCooldowns.getOrDefault(uuid, 0L) > System.currentTimeMillis();
    }

    public void setMirageCooldown(UUID uuid) {
        mirageCooldowns.put(uuid, System.currentTimeMillis() + 60000);
    }

    public int incrementHitStreak(UUID uuid) {
        long now = System.currentTimeMillis();
        long last = lastHitTime.getOrDefault(uuid, 0L);
        int current = hitStreaks.getOrDefault(uuid, 0);

        if (now - last < 2000) { // 2 second window for successive hits
            current++;
        } else {
            current = 1;
        }

        hitStreaks.put(uuid, current);
        lastHitTime.put(uuid, now);
        return current;
    }

    public void toggleEquipRelic(UUID uuid, String relicId) {
        loadPlayerDataIfNeeded(uuid);
        Set<String> equipped = equippedRelics.computeIfAbsent(uuid, k -> new HashSet<>());
        if (equipped.contains(relicId)) {
            equipped.remove(relicId);
        } else {
            if (equipped.size() >= MAX_EQUIPPED) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null)
                    player.sendMessage("§cYou can only equip up to 6 relics!");
                return;
            }
            equipped.add(relicId);
        }
        savePlayerData(uuid);
        
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            plugin.getRelicEffectListener().updatePlayerAttributes(player);
        }
    }

    public void unequipRelic(UUID uuid, String relicId) {
        loadPlayerDataIfNeeded(uuid);
        Set<String> equipped = equippedRelics.get(uuid);
        if (equipped != null && equipped.remove(relicId)) {
            savePlayerData(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                plugin.getRelicEffectListener().updatePlayerAttributes(player);
            }
        }
    }

    public List<RelicEffect> getAllEquippedEffects(UUID uuid) {
        List<RelicEffect> allEffects = new ArrayList<>();
        for (String id : getEquippedRelics(uuid)) {
            RelicData data = relicRegistry.get(id);
            if (data != null) {
                allEffects.addAll(data.getEffects());
            }
        }
        return allEffects;
    }

    private List<RelicEffect> getRandomEffectsWithWeights(int count) {
        Map<RelicEffect, Integer> weights = new HashMap<>();
        weights.put(RelicEffect.MAX_HP, 20);
        weights.put(RelicEffect.MAX_FP, 20);
        weights.put(RelicEffect.RANGE_ATTACK, 20);
        weights.put(RelicEffect.MELEE_ATTACK, 20);
        weights.put(RelicEffect.UNDEAD_DAMAGE, 20);
        weights.put(RelicEffect.PHYSICAL_ATTACK, 20);
        weights.put(RelicEffect.PHYSICAL_ATTACK_1, 12);
        weights.put(RelicEffect.PHYSICAL_ATTACK_2, 6);
        weights.put(RelicEffect.PHYSICAL_ATTACK_3, 3);
        weights.put(RelicEffect.PHYSICAL_ATTACK_4, 1);

        weights.put(RelicEffect.POTION_PHYSICAL_1, 5);
        weights.put(RelicEffect.POTION_PHYSICAL_2, 2);

        weights.put(RelicEffect.TAKE_DAMAGE_BUFF_1, 10);
        weights.put(RelicEffect.TAKE_DAMAGE_BUFF_2, 5);

        weights.put(RelicEffect.CRIT_HIT, 10);
        weights.put(RelicEffect.CRIT_HIT_1, 5);

        weights.put(RelicEffect.POTION_DAMAGE, 15);

        weights.put(RelicEffect.CROSSBOW_POWER, 15);
        weights.put(RelicEffect.BOW_POWER, 15);
        weights.put(RelicEffect.SWORD_POWER, 15);
        weights.put(RelicEffect.AXE_POWER, 15);

        weights.put(RelicEffect.TWO_ARMAMENTS, 10);
        weights.put(RelicEffect.TWO_HANDING, 10);

        weights.put(RelicEffect.LIGHTNING_0, 10);
        weights.put(RelicEffect.LIGHTNING_1, 6);
        weights.put(RelicEffect.LIGHTNING_2, 3);
        weights.put(RelicEffect.LIGHTNING_3, 2);
        weights.put(RelicEffect.LIGHTNING_4, 1);

        weights.put(RelicEffect.FIRE_0, 10);
        weights.put(RelicEffect.FIRE_1, 6);
        weights.put(RelicEffect.FIRE_2, 3);
        weights.put(RelicEffect.FIRE_3, 2);
        weights.put(RelicEffect.FIRE_4, 1);

        weights.put(RelicEffect.SHIELD_SHOCKWAVE, 5);

        weights.put(RelicEffect.PHYSICAL_NEGATION_1, 10);
        weights.put(RelicEffect.PHYSICAL_NEGATION_2, 5);

        weights.put(RelicEffect.LIGHTNING_NEGATION_1, 8);
        weights.put(RelicEffect.LIGHTNING_NEGATION_2, 4);

        weights.put(RelicEffect.FIRE_NEGATION_1, 8);
        weights.put(RelicEffect.FIRE_NEGATION_2, 4);

        weights.put(RelicEffect.AFFINITY_NEGATION, 12);
        weights.put(RelicEffect.AFFINITY_NEGATION_1, 6);
        weights.put(RelicEffect.AFFINITY_NEGATION_2, 3);

        // New effects
        weights.put(RelicEffect.RUNE_ACQUISITION, 10);
        weights.put(RelicEffect.XP_ACQUISITION, 10);
        weights.put(RelicEffect.POISE_1, 10);
        weights.put(RelicEffect.POISE_2, 5);
        weights.put(RelicEffect.POISE_3, 2);
        weights.put(RelicEffect.VIGOR_1, 10);
        weights.put(RelicEffect.VIGOR_2, 5);
        weights.put(RelicEffect.VIGOR_3, 2);
        weights.put(RelicEffect.MIND_1, 10);
        weights.put(RelicEffect.MIND_2, 5);
        weights.put(RelicEffect.MIND_3, 2);
        weights.put(RelicEffect.IMPROVED_RESTORATION, 8);
        weights.put(RelicEffect.GUARD_RECOVERY, 8);
        weights.put(RelicEffect.SUCCESSIVE_ATTACK_RECOVERY, 8);
        weights.put(RelicEffect.COLD_MIRAGE, 1);

        List<RelicEffect> selected = new ArrayList<>();
        List<RelicEffect> pool = new ArrayList<>(weights.keySet());
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            if (pool.isEmpty())
                break;

            int totalWeight = 0;
            for (RelicEffect effect : pool)
                totalWeight += weights.get(effect);

            int r = random.nextInt(totalWeight);
            int currentWeight = 0;
            for (RelicEffect effect : pool) {
                currentWeight += weights.get(effect);
                if (r < currentWeight) {
                    selected.add(effect);
                    RelicEffect.Category cat = effect.getCategory();
                    String root = effect.getRootName();
                    // Avoid same category AND same root type
                    pool.removeIf(e -> e.getCategory() == cat || e.getRootName().equals(root));
                    break;
                }
            }
        }
        return selected;
    }

    public RelicType getRelicType(int cmd) {
        for (RelicType type : RelicType.values()) {
            if (type.getCustomModelData() == cmd)
                return type;
        }
        return null;
    }

    public boolean hasRelic(UUID uuid, int customModelData) {
        loadPlayerDataIfNeeded(uuid);
        for (String id : getPlayerRelics(uuid)) {
            RelicData data = relicRegistry.get(id);
            if (data != null && data.getCustomModelData() == customModelData)
                return true;
        }
        return false;
    }

    public void saveData() {
        // Global flush on shutdown or manual save
        for (UUID uuid : playerRelics.keySet()) {
            savePlayerData(uuid);
        }
    }

    public void savePlayerData(UUID uuid) {
        File playerDir = new File(plugin.getDataFolder(), "players");
        if (!playerDir.exists()) playerDir.mkdirs();

        File file = new File(playerDir, uuid.toString() + ".yml");
        File tempFile = new File(playerDir, uuid.toString() + ".yml.tmp");
        
        YamlConfiguration config = new YamlConfiguration();

        List<String> relics = playerRelics.get(uuid);
        if (relics == null || relics.isEmpty()) {
            if (file.exists()) file.delete();
            return;
        }

        config.set("relics", relics);
        config.set("equipped", new ArrayList<>(equippedRelics.getOrDefault(uuid, new HashSet<>())));

        ConfigurationSection registry = config.createSection("registry");
        for (String id : relics) {
            RelicData data = relicRegistry.get(id);
            if (data != null) {
                ConfigurationSection section = registry.createSection(id);
                section.set("cmd", data.getCustomModelData());
                List<String> rawEffects = new ArrayList<>();
                for (RelicEffect eff : data.getEffects()) rawEffects.add(eff.name());
                section.set("effects", rawEffects);
            }
        }

        try {
            config.save(tempFile);
            if (file.exists()) file.delete();
            tempFile.renameTo(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data for player " + uuid);
        }
    }

    public void loadData() {
        File legacyFile = new File(plugin.getDataFolder(), "relics.yml");
        if (legacyFile.exists()) {
            migrateLegacyData(legacyFile);
        }
    }

    private void migrateLegacyData(File file) {
        plugin.getLogger().info("Migrating legacy relics.yml to individual player files...");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Use temporary maps to load everything first
        Map<String, RelicData> tempRegistry = new HashMap<>();
        ConfigurationSection regSection = config.getConfigurationSection("registry");
        if (regSection != null) {
            for (String id : regSection.getKeys(false)) {
                ConfigurationSection s = regSection.getConfigurationSection(id);
                if (s != null) {
                    List<RelicEffect> effects = new ArrayList<>();
                    for (String eName : s.getStringList("effects")) {
                        try { effects.add(RelicEffect.valueOf(eName)); } catch (Exception ignored) {}
                    }
                    tempRegistry.put(id, new RelicData(id, s.getInt("cmd"), effects));
                }
            }
        }

        ConfigurationSection playersSection = config.getConfigurationSection("players");
        ConfigurationSection equippedSection = config.getConfigurationSection("equipped");

        if (playersSection != null) {
            for (String uuidStr : playersSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> relics = playersSection.getStringList(uuidStr);
                playerRelics.put(uuid, relics);
                for (String id : relics) {
                    RelicData data = tempRegistry.get(id);
                    if (data != null) relicRegistry.put(id, data);
                }
                
                if (equippedSection != null && equippedSection.contains(uuidStr)) {
                    equippedRelics.put(uuid, new HashSet<>(equippedSection.getStringList(uuidStr)));
                }
                
                savePlayerData(uuid);
            }
        }

        file.renameTo(new File(plugin.getDataFolder(), "relics.yml.bak"));
        plugin.getLogger().info("Migration complete. Legacy file backed up to relics.yml.bak");
        
        // Clear memory if necessary, though we just loaded it
    }

    public void loadPlayerData(UUID uuid) {
        File file = new File(plugin.getDataFolder(), "players/" + uuid + ".yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> relics = config.getStringList("relics");
        playerRelics.put(uuid, relics);
        equippedRelics.put(uuid, new HashSet<>(config.getStringList("equipped")));

        ConfigurationSection registry = config.getConfigurationSection("registry");
        if (registry != null) {
            for (String id : registry.getKeys(false)) {
                ConfigurationSection s = registry.getConfigurationSection(id);
                if (s != null) {
                    List<RelicEffect> effects = new ArrayList<>();
                    for (String eName : s.getStringList("effects")) {
                        try { effects.add(RelicEffect.valueOf(eName)); } catch (Exception ignored) {}
                    }
                    relicRegistry.put(id, new RelicData(id, s.getInt("cmd"), effects));
                }
            }
        }
    }

    public void unloadPlayerData(UUID uuid) {
        List<String> relics = playerRelics.remove(uuid);
        if (relics != null) {
            for (String id : relics) relicRegistry.remove(id);
        }
        equippedRelics.remove(uuid);
        mirageCooldowns.remove(uuid);
        hitStreaks.remove(uuid);
        lastHitTime.remove(uuid);
    }
}
