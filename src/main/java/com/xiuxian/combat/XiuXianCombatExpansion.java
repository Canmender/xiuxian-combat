package com.xiuxian.combat;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * XiuXianCombat PAPI 变量
 */
public class XiuXianCombatExpansion extends PlaceholderExpansion {
    
    private final XiuXianCombat plugin;
    
    public XiuXianCombatExpansion(XiuXianCombat plugin) {
        this.plugin = plugin;
    }
    
    @Override public String getIdentifier() { return "xicombat"; }
    @Override public String getAuthor() { return "XiuXianCore"; }
    @Override public String getVersion() { return "1.0"; }
    @Override public boolean persist() { return true; }
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        CombatData data = plugin.getCombatData(player.getUniqueId());
        if (data == null) return "0";
        
        switch (identifier) {
            // HP/真元
            case "max_hp":          return String.format("%.0f", data.maxHp);
            case "current_hp":      return String.format("%.0f", player.getHealth());
            case "max_mana":        return String.format("%.0f", data.maxMana);
            case "current_mana":    return String.format("%.0f", data.mana);
            case "mana_percent":    return data.maxMana > 0 ? String.format("%.1f", data.mana / data.maxMana * 100) : "0";
            // 物理伤害系统
            case "phy_base_damage":   return String.format("%.1f", data.physicalBaseDamage);
            case "phy_crit_rate":     return String.format("%.1f", data.physicalCritRate);
            case "phy_crit_multi":    return String.format("%.2f", data.physicalCritMulti);
            case "phy_penetration":   return String.format("%.1f", data.physicalPenetration);
            case "phy_block":         return String.format("%.1f", data.physicalBlock);
            case "phy_defense":       return String.format("%.1f", data.physicalDefense);
            // 法术伤害系统
            case "mag_base_damage":   return String.format("%.1f", data.magicalBaseDamage);
            case "mag_crit_rate":     return String.format("%.1f", data.magicalCritRate);
            case "mag_crit_multi":    return String.format("%.2f", data.magicalCritMulti);
            case "mag_penetration":   return String.format("%.1f", data.magicalPenetration);
            case "mag_block":         return String.format("%.1f", data.magicalBlock);
            case "mag_defense":       return String.format("%.1f", data.magicalDefense);
            // 会心抵抗/抗性
            case "crit_resistance":   return String.format("%.1f", data.critResistance);
            case "crit_tenacity":     return String.format("%.1f", data.critTenacity);
            // 其他
            case "attack_speed":      return String.format("%.2f", data.attackSpeed);
            case "damage_type":       return data.isLianti() ? "\u7269\u7406" : "\u6CD5\u672F";
            default: return null;
        }
    }
}
