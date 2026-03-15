package com.test;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RelicEffectListener implements Listener {

    private final NightreignRelic plugin;
    private static final NamespacedKey HP_MODIFIER_KEY = new NamespacedKey("nightreign", "relic_hp");
    
    private final Map<UUID, Long> potionBuffExpiries = new HashMap<>();
    private final Map<UUID, Long> damageBuffExpiries = new HashMap<>();
    private static final NamespacedKey POISE_MODIFIER_KEY = new NamespacedKey("nightreign", "relic_poise");
    private static final NamespacedKey VIGOR_MODIFIER_KEY = new NamespacedKey("nightreign", "relic_vigor");

    public RelicEffectListener(NightreignRelic plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAction() == EntityPotionEffectEvent.Action.ADDED || event.getAction() == EntityPotionEffectEvent.Action.CHANGED) {
            potionBuffExpiries.put(player.getUniqueId(), System.currentTimeMillis() + 30000);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            RelicManager manager = plugin.getRelicManager();
            List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
            
            if (effects.contains(RelicEffect.COLD_MIRAGE) && !manager.isOnMirageCooldown(player.getUniqueId())) {
                event.setCancelled(true);
                player.setHealth(2.0); // Stay alive with 1 heart
                manager.setMirageCooldown(player.getUniqueId());
                
                // Cold Mirage effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false, false));
                player.sendMessage("§b§lCold Mirage! §7You have escaped fatal damage.");
                
                // Blind the attacker
                if (event instanceof EntityDamageByEntityEvent edee && edee.getDamager() instanceof LivingEntity attacker) {
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                }
                
                // Note: Armor/Item invisibility usually requires packets or a separate plugin, 
                // but we'll use the basic invisibility potion for now.
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // Tracking for buff triggers
        damageBuffExpiries.put(player.getUniqueId(), System.currentTimeMillis() + 10000);

        // Damage Negation Logic
        RelicManager manager = plugin.getRelicManager();
        List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
        if (effects.isEmpty()) return;

        double physicalNeg = 0;
        double fireNeg = 0;
        double lightningNeg = 0;
        double generalNeg = 0; // Affinity negation applies to all except physical

        for (RelicEffect effect : effects) {
            switch (effect) {
                case PHYSICAL_NEGATION_1 -> physicalNeg += 0.11;
                case PHYSICAL_NEGATION_2 -> physicalNeg += 0.13;
                
                case LIGHTNING_NEGATION_1 -> lightningNeg += 0.15;
                case LIGHTNING_NEGATION_2 -> lightningNeg += 0.20;
                
                case FIRE_NEGATION_1 -> fireNeg += 0.15;
                case FIRE_NEGATION_2 -> fireNeg += 0.20;
                
                case AFFINITY_NEGATION -> generalNeg += 0.06;
                case AFFINITY_NEGATION_1 -> generalNeg += 0.11;
                case AFFINITY_NEGATION_2 -> generalNeg += 0.15;
                default -> {}
            }
        }

        double totalReduction = 0;
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || 
            cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
            totalReduction = Math.min(0.8, fireNeg + generalNeg);
        } else if (cause == EntityDamageEvent.DamageCause.LIGHTNING) {
            totalReduction = Math.min(0.8, lightningNeg + generalNeg);
        } else if (cause == EntityDamageEvent.DamageCause.MAGIC || cause == EntityDamageEvent.DamageCause.WITHER || cause == EntityDamageEvent.DamageCause.DRAGON_BREATH) {
            totalReduction = Math.min(0.8, generalNeg);
        } else if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.PROJECTILE || 
                   cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            totalReduction = Math.min(0.8, physicalNeg);
        }

        if (totalReduction > 0) {
            event.setDamage(event.getDamage() * (1.0 - totalReduction));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        // Handle Shield Shockwave
        if (event.getEntity() instanceof Player victim) {
            if (victim.isBlocking()) {
                handleShieldRecovery(victim);
                handleShieldShockwave(victim);
            }
            
            if (event.getDamager() instanceof Player attacker) {
                handleSuccessiveAttackRecovery(attacker);
            }
        }

        // Damage calculation
        if (!(event.getDamager() instanceof Player)) {
            if (event.getDamager() instanceof AbstractArrow arrow && arrow.getShooter() instanceof Player shooter) {
                applyDamageModifiers(event, shooter, true);
            } else if (event.getDamager() instanceof ThrownPotion potion && potion.getShooter() instanceof Player shooter) {
                applyDamageModifiers(event, shooter, false);
            } else if (event.getDamager() instanceof LightningStrike) {
                // Harder to get player from lightning bolt directly in Bukkit, 
                // but usually LightningStrike is from trident if summoned.
                // For simplicity, we'll check if any nearby player has lightning bonus.
            }
            return;
        }
        applyDamageModifiers(event, (Player) event.getDamager(), false);
    }

    private void handleShieldRecovery(Player player) {
        RelicManager manager = plugin.getRelicManager();
        List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
        if (effects.contains(RelicEffect.GUARD_RECOVERY)) {
            double newHealth = Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + 2);
            player.setHealth(newHealth);
        }
    }

    private void handleSuccessiveAttackRecovery(Player player) {
        RelicManager manager = plugin.getRelicManager();
        List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
        if (effects.contains(RelicEffect.SUCCESSIVE_ATTACK_RECOVERY)) {
            int streak = manager.incrementHitStreak(player.getUniqueId());
            if (streak >= 3) { // Restore 1HP every hit after a 3-hit streak to balance
                 double newHealth = Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + 1);
                 player.setHealth(newHealth);
            }
        }
    }

    private void handleShieldShockwave(Player player) {
        RelicManager manager = plugin.getRelicManager();
        List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
        if (effects.contains(RelicEffect.SHIELD_SHOCKWAVE)) {
            player.getWorld().getNearbyEntities(player.getLocation(), 4, 2, 4).forEach(entity -> {
                if (entity != player && entity instanceof LivingEntity) {
                    Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    entity.setVelocity(direction.multiply(0.8).setY(0.4));
                }
            });
        }
    }

    private void applyDamageModifiers(EntityDamageByEntityEvent event, Player player, boolean isRanged) {
        RelicManager manager = plugin.getRelicManager();
        List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
        if (effects.isEmpty()) return;

        double percentBonus = 0;
        double maxMasteryBonus = 0;
        double maxTriggeredBonus = 0;
        
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        Material type = mainHand.getType();

        boolean isMelee = type.name().endsWith("_SWORD") || type.name().endsWith("_AXE") || 
                         type.equals(Material.MACE) || type.equals(Material.TRIDENT) ||
                         type.name().endsWith("_PICKAXE");
        
        boolean hasPotionBuff = potionBuffExpiries.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis();
        boolean hasDamageBuff = damageBuffExpiries.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis();

        for (RelicEffect effect : effects) {
            switch (effect) {
                case PHYSICAL_ATTACK -> percentBonus += 0.03;
                case PHYSICAL_ATTACK_1 -> percentBonus += 0.04;
                case PHYSICAL_ATTACK_2 -> percentBonus += 0.05;
                case PHYSICAL_ATTACK_3 -> percentBonus += 0.065;
                case PHYSICAL_ATTACK_4 -> percentBonus += 0.08;
                
                case POTION_PHYSICAL_1 -> { if (hasPotionBuff) maxTriggeredBonus = Math.max(maxTriggeredBonus, 0.15); }
                case POTION_PHYSICAL_2 -> { if (hasPotionBuff) maxTriggeredBonus = Math.max(maxTriggeredBonus, 0.20); }
                case TAKE_DAMAGE_BUFF_1 -> { if (hasDamageBuff) maxTriggeredBonus = Math.max(maxTriggeredBonus, 0.05); }
                case TAKE_DAMAGE_BUFF_2 -> { if (hasDamageBuff) maxTriggeredBonus = Math.max(maxTriggeredBonus, 0.07); }
                
                case CRIT_HIT -> { if (event.isCritical()) percentBonus += 0.08; }
                case CRIT_HIT_1 -> { if (event.isCritical()) percentBonus += 0.13; }
                
                case POTION_DAMAGE -> { if (event.getDamager() instanceof ThrownPotion) percentBonus += 0.15; }
                
                case CROSSBOW_POWER -> { if (type == Material.CROSSBOW) maxMasteryBonus = Math.max(maxMasteryBonus, 0.10); }
                case BOW_POWER -> { if (type == Material.BOW) maxMasteryBonus = Math.max(maxMasteryBonus, 0.05); }
                case SWORD_POWER -> { if (type.name().endsWith("_SWORD")) maxMasteryBonus = Math.max(maxMasteryBonus, 0.10); }
                case AXE_POWER -> { if (type.name().endsWith("_AXE")) maxMasteryBonus = Math.max(maxMasteryBonus, 0.15); }
                
                case TWO_ARMAMENTS -> { 
                    if (mainHand.getType() != Material.AIR && offHand.getType() != Material.AIR) percentBonus += 0.07; 
                }
                case TWO_HANDING -> {
                    if (mainHand.getType() != Material.AIR && offHand.getType() == Material.AIR) percentBonus += 0.15;
                }
                
                case LIGHTNING_0 -> { if (isLightning(event, mainHand)) percentBonus += 0.025; }
                case LIGHTNING_1 -> { if (isLightning(event, mainHand)) percentBonus += 0.05; }
                case LIGHTNING_2 -> { if (isLightning(event, mainHand)) percentBonus += 0.075; }
                case LIGHTNING_3 -> { if (isLightning(event, mainHand)) percentBonus += 0.10; }
                case LIGHTNING_4 -> { if (isLightning(event, mainHand)) percentBonus += 0.125; }
                
                case FIRE_0 -> { if (isFire(event, mainHand)) percentBonus += 0.025; }
                case FIRE_1 -> { if (isFire(event, mainHand)) percentBonus += 0.05; }
                case FIRE_2 -> { if (isFire(event, mainHand)) percentBonus += 0.075; }
                case FIRE_3 -> { if (isFire(event, mainHand)) percentBonus += 0.10; }
                case FIRE_4 -> { if (isFire(event, mainHand)) percentBonus += 0.125; }

                case MELEE_ATTACK -> { if (isMelee && !isRanged) percentBonus += 0.10; }
                case RANGE_ATTACK -> { if (isRanged) percentBonus += 0.10; }
                case UNDEAD_DAMAGE -> {
                    if (event.getEntity() instanceof LivingEntity victim && isUndead(victim)) {
                        percentBonus += 0.10;
                    }
                }
                default -> {}
            }
        }

        percentBonus += maxMasteryBonus;
        percentBonus += maxTriggeredBonus;

        if (percentBonus > 0) {
            event.setDamage(event.getDamage() * (1 + percentBonus));
        }
    }

    private boolean isLightning(EntityDamageByEntityEvent event, ItemStack item) {
        if (event.getDamager() instanceof Trident trident) {
            return trident.getItemStack().containsEnchantment(Enchantment.CHANNELING);
        }
        return false;
    }

    private boolean isFire(EntityDamageByEntityEvent event, ItemStack item) {
        if (item == null) return false;
        return item.containsEnchantment(Enchantment.FIRE_ASPECT) || item.containsEnchantment(Enchantment.FLAME);
    }

    private boolean isUndead(LivingEntity entity) {
        // Simple undead check for 1.21
        switch (entity.getType()) {
            case ZOMBIE, SKELETON, WITHER_SKELETON, STRAY, HUSK, DROWNED, ZOMBIE_VILLAGER, 
                 PHANTOM, ZOGLIN, ZOMBIFIED_PIGLIN, WITHER, SKELETON_HORSE, ZOMBIE_HORSE -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        RelicManager manager = plugin.getRelicManager();
        List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
        
        if (effects.contains(RelicEffect.XP_ACQUISITION)) {
            event.setAmount((int) (event.getAmount() * 1.25)); // 25% bonus XP
        }
    }

    @EventHandler
    public void onHealthRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        RelicManager manager = plugin.getRelicManager();
        List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
        
        if (effects.contains(RelicEffect.IMPROVED_RESTORATION)) {
            event.setAmount(event.getAmount() * 1.20);
        }
    }

    public void updatePlayerAttributes(Player player) {
        RelicManager manager = plugin.getRelicManager();
        
        // Remove existing modifiers
        AttributeInstance hpAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (hpAttr != null) {
            for (AttributeModifier mod : hpAttr.getModifiers()) {
                if (mod.getName().equals("RelicHP") || mod.getName().equals("RelicVigor")) {
                    hpAttr.removeModifier(mod);
                }
            }
        }

        AttributeInstance poiseAttr = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (poiseAttr != null) {
            for (AttributeModifier mod : poiseAttr.getModifiers()) {
                if (mod.getName().equals("RelicPoise")) {
                    poiseAttr.removeModifier(mod);
                }
            }
        }
        
        // Remove AuraSkills modifiers
        plugin.getAuraSkillsHook().removeFPModifier(player);
        plugin.getAuraSkillsHook().removeManaModifier(player);

        List<RelicEffect> effects = manager.getAllEquippedEffects(player.getUniqueId());
        if (effects.isEmpty()) return;

        double hpPercentBonus = 0;
        double vigorFlatBonus = 0;
        double poiseFlatBonus = 0;
        double fpBonus = 0;
        double manaFlatBonus = 0;

        for (RelicEffect effect : effects) {
            switch (effect) {
                case MAX_HP -> hpPercentBonus += 0.10;
                case MAX_FP -> fpBonus += 0.10;
                case VIGOR_1 -> vigorFlatBonus += 2;
                case VIGOR_2 -> vigorFlatBonus += 4;
                case VIGOR_3 -> vigorFlatBonus += 6;
                case POISE_1 -> poiseFlatBonus += 0.2;
                case POISE_2 -> poiseFlatBonus += 0.4;
                case POISE_3 -> poiseFlatBonus += 0.7;
                case MIND_1 -> manaFlatBonus += 10;
                case MIND_2 -> manaFlatBonus += 20;
                case MIND_3 -> manaFlatBonus += 30;
                default -> {}
            }
        }

        // Apply Vigor (flat) using ADD_NUMBER (Operation 0)
        if (vigorFlatBonus > 0 && hpAttr != null) {
            hpAttr.addModifier(new AttributeModifier(VIGOR_MODIFIER_KEY, vigorFlatBonus, AttributeModifier.Operation.ADD_NUMBER));
        }

        // Apply MAX_HP (percent) using ADD_SCALAR (Operation 1)
        if (hpPercentBonus > 0 && hpAttr != null) {
            hpAttr.addModifier(new AttributeModifier(HP_MODIFIER_KEY, hpPercentBonus, AttributeModifier.Operation.ADD_SCALAR));
        }

        if (poiseFlatBonus > 0 && poiseAttr != null) {
            poiseAttr.addModifier(new AttributeModifier(POISE_MODIFIER_KEY, poiseFlatBonus, AttributeModifier.Operation.ADD_NUMBER));
        }
        
        if (fpBonus > 0) {
            plugin.getAuraSkillsHook().applyFPModifier(player, fpBonus);
        }

        if (manaFlatBonus > 0) {
            plugin.getAuraSkillsHook().applyManaModifier(player, manaFlatBonus);
        }
    }
}
