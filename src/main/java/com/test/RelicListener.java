package com.test;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RelicListener implements Listener {

    private final NightreignRelic plugin;

    public RelicListener(NightreignRelic plugin) {
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        plugin.getRelicManager().loadPlayerData(event.getPlayer().getUniqueId());
    }

    @org.bukkit.event.EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        plugin.getRelicManager().savePlayerData(uuid);
        plugin.getRelicManager().unloadPlayerData(uuid);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType().isAir()) return;

        // Check Return button first in all menus
        if (clicked.getType() == org.bukkit.Material.BARRIER) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.getDisplayName().contains("Return")) {
                event.setCancelled(true);
                RelicMenu.openRelicMain(player);
                return;
            }
        }

        if (title.equals(RelicMenu.MAIN_TITLE)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 10) {
                RelicMenu.openGachaMenu(player, plugin.getEconomyManager());
            } else if (slot == 13) {
                RelicMenu.openEquipMenu(player, plugin.getRelicManager());
            } else if (slot == 16) {
                RelicMenu.openRelicsRite(player, plugin.getRelicManager());
            }
        } else if (title.equals(RelicMenu.GACHA_TITLE)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            RelicManager.GachaOption option = null;
            if (slot == 10) option = RelicManager.GachaOption.TIER_1;
            else if (slot == 13) option = RelicManager.GachaOption.TIER_2;
            else if (slot == 16) option = RelicManager.GachaOption.TIER_3;

            if (option != null) {
                RelicMenu.openAmountSelectionMenu(player, option, 1);
            }
        } else if (title.startsWith(RelicMenu.AMOUNT_TITLE)) {
            event.setCancelled(true);
            String optionName = title.substring(RelicMenu.AMOUNT_TITLE.length());
            RelicManager.GachaOption option = RelicManager.GachaOption.valueOf(optionName);
            
            ItemStack infoItem = event.getInventory().getItem(13);
            if (infoItem == null) return;
            ItemMeta meta = infoItem.getItemMeta();
            if (meta == null) return;
            
            String amountLine = meta.getLore().get(0);
            int currentAmount = Integer.parseInt(amountLine.split(": §e")[1]);
            int slot = event.getSlot();

            if (slot == 11) currentAmount = Math.max(1, currentAmount - 1);
            else if (slot == 12) currentAmount = Math.max(1, currentAmount - 5);
            else if (slot == 14) currentAmount = Math.min(27, currentAmount + 5);
            else if (slot == 15) currentAmount = Math.min(27, currentAmount + 1);
            else if (slot == 22) {
                // Confirm
                EconomyManager eco = plugin.getEconomyManager();
                double totalPrice = option.getPrice() * currentAmount;
                if (eco.hasEnough(player, totalPrice)) {
                    eco.withdraw(player, totalPrice);
                    RelicMenu.startGachaAnimation(player, option, currentAmount, plugin);
                } else {
                    player.sendMessage("§cYou don't have enough " + (eco.hasVault() ? "money" : "XP") + "!");
                }
                return;
            }

            RelicMenu.openAmountSelectionMenu(player, option, currentAmount);
        } else if (title.equals(RelicMenu.ANIMATION_TITLE)) {
            event.setCancelled(true);
            if (event.getSlot() == 53) {
                RelicMenu.openGachaMenu(player, plugin.getEconomyManager());
            }
        } else if (title.equals(RelicMenu.EQUIP_TITLE)) {
            event.setCancelled(true);
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(RelicManager.RELIC_ID_KEY, PersistentDataType.STRING)) {
                String relicId = meta.getPersistentDataContainer().get(RelicManager.RELIC_ID_KEY, PersistentDataType.STRING);
                RelicManager manager = plugin.getRelicManager();
                
                if (event.isShiftClick() && event.isRightClick() && player.isOp()) {
                    // Admin Editor
                    RelicMenu.openAdminEditor(player, relicId, player.getUniqueId(), manager, 0);
                } else if (event.isShiftClick()) {
                    // Sell
                    if (manager.isEquipped(player.getUniqueId(), relicId)) {
                        player.sendMessage("§cYou cannot sell an equipped relic!");
                        return;
                    }
                    
                    RelicManager.RelicData data = manager.getRelicData(relicId);
                    if (data != null) {
                        RelicManager.RelicType type = manager.getRelicType(data.getCustomModelData());
                        if (type != null) {
                            double sellPrice = type.getPrice() * 0.3;
                            manager.removeRelic(player.getUniqueId(), relicId);
                            plugin.getEconomyManager().deposit(player, sellPrice);
                            player.sendMessage("§aYou sold the relic for " + plugin.getEconomyManager().getCurrencyName(sellPrice) + "!");
                            RelicMenu.openEquipMenu(player, manager); // Refresh
                        }
                    }
                } else if (event.isRightClick()) {
                    // Force Unequip
                    manager.unequipRelic(player.getUniqueId(), relicId);
                    // In-place refresh
                    RelicManager.RelicData data = manager.getRelicData(relicId);
                    if (data != null) {
                        RelicMenu.updateItemInPlace(clicked, data, player.getUniqueId(), manager, true, RelicMenu.LoreContext.USER);
                    }
                } else {
                    // Toggle Equip
                    manager.toggleEquipRelic(player.getUniqueId(), relicId);
                    // In-place refresh
                    RelicManager.RelicData data = manager.getRelicData(relicId);
                    if (data != null) {
                        RelicMenu.updateItemInPlace(clicked, data, player.getUniqueId(), manager, true, RelicMenu.LoreContext.USER);
                    }
                }
            }
        } else if (title.equals(RelicMenu.RITE_TITLE)) {
            event.setCancelled(true);
            if (event.getSlot() == 22) {
                // Share to Chat
                TextComponent message = new TextComponent("§d§l[RELICS] §f" + player.getName() + " shared their equipped relics! §e§l[CLICK TO VIEW]");
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nr view " + player.getName()));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click to view " + player.getName() + "'s relics").create()));
                
                 Bukkit.spigot().broadcast(message);
                player.sendMessage("§aShared your relics to chat!");
                player.closeInventory();
            }
        } else if (title.startsWith(RelicMenu.SHARED_TITLE)) {
            event.setCancelled(true);
        } else if (title.startsWith(RelicMenu.ADMIN_EDITOR_TITLE)) {
            event.setCancelled(true);
            RelicManager manager = plugin.getRelicManager();
            String fullRelicId = null;
            ItemStack preview = event.getInventory().getItem(4);
            if (preview != null && preview.getItemMeta() != null) {
                fullRelicId = preview.getItemMeta().getPersistentDataContainer().get(RelicManager.RELIC_ID_KEY, PersistentDataType.STRING);
            }
            
            if (fullRelicId == null) return;
            RelicManager.RelicData data = manager.getRelicData(fullRelicId);
            if (data == null) return;

            int slot = event.getSlot();
            if (slot == 49) {
                // Return logic: check if we are editing ourselves or someone else
                String ownerName = title.substring(RelicMenu.ADMIN_EDITOR_TITLE.length()).split(" - ")[0];
                if (ownerName.equalsIgnoreCase(player.getName())) {
                    RelicMenu.openEquipMenu(player, manager);
                } else {
                    Player target = Bukkit.getPlayer(ownerName);
                    if (target != null) {
                        RelicMenu.openAdminTargetEquip(player, target, manager);
                    } else {
                        player.closeInventory();
                    }
                }
                return;
            }

            if (slot >= 9 && slot < 54) {
                ItemStack item = event.getCurrentItem();
                if (item == null || item.getType() == Material.AIR) return;
                
                if (item.getType() == Material.ARROW) {
                    int currentPage = 0; // Simplified pagination for now
                    RelicMenu.openAdminEditor(player, fullRelicId, player.getUniqueId(), manager, slot == 45 ? currentPage - 1 : currentPage + 1);
                    return;
                }

                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;
                
                String effectName = meta.getLore().get(0).substring(2); // Remove "§8"
                try {
                    RelicEffect effect = RelicEffect.valueOf(effectName);
                    List<RelicEffect> effects = new ArrayList<>(data.getEffects());
                    if (effects.contains(effect)) {
                        effects.remove(effect);
                        player.sendMessage("§cRemoved effect: §e" + effect.getName());
                    } else {
                        // Category and Type Exclusivity
                        RelicEffect.Category cat = effect.getCategory();
                        String root = effect.getRootName();
                        effects.removeIf(e -> e.getCategory() == cat || e.getRootName().equals(root));
                        effects.add(effect);
                        player.sendMessage("§aAdded effect: §e" + effect.getName());
                    }
                    manager.updateRelicEffects(fullRelicId, effects);
                    RelicMenu.openAdminEditor(player, fullRelicId, player.getUniqueId(), manager, 0); // Refresh
                } catch (Exception ignored) {}
            }
        } else if (title.startsWith("§c§lEditing: ")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 53) {
                RelicMenu.openPlayerList(player, 0);
                return;
            }
            
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;
            
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(RelicManager.RELIC_ID_KEY, PersistentDataType.STRING)) {
                String relicId = meta.getPersistentDataContainer().get(RelicManager.RELIC_ID_KEY, PersistentDataType.STRING);
                String targetName = title.substring("§c§lEditing: ".length());
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                if (target == null || target.getName() == null) return;

                if (event.isShiftClick() && event.isRightClick()) {
                    RelicMenu.openAdminEditor(player, relicId, target.getUniqueId(), plugin.getRelicManager(), 0);
                } else if (event.isShiftClick()) {
                    // Sell target's relic
                    RelicManager manager = plugin.getRelicManager();
                    RelicManager.RelicData data = manager.getRelicData(relicId);
                    if (data != null) {
                        RelicManager.RelicType type = manager.getRelicType(data.getCustomModelData());
                        if (type != null) {
                            double sellPrice = type.getPrice() * 0.3;
                            manager.removeRelic(target.getUniqueId(), relicId);
                            if (target.isOnline()) {
                                plugin.getEconomyManager().deposit((Player)target, sellPrice);
                            } else {
                                // For offline players, we'd need a way to deposit to their balance
                                // If EconomyManager supports UUID, use that.
                                plugin.getEconomyManager().deposit(target.getUniqueId(), sellPrice);
                            }
                            player.sendMessage("§aYou sold " + target.getName() + "'s relic for " + plugin.getEconomyManager().getCurrencyName(sellPrice) + ".");
                            RelicMenu.openAdminTargetEquip(player, target, manager);
                        }
                    }
                }
                // Regular clicks (Equip) are blocked for OPs in this view
            }
        } else if (title.equals(RelicMenu.ADMIN_MAIN_TITLE)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 10) RelicMenu.openPlayerList(player, 0);
            else if (slot == 13) RelicMenu.openCustomCreatorBase(player);
            else if (slot == 16) RelicMenu.openAdminGlobalActions(player);
        } else if (title.equals(RelicMenu.PLAYER_LIST_TITLE)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 49) { RelicMenu.openAdminMain(player); return; }
            
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.PLAYER_HEAD) {
                String name = item.getItemMeta().getDisplayName().substring(2); // Remove §f
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                if (target != null && target.getName() != null) {
                    RelicMenu.openAdminTargetEquip(player, target, plugin.getRelicManager());
                }
            }
        } else if (title.equals(RelicMenu.GLOBAL_ACTIONS_TITLE)) {
            event.setCancelled(true);
            if (event.getSlot() == 13) {
                // Bulk Sell All
                plugin.getRelicManager().sellAllRelics(plugin.getEconomyManager());
                player.sendMessage("§c§l[ADMIN] §aAll relics have been sold and cleared!");
                player.closeInventory();
            }
        } else if (title.startsWith(RelicMenu.CUSTOM_CREATOR_TITLE)) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.PAPER) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData()) {
                    // Give custom relic to self (simplified creator for now)
                    plugin.getRelicManager().addRelic(player.getUniqueId(), meta.getCustomModelData());
                    player.sendMessage("§aCreated custom relic!");
                    player.closeInventory();
                }
            }
        }
    }
}
