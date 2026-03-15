package com.test;

public enum RelicEffect {
    MAX_HP("Increase Max HP", "§7Increases Max HP by §a10%", Category.STATS),
    MAX_FP("Increase Max FP", "§7Increases Max FP by §a10%", Category.STATS),
    RANGE_ATTACK("Range Weapon Attack", "§7Increases Ranged damage by §a10%", Category.ATTACK),
    MELEE_ATTACK("Melee Weapon Attack", "§7Increases Melee damage by §a10%", Category.ATTACK),
    UNDEAD_DAMAGE("Undead Slayer", "§7Increases damage against Undead by §a10%", Category.ATTACK),
    PHYSICAL_ATTACK("Physical Attack Up", "§7Increases Physical damage by §a3%", Category.ATTACK),
    PHYSICAL_ATTACK_1("Physical Attack Up +1", "§7Increases Physical damage by §a4%", Category.ATTACK),
    PHYSICAL_ATTACK_2("Physical Attack Up +2", "§7Increases Physical damage by §a5%", Category.ATTACK),
    PHYSICAL_ATTACK_3("Physical Attack Up +3", "§7Increases Physical damage by §a6.5%", Category.ATTACK),
    PHYSICAL_ATTACK_4("Physical Attack Up +4", "§7Increases Physical damage by §a8%", Category.ATTACK),
    
    POTION_PHYSICAL_1("Alchemical Strike", "§7Physical damage §a+15% §7for 30s after gaining potion effect", Category.TRIGGERED),
    POTION_PHYSICAL_2("Alchemical Strike +1", "§7Physical damage §a+20% §7for 30s after gaining potion effect", Category.TRIGGERED),
    
    TAKE_DAMAGE_BUFF_1("Vengeful Power", "§7Damage §a+5% §7for 10s after taking damage", Category.TRIGGERED),
    TAKE_DAMAGE_BUFF_2("Vengeful Power +1", "§7Damage §a+7% §7for 10s after taking damage", Category.TRIGGERED),
    
    CRIT_HIT("Improved Critical Hits", "§7Critical hit damage §a+8%", Category.ATTACK),
    CRIT_HIT_1("Improved Critical Hits +1", "§7Critical hit damage §a+13%", Category.ATTACK),
    
    POTION_DAMAGE("Potent Brews", "§7Increases damage of thrown potions by §a15%", Category.UTILITY),
    
    CROSSBOW_POWER("Crossbow Mastery", "§7Increases Crossbow damage by §a10%", Category.ATTACK),
    BOW_POWER("Bow Mastery", "§7Increases Bow damage by §a5%", Category.ATTACK),
    SWORD_POWER("Sword Mastery", "§7Increases Sword damage by §a10%", Category.ATTACK),
    AXE_POWER("Axe Mastery", "§7Increases Axe damage by §a15%", Category.ATTACK),
    
    TWO_ARMAMENTS("Dual Wielder", "§7Increases damage by §a7% §7when using both hands", Category.ATTACK),
    TWO_HANDING("Two-Handing", "§7Increases damage by §a15% §7when off-hand is empty", Category.ATTACK),
    
    LIGHTNING_0("Lightning Attack Up", "§7Increases Lightning damage by §a2.5%", Category.ELEMENTAL),
    LIGHTNING_1("Lightning Attack Up +1", "§7Increases Lightning damage by §a5%", Category.ELEMENTAL),
    LIGHTNING_2("Lightning Attack Up +2", "§7Increases Lightning damage by §a7.5%", Category.ELEMENTAL),
    LIGHTNING_3("Lightning Attack Up +3", "§7Increases Lightning damage by §a10%", Category.ELEMENTAL),
    LIGHTNING_4("Lightning Attack Up +4", "§7Increases Lightning damage by §a12.5%", Category.ELEMENTAL),
    
    FIRE_0("Fire Attack Up", "§7Increases Fire damage by §a2.5%", Category.ELEMENTAL),
    FIRE_1("Fire Attack Up +1", "§7Increases Fire damage by §a5%", Category.ELEMENTAL),
    FIRE_2("Fire Attack Up +2", "§7Increases Fire damage by §a7.5%", Category.ELEMENTAL),
    FIRE_3("Fire Attack Up +3", "§7Increases Fire damage by §a10%", Category.ELEMENTAL),
    FIRE_4("Fire Attack Up +4", "§7Increases Fire damage by §a12.5%", Category.ELEMENTAL),
    
