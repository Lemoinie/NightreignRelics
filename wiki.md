# NightreignRelics Wiki

Welcome to the official wiki for NightreignRelics. This guide covers everything from obtaining your first relic to mastering the various effects they provide.

## 🏺 Relic Rarities

Relics come in three tiers of quality. The rarity determines how many random effects a relic can have:

| Rarity | Effects |
| :--- | :--- |
| **Delicate** | 1 Effect |
| **Polished** | 2 Effects |
| **Grand** | 3 Effects |

## 🎲 Gacha System

Relics are obtained through the Gacha menu. There are three tiers of gacha, each with different odds for high-rarity relics.

- **Standard**: Cheap, low chance for Grand relics.
- **Balanced**: Moderate cost, equal chances for all rarities.
- **Premium**: High cost, significantly higher chance for Grand relics.

### Costs & Currency
Costs are configurable in `config.yml` and can consume either **Vault Economy** or **Player XP**.

---

## 🔥 Complete Effect List

Effects are assigned randomly when a relic is acquired. Most damage bonuses are **cumulative** (applied additively), but **Weapon Mastery** and **Triggered Buffs** categories are non-stacking (only the strongest equipped bonus applies).

### Stat Boosts
- **Max HP Up**: +10% Max Health.
- **Max FP Up**: +10% Max Mana (Requires AuraSkills).

### Physical Damage Tiers
- **Physical Attack Up**: +3%
- **Physical Attack Up +1**: +4%
- **Physical Attack Up +2**: +5%
- **Physical Attack Up +3**: +6.5%
- **Physical Attack Up +4**: +8%

### Weapon Mastery (Non-Stacking)
- **Sword Mastery**: +10% Sword damage.
- **Axe Mastery**: +15% Axe damage.
- **Bow Mastery**: +5% Bow damage.
- **Crossbow Mastery**: +10% Crossbow damage.
- **Range Weapon Attack**: +10% damage with Bows, Crossbows, and Tridents.
- **Melee Weapon Attack**: +10% damage with general melee weapons.

### Elemental & Tactical
- **Fire Attack Up (0-4)**: +2.5% to +12.5% extra damage from Fire Aspect/Flame.
- **Lightning Attack Up (0-4)**: +2.5% to +12.5% extra damage from Channeling Tridents.
- **Undead Slayer**: +10% damage against undead enemies.
- **Potent Brews**: +15% damage from thrown potions.

### Triggered Buffs (Non-Stacking)
- **Alchemical Strike (0-1)**: +15%/+20% Physical damage for 30s after gaining a potion effect.
- **Vengeful Power (0-1)**: +5%/+7% Damage for 10s after taking damage.

### Utility & Style
- **Improved Critical Hits (0-1)**: +8%/+13% Crit damage.
- **Dual Wielder**: +7% damage when holding items in both hands.
- **Two-Handing**: +15% damage when the off-hand is empty.
- **Retribution Wave**: Blocking with a shield triggers a knockback shockwave.

---

## ⚙️ Configuration

The `config.yml` file allows you to customize the gacha experience:

```yaml
gacha:
  tier1:
    cost: 100
    currency: VAULT
  tier2:
    cost: 500
    currency: VAULT
  tier3:
    cost: 10
    currency: XP
```
