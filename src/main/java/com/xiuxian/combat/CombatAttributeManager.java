package com.xiuxian.combat;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * 战斗属性计算器 - 完整RPG数值体系
 * 
 * 所有属性都使用指数增长公式：
 * value = level × perLevel × typeMulti × (1+realmIdx)^realmPower
 */
public class CombatAttributeManager {
    
    // HP系统
    private double hpBase, hpPerLevel, liantiHpMulti, xiufaHpMulti, hpRealmPower;
    
    // 真元系统
    private double manaPerLevel, manaRecovery, manaChongBonus, manaRealmBonus;
    private double liantiManaMulti, xiufaManaMulti, manaRealmPower;
    
    // 物理伤害系统
    private double physBaseDmgPerLevel, physDmgRealmPower;
    private double physCritRatePerLevel, physCritMultiPerLevel;
    private double physPenPerLevel, physPenRealmPower, physBlockPerRealm;
    private double maxPhysCritRate, maxPhysPen, maxPhysBlock;
    
    // 法术伤害系统
    private double magBaseDmgPerLevel, magDmgRealmPower;
    private double magCritRatePerLevel, magCritMultiPerLevel;
    private double magPenPerLevel, magPenRealmPower, magBlockPerRealm;
    private double maxMagCritRate, maxMagPen, maxMagBlock;
    
    // 防御属性
    private double physDefPerLevel, physDefRealmPower;
    private double magDefPerLevel, magDefRealmPower;
    private double maxPhysDef, maxMagDef;
    
    // 会心抵抗/抗性
    private double critResPerRealm, critTenPerRealm;
    private double maxCritRes, maxCritTen;
    
    // 类型加成
    private double liantiPhysDmgMulti, liantiPhysCritRateMulti, liantiPhysPenMulti, liantiPhysBlockMulti;
    private double liantiMagDmgMulti, liantiMagDefMulti;
    private double xiufaMagDmgMulti, xiufaMagCritRateMulti, xiufaMagPenMulti, xiufaMagBlockMulti;
    private double xiufaPhysDmgMulti, xiufaPhysDefMulti;
    
    // 攻速
    private double atkSpeedPerLevel, maxAtkSpeed;
    
    // 境界
    private int realmLevels;
    private boolean realmMultiplierEnabled;
    