    SHIELD_SHOCKWAVE("Retribution Wave", "§7Blocks trigger a shockwave that knocks back enemies", Category.UTILITY),
    
    PHYSICAL_NEGATION_1("Physical Guard +1", "§7Improves physical damage negation by §a11%", Category.DEFENSIVE),
    PHYSICAL_NEGATION_2("Physical Guard +2", "§7Improves physical damage negation by §a13%", Category.DEFENSIVE),
    
    LIGHTNING_NEGATION_1("Lightning Guard +1", "§7Improves lightning damage negation by §a15%", Category.DEFENSIVE),
    LIGHTNING_NEGATION_2("Lightning Guard +2", "§7Improves lightning damage negation by §a20%", Category.DEFENSIVE),
    
    FIRE_NEGATION_1("Fire Guard +1", "§7Improves fire damage negation by §a15%", Category.DEFENSIVE),
    FIRE_NEGATION_2("Fire Guard +2", "§7Improves fire damage negation by §a20%", Category.DEFENSIVE),
    
    AFFINITY_NEGATION("Affinity Guard", "§7Negate Elemental/Potion damage by §a6%", Category.DEFENSIVE),
    AFFINITY_NEGATION_1("Affinity Guard +1", "§7Negate Elemental/Potion damage by §a11%", Category.DEFENSIVE),
    AFFINITY_NEGATION_2("Affinity Guard +2", "§7Negate Elemental/Potion damage by §a15%", Category.DEFENSIVE),
    
    RUNE_ACQUISITION("Golden Rune", "§7Increases currency gains by §a3.5%", Category.UTILITY),
    XP_ACQUISITION("Wise Spirit", "§7Increases XP gains from all sources", Category.UTILITY),
    
    POISE_1("Poise +1", "§7Increases knockback resistance slightly", Category.STATS),
    POISE_2("Poise +2", "§7Increases knockback resistance moderately", Category.STATS),
    POISE_3("Poise +3", "§7Increases knockback resistance significantly", Category.STATS),
    
    VIGOR_1("Vigor +1", "§7Increases Max HP by §a2 (1 Heart)", Category.STATS),
    VIGOR_2("Vigor +2", "§7Increases Max HP by §a4 (2 Hearts)", Category.STATS),
    VIGOR_3("Vigor +3", "§7Increases Max HP by §a6 (3 Hearts)", Category.STATS),
    
    MIND_1("Mind +1", "§7Increases Max Mana by §a10 §8(Requires AuraSkills)", Category.STATS),
    MIND_2("Mind +2", "§7Increases Max Mana by §a20 §8(Requires AuraSkills)", Category.STATS),
    MIND_3("Mind +3", "§7Increases Max Mana by §a30 §8(Requires AuraSkills)", Category.STATS),
    
    IMPROVED_RESTORATION("Blessed Dew", "§7Increases HP restoration by §a20%", Category.REGEN),
    GUARD_RECOVERY("Shield Bash Recovery", "§7Restore §a2 HP §7when successfully blocking", Category.REGEN),
    SUCCESSIVE_ATTACK_RECOVERY("Continuous Strike", "§7Restore §a1 HP §7on successive attacks", Category.REGEN),
    
    COLD_MIRAGE("Cold Mirage", "§7Turn invisible and blind attacker on fatal damage §8(CD: 1m)", Category.SPECIAL);

    public enum Category {
        STATS, ATTACK, TRIGGERED, ELEMENTAL, DEFENSIVE, UTILITY, REGEN, SPECIAL
    }

    private final String name;
    private final String description;
    private final Category category;

    RelicEffect(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public String getRootName() {
        // Extracts the part before " +" or "Up" or "+" if it exists, for exclusivity checks
        String base = name();
        if (base.contains("_")) {
            // e.g., PHYSICAL_ATTACK_3 -> PHYSICAL_ATTACK
            // VIGOR_2 -> VIGOR
            int lastUnderscore = base.lastIndexOf('_');
            if (Character.isDigit(base.charAt(lastUnderscore + 1))) {
                return base.substring(0, lastUnderscore);
            }
        }
        return base;
    }
}
