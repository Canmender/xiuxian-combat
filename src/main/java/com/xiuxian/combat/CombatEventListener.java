package com.xiuxian.combat;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

/**
 * 战斗事件监听器 - 物理和法术完全独立计算
 */
public class CombatEventListener implements Listener {
    
    private final XiuXianCombat plugin;
    private final Random random = new Random();
    
    public CombatEventListener(XiuXianCombat plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        Player attacker = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        
        CombatData atkData = plugin.getCombatData(attacker.getUniqueId());
        if (atkData == null) return;
        
        // 获取防御方属性
        double defPhysDefense = 0, defMagDefense = 0;
        double defPhysBlock = 0, defMagBlock = 0;
        double defPhysBlockReduction = 0, defMagBlockReduction = 0;
        double defCritRes = 0, defCritTen = 0;
        
        if (target instanceof Player) {
            CombatData defData = plugin.getCombatData(target.getUniqueId());
            if (defData != null) {
                defPhysDefense = defData.physicalDefense;
                defMagDefense = defData.magicalDefense;
                defPhysBlock = defData.physicalBlock;
                defMagBlock = defData.magicalBlock;
                defPhysBlockReduction = plugin.getConfig().getDouble("physical-block-damage-reduction", 0.5);
                defMagBlockReduction = plugin.getConfig().getDouble("magical-block-damage-reduction", 0.5);
                defCritRes = defData.critResistance;
                defCritTen = defData.critTenacity;
            }
        }
        
        // === 物理伤害计算 ===
        double physDamage = atkData.physicalBaseDamage;
        
        // 物理会心判定
        double physCritRate = Math.max(0, atkData.physicalCritRate - defCritRes);
        double physCritMulti = Math.max(1.0, atkData.physicalCritMulti - defCritTen / 100.0);
        boolean physCrit = random.nextDouble() * 100 < physCritRate;
        if (physCrit) physDamage *= physCritMulti;
        
        // 物理防御减免
        if (defPhysDefense > 0) {
            double effectiveDef = defPhysDefense * (1.0 - Math.min(atkData.physicalPenetration, 100.0) / 100.0);
            double reduction = effectiveDef / (100.0 + effectiveDef);
            physDamage *= (1.0 - reduction);
            if (atkData.physicalPenetration > 0 && plugin.isShowCombatLog()) {
                attacker.sendMessage(colorize(plugin.getConfig().getString("physical-penetration-message", "&c&l【物穿】&7无视了 &f{amount} &7点物理防御").replace("{amount}", String.format("%.1f", atkData.physicalPenetration))));
            }
        }
        
        // 物理格挡
        if (target instanceof Player && defPhysBlock > 0) {
            if (random.nextDouble() * 100 < defPhysBlock) {
                physDamage *= (1.0 - defPhysBlockReduction);
                if (plugin.isShowCombatLog()) {
                    ((Player) target).sendMessage(colorize(plugin.getConfig().getString("physical-block-message", "&a&l【物挡】&7格挡了物理攻击")));
                }
            }
        }
        
        // === 法术伤害计算 ===
        double magDamage = atkData.magicalBaseDamage;
        
        // 法术会心判定
        double magCritRate = Math.max(0, atkData.magicalCritRate - defCritRes);
        double magCritMulti = Math.max(1.0, atkData.magicalCritMulti - defCritTen / 100.0);
        boolean magCrit = random.nextDouble() * 100 < magCritRate;
        if (magCrit) magDamage *= magCritMulti;
        
        // 法术防御减免
        if (defMagDefense > 0) {
            double effectiveDef = defMagDefense * (1.0 - Math.min(atkData.magicalPenetration, 100.0) / 100.0);
            double reduction = effectiveDef / (100.0 + effectiveDef);
            magDamage *= (1.0 - reduction);
            if (atkData.magicalPenetration > 0 && plugin.isShowCombatLog()) {
                attacker.sendMessage(colorize(plugin.getConfig().getString("magical-penetration-message", "&9&l【法穿】&7无视了 &f{amount} &7点法术防御").replace("{amount}", String.format("%.1f", atkData.magicalPenetration))));
            }
        }
        
        // 法术格挡
        if (target instanceof Player && defMagBlock > 0) {
            if (random.nextDouble() * 100 < defMagBlock) {
                magDamage *= (1.0 - defMagBlockReduction);
                if (plugin.isShowCombatLog()) {
                    ((Player) target).sendMessage(colorize(plugin.getConfig().getString("magical-block-message", "&9&l【法挡】&7格挡了法术攻击")));
                }
            }
        }
        
        // === 合并伤害 ===
        double finalDamage = physDamage + magDamage;
        event.setDamage(Math.max(0, finalDamage));
        
        // 战斗日志
        if (plugin.isShowCombatLog() && (physCrit || magCrit)) {
            String critMsg = plugin.getConfig().getString("crit-message", "&6&l【会心】&7造成 &c&l{damage} &7伤害");
            attacker.sendMessage(colorize(critMsg.replace("{damage}", String.format("%.1f", finalDamage))));
        }
    }
    
    private String colorize(String msg) {
        return msg.replace("&0", ChatColor.BLACK.toString())
                  .replace("&1", ChatColor.DARK_BLUE.toString())
                  .replace("&2", ChatColor.DARK_GREEN.toString())
                  .replace("&3", ChatColor.DARK_AQUA.toString())
                  .replace("&4", ChatColor.DARK_RED.toString())
                  .replace("&5", ChatColor.DARK_PURPLE.toString())
                  .replace("&6", ChatColor.GOLD.toString())
                  .replace("&7", ChatColor.GRAY.toString())
                  .replace("&8", ChatColor.DARK_GRAY.toString())
                  .replace("&9", ChatColor.BLUE.toString())
                  .replace("&a", ChatColor.GREEN.toString())
                  .replace("&b", ChatColor.AQUA.toString())
                  .replace("&c", ChatColor.RED.toString())
                  .replace("&d", ChatColor.LIGHT_PURPLE.toString())
                  .replace("&e", ChatColor.YELLOW.toString())
                  .replace("&f", ChatColor.WHITE.toString())
                  .replace("&l", ChatColor.BOLD.toString());
    }
}
