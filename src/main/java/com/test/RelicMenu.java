package com.test;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class RelicMenu {

    public static final String MAIN_TITLE = "§8Relics Menu";
    public static final String BUY_TITLE = "§8Buy Relics";
    public static final String EQUIP_TITLE = "§8Equip Relics";

    public static final String GACHA_TITLE = "§8Select Buying Option";
    public static final String AMOUNT_TITLE = "§8Select Amount: ";
    public static final String ANIMATION_TITLE = "§8Rolling...";

    public static final String RITE_TITLE = "§8Relics Rite";
    public static final String SHARED_TITLE = "§8Shared Relics: ";
    public static final String ADMIN_EDITOR_TITLE = "§c§lAdmin Editor: ";
    public static final String ADMIN_MAIN_TITLE = "§c§lAdmin Management";
    public static final String PLAYER_LIST_TITLE = "§c§lManage Players";
    public static final String GLOBAL_ACTIONS_TITLE = "§c§lGlobal Actions";
    public static final String CUSTOM_CREATOR_TITLE = "§c§lCustom Relic Creator";

    public enum LoreContext {
        USER,
        ADMIN
    }

    public static final int RETURN_SLOT = 26; // Bottom right for 3-row, adjusted for others

    public static void openRelicMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_TITLE);

        inv.setItem(10, createMenuItem(Material.GOLD_INGOT, "§e§lBuy Relics", "§7Purchase a random relic from one of three tiers."));
        inv.setItem(13, createMenuItem(Material.CHEST, "§a§lEquip Relics", "§7Manage your personal containment."));
        inv.setItem(16, createMenuItem(Material.ENCHANTED_BOOK, "§d§lRelics Rite", "§7View your equipped relics and share them."));

        player.openInventory(inv);
    }

    public static void openGachaMenu(Player player, EconomyManager eco) {
        Inventory inv = Bukkit.createInventory(null, 27, GACHA_TITLE);

        int[] slots = {10, 13, 16};
        RelicManager.GachaOption[] options = RelicManager.GachaOption.values();

        for (int i = 0; i < 3; i++) {
            RelicManager.GachaOption opt = options[i];
            String price = eco.getCurrencyName(opt.getPrice());
            inv.setItem(slots[i], createMenuItem(Material.AMETHYST_SHARD, "§d§l" + opt.getName(), 
                "§7Price: §e" + price, "", "§eClick to buy!"));
        }

        addReturnButton(inv);
        player.openInventory(inv);
    }

    // ... (openBuyMenu keep it or add return)
    public static void openBuyMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, BUY_TITLE);
        // ... items ...
        inv.setItem(35, createMenuItem(Material.BARRIER, "§c§lReturn", "§7Go back to main menu"));
        player.openInventory(inv);
    }

    public static void openEquipMenu(Player player, RelicManager manager) {
        Inventory inv = Bukkit.createInventory(null, 54, EQUIP_TITLE);

        List<String> relicIds = manager.getPlayerRelics(player.getUniqueId());
        for (String id : relicIds) {
            RelicManager.RelicData data = manager.getRelicData(id);
            if (data != null) {
                ItemStack item = data.createItem();
                applyStandardLore(item, data, player.getUniqueId(), manager, true, LoreContext.USER);
                inv.addItem(item);
            }
        }

        inv.setItem(53, createMenuItem(Material.BARRIER, "§c§lReturn", "§7Go back to main menu"));
        player.openInventory(inv);
    }

    public static void openRelicsRite(Player player, RelicManager manager) {
        Inventory inv = Bukkit.createInventory(null, 27, RITE_TITLE);

        Set<String> equipped = manager.getEquippedRelics(player.getUniqueId());
        int[] slots = {10, 11, 12, 14, 15, 16};
        int i = 0;
        for (String id : equipped) {
            if (i >= slots.length) break;
            RelicManager.RelicData data = manager.getRelicData(id);
            if (data != null) {
                ItemStack item = data.createItem();
                applyStandardLore(item, data, player.getUniqueId(), manager, false, LoreContext.USER);
                inv.setItem(slots[i++], item);
            }
        }

        inv.setItem(22, createMenuItem(Material.PAPER, "§b§lShare to Chat", "§7Click to show everyone your relics!"));
        addReturnButton(inv);
        player.openInventory(inv);
    }

    public static void openSharedMenu(Player viewer, Player target, RelicManager manager) {
        Inventory inv = Bukkit.createInventory(null, 27, SHARED_TITLE + target.getName());

        Set<String> equipped = manager.getEquippedRelics(target.getUniqueId());
        int[] slots = {10, 11, 12, 14, 15, 16};
        int i = 0;
        for (String id : equipped) {
            if (i >= slots.length) break;
            RelicManager.RelicData data = manager.getRelicData(id);
            if (data != null) {
                ItemStack item = data.createItem();
                applyStandardLore(item, data, target.getUniqueId(), manager, false, LoreContext.USER);
                inv.setItem(slots[i++], item);
            }
        }

        viewer.openInventory(inv);
    }

    public static void updateItemInPlace(ItemStack item, RelicManager.RelicData data, UUID owner, RelicManager manager, boolean showSellInfo, LoreContext context) {
        applyStandardLore(item, data, owner, manager, showSellInfo, context);
    }

    private static void applyStandardLore(ItemStack item, RelicManager.RelicData data, UUID owner, RelicManager manager, boolean showSellInfo, LoreContext context) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();
        
        // Add Effects
        List<RelicEffect> effects = data.getEffects();
        if (!effects.isEmpty()) {
            lore.add("§d§lEffects:");
            for (RelicEffect effect : effects) {
                lore.add(" §7- " + effect.getDescription());
            }
            lore.add("");
        }
        
        boolean isEquipped = manager.isEquipped(owner, data.getId());
        if (context == LoreContext.USER) {
            if (isEquipped) {
                lore.add("§a§lEQUIPPED");
                lore.add("§7Right-click to unequip");
                meta.setEnchantmentGlintOverride(true);
            } else if (showSellInfo) {
                lore.add("§eClick to equip!");
            }
        }

        if (showSellInfo || context == LoreContext.ADMIN) {
            // Sell info
            RelicManager.RelicType type = manager.getRelicType(data.getCustomModelData());
            if (type != null) {
                int sellPrice = (int) (type.getPrice() * 0.3);
                lore.add("§6Shift-Click to sell for " + sellPrice + "!");
            }
        }

        if (context == LoreContext.ADMIN) {
            lore.add("§c§lOP: §eShift+Right-click to edit effects");
        } else if (Bukkit.getPlayer(owner) != null && Bukkit.getPlayer(owner).isOp()) {
            lore.add("§7§oAdmin management available in /nr admin");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private static void addReturnButton(Inventory inv) {
        inv.setItem(RETURN_SLOT, createMenuItem(Material.BARRIER, "§c§lReturn", "§7Go back to main menu"));
    }

    public static void startGachaAnimation(Player player, RelicManager.GachaOption option, int amount, NightreignRelic plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, ANIMATION_TITLE);
        
        ItemStack glass = createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
            }
        }
        inv.setItem(53, createMenuItem(Material.BARRIER, "§c§lReturn to Gacha Menu", "§7Click to go back"));
        
        player.openInventory(inv);
        
        RelicManager.RelicType[] results = new RelicManager.RelicType[amount];
        for (int i = 0; i < amount; i++) {
            results[i] = plugin.getRelicManager().rollGacha(option);
        }
        
        int[] slots = new int[amount];
        for (int i = 0; i < amount; i++) {
            int row = i / 7;
            int col = i % 7;
            slots[i] = 10 + row * 9 + col;
        }

        new BukkitRunnable() {
            int ticks = 0;
            int maxTicks = 40;
            Random random = new Random();
            
            @Override
            public void run() {
                if (!player.getOpenInventory().getTitle().equals(ANIMATION_TITLE)) {
                    this.cancel();
                    return;
                }

                if (ticks >= maxTicks) {
                    for (int i = 0; i < amount; i++) {
                        inv.setItem(slots[i], results[i].createItem());
                        plugin.getRelicManager().addRelic(player.getUniqueId(), results[i].getCustomModelData());
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    player.sendMessage("§aGacha complete! Check your collection.");
                    this.cancel();
                    return;
                }
                
                for (int i = 0; i < amount; i++) {
                    RelicManager.RelicType randomType = RelicManager.RelicType.values()[random.nextInt(RelicManager.RelicType.values().length)];
                    inv.setItem(slots[i], randomType.createItem());
                }
                
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 0.5f + (ticks / (float)maxTicks));
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    public static void openAmountSelectionMenu(Player player, RelicManager.GachaOption option, int amount) {
        Inventory inv = Bukkit.createInventory(null, 27, AMOUNT_TITLE + option.name());

        inv.setItem(11, createMenuItem(Material.RED_STAINED_GLASS_PANE, "§c-1", "§7Decrease amount by 1"));
        inv.setItem(12, createMenuItem(Material.ORANGE_STAINED_GLASS_PANE, "§6-5", "§7Decrease amount by 5"));
        
        ItemStack info = createMenuItem(Material.AMETHYST_SHARD, "§d§l" + option.getName(), 
            "§7Selected Amount: §e" + amount,
            "§7Total Price: §e" + (option.getPrice() * amount),
            "", "§eAdjust amount using buttons");
        inv.setItem(13, info);

        inv.setItem(14, createMenuItem(Material.YELLOW_STAINED_GLASS_PANE, "§e+5", "§7Increase amount by 5"));
        inv.setItem(15, createMenuItem(Material.LIME_STAINED_GLASS_PANE, "§a+1", "§7Increase amount by 1"));

        inv.setItem(22, createMenuItem(Material.GREEN_STAINED_GLASS, "§a§lCONFIRM", "§7Click to start gacha!"));

        addReturnButton(inv);
        player.openInventory(inv);
    }

    private static ItemStack createMenuItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>(List.of(loreLines));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void openAdminTargetEquip(Player admin, OfflinePlayer target, RelicManager manager) {
        Inventory inv = Bukkit.createInventory(null, 54, "§c§lEditing: " + target.getName());

        List<String> relicIds = manager.getPlayerRelics(target.getUniqueId());
        for (String id : relicIds) {
            RelicManager.RelicData data = manager.getRelicData(id);
            if (data != null) {
                ItemStack item = data.createItem();
                // We show target's relics to the admin context
                applyStandardLore(item, data, target.getUniqueId(), manager, true, LoreContext.ADMIN);
                inv.addItem(item);
            }
        }

        inv.setItem(53, createMenuItem(Material.BARRIER, "§c§lBACK", "§7Return to player list"));
        admin.openInventory(inv);
    }

    public static void openAdminEditor(Player player, String relicId, UUID ownerUuid, RelicManager manager, int page) {
        RelicManager.RelicData data = manager.getRelicData(relicId);
        if (data == null) return;

        String ownerName = Bukkit.getOfflinePlayer(ownerUuid).getName();
        Inventory inv = Bukkit.createInventory(null, 54, ADMIN_EDITOR_TITLE + ownerName + " - " + relicId.substring(0, 8));
        
        // Fill background
        ItemStack glass = createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        // Show current relic at top
        ItemStack preview = data.createItem();
        ItemMeta meta = preview.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§d§lCurrent Effects:");
            for (RelicEffect eff : data.getEffects()) {
                lore.add(" §7- " + eff.getName());
            }
            meta.setLore(lore);
            preview.setItemMeta(meta);
        }
        inv.setItem(4, preview);

        // Group effects by category
        RelicEffect[] allEffects = RelicEffect.values();
        int slot = 9;
        RelicEffect.Category lastCategory = null;

        for (RelicEffect effect : allEffects) {
            if (slot >= 45) break; // Limit to 4 rows for effects for now

            if (lastCategory != effect.getCategory()) {
                lastCategory = effect.getCategory();
                // We could skip a slot or add a separator here if we had space
            }

            boolean hasEffect = data.getEffects().contains(effect);
            Material mat = hasEffect ? Material.ENCHANTED_BOOK : Material.BOOK;
            
            // Color mapping based on category
            String color = switch (effect.getCategory()) {
                case STATS -> "§b";
                case ATTACK -> "§c";
                case TRIGGERED -> "§6";
                case ELEMENTAL -> "§3";
                case DEFENSIVE -> "§a";
                case UTILITY -> "§d";
                case REGEN -> "§2"; // Dark Green for regen
                case SPECIAL -> "§b";
            };

            ItemStack item = new ItemStack(mat);
            ItemMeta eMeta = item.getItemMeta();
            if (eMeta != null) {
                eMeta.setDisplayName(color + "§l" + effect.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§8" + effect.name());
                lore.add("§7Category: " + color + effect.getCategory().name());
                lore.add("");
                lore.add(effect.getDescription());
                lore.add("");
                lore.add(hasEffect ? "§cClick to remove" : "§aClick to add");
                eMeta.setLore(lore);
                item.setItemMeta(eMeta);
            }
            inv.setItem(slot++, item);
        }

        inv.setItem(49, createMenuItem(Material.BARRIER, "§c§lBACK", "§7Return to collection"));
        
        player.openInventory(inv);
    }

    public static void openAdminMain(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 27, ADMIN_MAIN_TITLE);

        inv.setItem(10, createMenuItem(Material.PLAYER_HEAD, "§e§lManage Players", "§7Edit effects or sell relics from other players."));
        inv.setItem(13, createMenuItem(Material.NETHER_STAR, "§b§lCustom Relic Creator", "§7Create a custom relic with specific effects."));
        inv.setItem(16, createMenuItem(Material.TNT, "§c§lGlobal Actions", "§7Perform bulk actions like 'Sell All'."));

        addReturnButton(inv);
        admin.openInventory(inv);
    }

    public static void openPlayerList(Player admin, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, PLAYER_LIST_TITLE);
        
        File playerDir = new File(Bukkit.getPluginManager().getPlugin("NightreignRelics").getDataFolder(), "players");
        List<OfflinePlayer> allPlayers = new ArrayList<>();
        if (playerDir.exists()) {
            File[] files = playerDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File f : files) {
                    try {
                        UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
                        allPlayers.add(Bukkit.getOfflinePlayer(uuid));
                    } catch (Exception ignored) {}
                }
            }
        }
        
        // Also add online players who might not have files yet
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (allPlayers.stream().noneMatch(op -> op.getUniqueId().equals(p.getUniqueId()))) {
                allPlayers.add(p);
            }
        }

        int startIndex = page * 45;
        for (int i = 0; i < 45 && (startIndex + i) < allPlayers.size(); i++) {
            OfflinePlayer p = allPlayers.get(startIndex + i);
            String status = p.isOnline() ? "§a(Online)" : "§7(Offline)";
            inv.setItem(i, createMenuItem(Material.PLAYER_HEAD, "§f" + p.getName(), status, "§7Click to manage relics"));
        }

        if (page > 0) inv.setItem(45, createMenuItem(Material.ARROW, "§ePrevious Page", ""));
        if (startIndex + 45 < allPlayers.size()) inv.setItem(53, createMenuItem(Material.ARROW, "§eNext Page", ""));
        
        inv.setItem(49, createMenuItem(Material.BARRIER, "§c§lBACK", "§7Return to admin menu"));
        admin.openInventory(inv);
    }

    public static void openAdminGlobalActions(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 27, GLOBAL_ACTIONS_TITLE);

        inv.setItem(13, createMenuItem(Material.BARRIER, "§c§lSELL ALL RELICS", "§7DANGEROUS: Sells all relics from ALL players.", "§7Gives owners 30% of base price."));

        addReturnButton(inv);
        admin.openInventory(inv);
    }

    public static void openCustomCreatorBase(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 27, CUSTOM_CREATOR_TITLE + ": Select Base");
        
        for (RelicManager.RelicType type : RelicManager.RelicType.values()) {
            inv.addItem(type.createItem());
        }

        addReturnButton(inv);
        admin.openInventory(inv);
    }

}