    public void loadConfig(FileConfiguration cfg, int realmLevels) {
        this.realmLevels = realmLevels;
        
        // HP
        hpBase = cfg.getDouble("hp-base", 20);
        hpPerLevel = cfg.getDouble("hp-per-level", 10);
        liantiHpMulti = cfg.getDouble("lianti-hp-multiplier", 1.3);
        xiufaHpMulti = cfg.getDouble("xiufa-hp-multiplier", 0.7);
        hpRealmPower = cfg.getDouble("hp-realm-power", 2.5);
        
        // 真元
        manaPerLevel = cfg.getDouble("mana-per-level", 30);
        manaRecovery = cfg.getDouble("mana-recovery", 0.5);
        manaChongBonus = cfg.getDouble("mana-chong-bonus", 2000);
        manaRealmBonus = cfg.getDouble("mana-realm-bonus", 20000);
        liantiManaMulti = cfg.getDouble("lianti-mana-multiplier", 0.7);
        xiufaManaMulti = cfg.getDouble("xiufa-mana-multiplier", 1.3);
        manaRealmPower = cfg.getDouble("mana-realm-power", 2.5);
        
        // 物理伤害
        physBaseDmgPerLevel = cfg.getDouble("physical-base-damage-per-level", 2.0);
        physDmgRealmPower = cfg.getDouble("physical-damage-realm-power", 2.0);
        physCritRatePerLevel = cfg.getDouble("physical-crit-rate-per-level", 0.15);
        physCritMultiPerLevel = cfg.getDouble("physical-crit-multi-per-level", 0.002);
        physPenPerLevel = cfg.getDouble("physical-penetration-per-level", 0.12);
        physPenRealmPower = cfg.getDouble("physical-penetration-realm-power", 1.5);
        physBlockPerRealm = cfg.getDouble("physical-block-per-realm", 15.0);
        maxPhysCritRate = cfg.getDouble("max-physical-crit-rate", 75.0);
        maxPhysPen = cfg.getDouble("max-physical-penetration", 80.0);
        maxPhysBlock = cfg.getDouble("max-physical-block", 65.0);
        
        // 法术伤害
        magBaseDmgPerLevel = cfg.getDouble("magical-base-damage-per-level", 2.0);
        magDmgRealmPower = cfg.getDouble("magical-damage-realm-power", 2.0);
        magCritRatePerLevel = cfg.getDouble("magical-crit-rate-per-level", 0.15);
        magCritMultiPerLevel = cfg.getDouble("magical-crit-multi-per-level", 0.002);
        magPenPerLevel = cfg.getDouble("magical-penetration-per-level", 0.12);
        magPenRealmPower = cfg.getDouble("magical-penetration-realm-power", 1.5);
        magBlockPerRealm = cfg.getDouble("magical-block-per-realm", 15.0);
        maxMagCritRate = cfg.getDouble("max-magical-crit-rate", 75.0);
        maxMagPen = cfg.getDouble("max-magical-penetration", 80.0);
        maxMagBlock = cfg.getDouble("max-magical-block", 65.0);
        
        // 防御
        physDefPerLevel = cfg.getDouble("physical-defense-per-level", 1.5);
        physDefRealmPower = cfg.getDouble("physical-defense-realm-power", 2.0);
        magDefPerLevel = cfg.getDouble("magical-defense-per-level", 1.5);
        magDefRealmPower = cfg.getDouble("magical-defense-realm-power", 2.0);
        maxPhysDef = cfg.getDouble("max-physical-defense", 50000.0);
        maxMagDef = cfg.getDouble("max-magical-defense", 50000.0);
        
        // 会心抵抗/抗性
        critResPerRealm = cfg.getDouble("crit-resistance-per-realm", 12.0);
        critTenPerRealm = cfg.getDouble("crit-tenacity-per-realm", 10.0);
        maxCritRes = cfg.getDouble("max-crit-resistance", 70.0);
        maxCritTen = cfg.getDouble("max-crit-tenacity", 50.0);
        
        // 类型加成
        liantiPhysDmgMulti = cfg.getDouble("lianti-physical-damage-multiplier", 1.3);
        liantiPhysCritRateMulti = cfg.getDouble("lianti-physical-crit-rate-multiplier", 1.2);
        liantiPhysPenMulti = cfg.getDouble("lianti-physical-penetration-multiplier", 1.3);
        liantiPhysBlockMulti = cfg.getDouble("lianti-physical-block-multiplier", 1.3);
        liantiMagDmgMulti = cfg.getDouble("lianti-magical-damage-multiplier", 0.7);
        liantiMagDefMulti = cfg.getDouble("lianti-magical-defense-multiplier", 0.8);
        
        xiufaMagDmgMulti = cfg.getDouble("xiufa-magical-damage-multiplier", 1.3);
        xiufaMagCritRateMulti = cfg.getDouble("xiufa-magical-crit-rate-multiplier", 1.2);
        xiufaMagPenMulti = cfg.getDouble("xiufa-magical-penetration-multiplier", 1.3);
        xiufaMagBlockMulti = cfg.getDouble("xiufa-magical-block-multiplier", 1.3);
        xiufaPhysDmgMulti = cfg.getDouble("xiufa-physical-damage-multiplier", 0.7);
        xiufaPhysDefMulti = cfg.getDouble("xiufa-physical-defense-multiplier", 0.8);
        
        // 攻速
        atkSpeedPerLevel = cfg.getDouble("attack-speed-per-level", 0.003);
        maxAtkSpeed = cfg.getDouble("max-attack-speed", 4.0);
        
        realmMultiplierEnabled = cfg.getBoolean("realm-multiplier-enabled", true);
    }
    
