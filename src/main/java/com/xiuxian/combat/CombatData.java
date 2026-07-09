package com.xiuxian.combat;

/**
 * 玩家战斗数据 - 物理和法术完全独立
 */
public class CombatData {
    public String activeType;
    public int liantiLevel, xiufaLevel;
    
    // HP系统
    public double maxHp;
    
    // 真元系统
    public double maxMana;
    public double mana;
    
    // 物理伤害系统
    public double physicalBaseDamage;
    public double physicalCritRate;
    public double physicalCritMulti;
    public double physicalPenetration;
    public double physicalBlock;
    public double physicalDefense;
    
    // 法术伤害系统
    public double magicalBaseDamage;
    public double magicalCritRate;
    public double magicalCritMulti;
    public double magicalPenetration;
    public double magicalBlock;
    public double magicalDefense;
    
    // 会心抵抗/抗性
    public double critResistance;
    public double critTenacity;
    
    // 攻速
    public double attackSpeed;
    
    public CombatData(String activeType, int liantiLevel, int xiufaLevel) {
        this.activeType = activeType;
        this.liantiLevel = liantiLevel;
        this.xiufaLevel = xiufaLevel;
    }
    
    public int getActiveLevel() {
        return "lianti".equals(activeType) ? liantiLevel : xiufaLevel;
    }
    
    public int getTotalLevel() {
        return liantiLevel + xiufaLevel;
    }
    
    public boolean isLianti() {
        return "lianti".equals(activeType);
    }
}