    public CombatData calculate(String activeType, int liantiLevel, int xiufaLevel) {
        CombatData data = new CombatData(activeType, liantiLevel, xiufaLevel);
        
        int ltRealmIdx = liantiLevel / realmLevels;
        int xfRealmIdx = xiufaLevel / realmLevels;
        
        // === HP（指数增长） ===
        double ltHp = liantiLevel * hpPerLevel * liantiHpMulti * Math.pow(1 + ltRealmIdx, hpRealmPower);
        double xfHp = xiufaLevel * hpPerLevel * xiufaHpMulti * Math.pow(1 + xfRealmIdx, hpRealmPower);
        data.maxHp = hpBase + ltHp + xfHp;
        
        // === 真元（指数增长） ===
        data.maxMana = calcManaForType(liantiLevel, liantiManaMulti) + calcManaForType(xiufaLevel, xiufaManaMulti);
        
        // === 物理伤害（指数增长） ===
        double ltPhysDmg = liantiLevel * physBaseDmgPerLevel * liantiPhysDmgMulti * Math.pow(1 + ltRealmIdx, physDmgRealmPower);
        double xfPhysDmg = xiufaLevel * physBaseDmgPerLevel * xiufaPhysDmgMulti * Math.pow(1 + xfRealmIdx, physDmgRealmPower);
        data.physicalBaseDamage = ltPhysDmg + xfPhysDmg;
        
        // 物理会心率（递减收益）
        double ltPhysCritRate = calcCritRate(liantiLevel, physCritRatePerLevel) * liantiPhysCritRateMulti;
        double xfPhysCritRate = calcCritRate(xiufaLevel, physCritRatePerLevel);
        data.physicalCritRate = Math.min(maxPhysCritRate, ltPhysCritRate + xfPhysCritRate);
        
        // 物理会心倍率
        double ltPhysCritMulti = calcStat(liantiLevel, physCritMultiPerLevel);
        double xfPhysCritMulti = calcStat(xiufaLevel, physCritMultiPerLevel);
        data.physicalCritMulti = 1.5 + ltPhysCritMulti + xfPhysCritMulti;
        
        // 物理破防（指数增长）
        double ltPhysPen = liantiLevel * physPenPerLevel * liantiPhysPenMulti * Math.pow(1 + ltRealmIdx, physPenRealmPower);
        double xfPhysPen = xiufaLevel * physPenPerLevel * Math.pow(1 + xfRealmIdx, physPenRealmPower);
        data.physicalPenetration = Math.min(maxPhysPen, ltPhysPen + xfPhysPen);
        
        // 物理格挡（每大境界）
        double ltPhysBlock = ltRealmIdx * physBlockPerRealm * liantiPhysBlockMulti;
        double xfPhysBlock = xfRealmIdx * physBlockPerRealm;
        data.physicalBlock = Math.min(maxPhysBlock, ltPhysBlock + xfPhysBlock);
        
        // 物理防御（指数增长）
        double ltPhysDef = liantiLevel * physDefPerLevel * Math.pow(1 + ltRealmIdx, physDefRealmPower);
        double xfPhysDef = xiufaLevel * physDefPerLevel * xiufaPhysDefMulti * Math.pow(1 + xfRealmIdx, physDefRealmPower);
        data.physicalDefense = Math.min(maxPhysDef, ltPhysDef + xfPhysDef);
        
        // === 法术伤害（指数增长） ===
        double ltMagDmg = liantiLevel * magBaseDmgPerLevel * liantiMagDmgMulti * Math.pow(1 + ltRealmIdx, magDmgRealmPower);
        double xfMagDmg = xiufaLevel * magBaseDmgPerLevel * xiufaMagDmgMulti * Math.pow(1 + xfRealmIdx, magDmgRealmPower);
        data.magicalBaseDamage = ltMagDmg + xfMagDmg;
        
        // 法术会心率（递减收益）
        double ltMagCritRate = calcCritRate(liantiLevel, magCritRatePerLevel);
        double xfMagCritRate = calcCritRate(xiufaLevel, magCritRatePerLevel) * xiufaMagCritRateMulti;
        data.magicalCritRate = Math.min(maxMagCritRate, ltMagCritRate + xfMagCritRate);
        
        // 法术会心倍率
        double ltMagCritMulti = calcStat(liantiLevel, magCritMultiPerLevel);
        double xfMagCritMulti = calcStat(xiufaLevel, magCritMultiPerLevel);
        data.magicalCritMulti = 1.5 + ltMagCritMulti + xfMagCritMulti;
        
        // 法术破防（指数增长）
        double ltMagPen = liantiLevel * magPenPerLevel * Math.pow(1 + ltRealmIdx, magPenRealmPower);
        double xfMagPen = xiufaLevel * magPenPerLevel * xiufaMagPenMulti * Math.pow(1 + xfRealmIdx, magPenRealmPower);
        data.magicalPenetration = Math.min(maxMagPen, ltMagPen + xfMagPen);
        
        // 法术格挡（每大境界）
        double ltMagBlock = ltRealmIdx * magBlockPerRealm;
        double xfMagBlock = xfRealmIdx * magBlockPerRealm * xiufaMagBlockMulti;
        data.magicalBlock = Math.min(maxMagBlock, ltMagBlock + xfMagBlock);
        
        // 法术防御（指数增长）
        double ltMagDef = liantiLevel * magDefPerLevel * liantiMagDefMulti * Math.pow(1 + ltRealmIdx, magDefRealmPower);
        double xfMagDef = xiufaLevel * magDefPerLevel * Math.pow(1 + xfRealmIdx, magDefRealmPower);
        data.magicalDefense = Math.min(maxMagDef, ltMagDef + xfMagDef);
        
        // === 会心抵抗/抗性（每大境界） ===
        data.critResistance = Math.min(maxCritRes, ltRealmIdx * critResPerRealm + xfRealmIdx * critResPerRealm);
        data.critTenacity = Math.min(maxCritTen, ltRealmIdx * critTenPerRealm + xfRealmIdx * critTenPerRealm);
        
        // === 攻速 ===
        double ltSpeed = calcStat(liantiLevel, atkSpeedPerLevel);
        double xfSpeed = calcStat(xiufaLevel, atkSpeedPerLevel);
        data.attackSpeed = Math.min(maxAtkSpeed, 2.0 + ltSpeed + xfSpeed);
        
        return data;
    }
    
    private double calcManaForType(int level, double typeMulti) {
        if (level <= 0) return 0;
        int realmIdx = level / realmLevels;
        double mana = level * manaPerLevel * typeMulti;
        mana += (level / 10) * manaChongBonus * typeMulti;
        for (int i = 0; i < realmIdx; i++) mana += manaRealmBonus * Math.pow(2, i) * typeMulti;
        mana *= Math.pow(1 + realmIdx, manaRealmPower);
        return mana;
    }
    
    /**
     * 会心率：递减收益公式
     * critRate = level × perLevel × (1 - level/2000)
     * 保证在高等级时不会超过75%
     */
    private double calcCritRate(int level, double perLevel) {
        if (level <= 0) return 0;
        double diminishing = Math.max(0.1, 1.0 - level / 2000.0);
        return level * perLevel * diminishing;
    }
    
    /**
     * 通用属性计算：level × perLevel
     */
    private double calcStat(int level, double perLevel) {
        if (level <= 0 || perLevel <= 0) return 0;
        return level * perLevel;
    }
    
    public double getManaRecovery() { return manaRecovery; }
}
