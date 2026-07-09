# 🗡️ XiuXianCombat

> MC 修仙服战斗插件 — 物理 / 法术双伤害独立战斗系统

[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

---

## 📖 简介

XiuXianCombat 提供完整的修仙战斗系统，支持**物理 / 法术双伤害独立计算**。体修擅长物理攻击，法修擅长法术攻击，通过类型倍率实现差异化。所有属性使用指数增长公式，确保后期也有成长空间。

## ✨ 功能特性

| 系统 | 说明 |
|------|------|
| ⚔️ 双伤害体系 | 物理伤害 + 法术伤害独立计算 |
| 💪 体修/法修分化 | 体修物理 ×1.3 / 法修法术 ×1.3 |
| 🎯 战斗属性 | 暴击率/暴击伤害/闪避率/格挡率/攻速 |
| 🔥 元素伤害 | 火/冰/雷元素，法修倍率 ×1.5 |
| 📈 指数增长 | alue = level × perL × typeM × (1+境界)^power |
| 🔌 PAPI 支持 | 21 个 PlaceholderAPI 变量 |

## 🏗️ 架构

`
XiuXianCombat (v1.0)
├── 战斗属性管理器 (CombatAttributeManager)
├── 战斗数据存储 (CombatData)
├── 事件监听器 (CombatEventListener)
├── PAPI 扩展 (XiuXianCombatExpansion)
├── 数据读取 → plugins/XiuXianCore/data/*.json
└── PAPI: %xicombat_*%
`

## ⚔️ 伤害计算

`
最终伤害 = 物理伤害 + 法术伤害

物理伤害 = 物理基础 × (1+物理会心) × (1-物理防御减免) × 物理格挡
法术伤害 = 法术基础 × (1+法术会心) × (1-法术防御减免) × 法术格挡
`

### 破防公式

`
有效防御 = 目标防御 × (1.0 - 破防百分比 / 100.0)
防御减免 = 有效防御 / (100.0 + 有效防御)
`

### 会心抵抗

`
最终会心率 = 攻击方会心率 - 防御方会心抵抗
最终会心倍率 = 攻击方会心倍率 - 防御方会心抗性 / 100
`

### 体修 / 法修加成

| 属性 | 体修倍率 | 法修倍率 |
|------|----------|----------|
| 气血 | ×1.3 | ×0.88 |
| 真元 | ×0.7 | ×1.3 |
| 物理伤害 | ×1.3 | ×0.5 |
| 法术伤害 | ×0.5 | ×1.3 |
| 暴击率 | ×1.3 | ×1.0 |
| 格挡率 | ×1.3 | ×1.0 |
| 闪避率 | ×0.8 | ×1.3 |
| 火/冰/雷元素 | ×1.0 | ×1.5 |

## 📊 属性上限

| 属性 | 上限 | 说明 |
|------|------|------|
| 暴击率 | 75% | 递减收益：level × perL × max(0.1, 1.0 - level/2000) |
| 闪避率 | 60% | — |
| 格挡率 | 65% | — |
| 攻速 | 4.0 | — |

## 📦 依赖

| 插件 | 状态 | 说明 |
|------|------|------|
| [XiuXianCore](https://github.com/Canmender/xiuxian-core) | ✅ 必需 | 读取玩家等级和境界数据 |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | 推荐 | PAPI 变量支持 |

## 📊 PlaceholderAPI 变量

### 气血 & 真元

| 变量 | 说明 |
|------|------|
| %xicombat_max_hp% | 最大气血 |
| %xicombat_max_mana% | 最大真元 |

### 物理伤害

| 变量 | 说明 |
|------|------|
| %xicombat_phy_base_damage% | 物理基础伤害 |
| %xicombat_phy_crit_rate% | 物理暴击率 |
| %xicombat_phy_crit_damage% | 物理暴击倍率 |
| %xicombat_phy_penetration% | 物理破防 |
| %xicombat_phy_block_rate% | 物理格挡率 |

### 法术伤害

| 变量 | 说明 |
|------|------|
| %xicombat_mag_base_damage% | 法术基础伤害 |
| %xicombat_mag_crit_rate% | 法术暴击率 |
| %xicombat_mag_crit_damage% | 法术暴击倍率 |
| %xicombat_mag_penetration% | 法术破防 |
| %xicombat_mag_block_rate% | 法术格挡率 |

### 防御 & 闪避

| 变量 | 说明 |
|------|------|
| %xicombat_phy_defense% | 物理防御 |
| %xicombat_mag_defense% | 法术防御 |
| %xicombat_dodge_rate% | 闪避率 |
| %xicombat_crit_resist% | 会心抵抗 |
| %xicombat_crit_ant% | 会心抗性 |

### 元素伤害

| 变量 | 说明 |
|------|------|
| %xicombat_fire_damage% | 火元素伤害 |
| %xicombat_ice_damage% | 冰元素伤害 |
| %xicombat_thunder_damage% | 雷元素伤害 |

## ⚙️ 配置

`yaml
# 每级增长
crit-rate-per-level: 0.15
crit-damage-per-level: 0.2
dodge-rate-per-level: 0.1
block-rate-per-level: 0.12
attack-speed-per-level: 0.003
fire-damage-per-level: 0.08
ice-damage-per-level: 0.08
thunder-damage-per-level: 0.08

# 体修/法修加成
lianti-crit-multiplier: 1.3
lianti-block-multiplier: 1.3
lianti-dodge-multiplier: 0.8
xiufa-dodge-multiplier: 1.3
xiufa-fire-multiplier: 1.5
xiufa-ice-multiplier: 1.5
xiufa-thunder-multiplier: 1.5

# 属性上限
max-crit-rate: 75.0
max-dodge-rate: 60.0
max-block-rate: 65.0
max-attack-speed: 4.0
`

## 🔨 编译

`ash
javac -cp paper-api-1.21.4.jar:gson-2.11.0.jar:PlaceholderAPI-2.12.3.jar \
      -d build \
      -sourcepath src/main/java \
      src/main/java/com/xiuxian/combat/*.java
`

## 🚀 部署

1. 确保 XiuXianCore 已安装
2. 编译 JAR 文件
3. 放入 server/plugins/ 目录
4. 重启服务器

## 📝 更新日志

### v1.0 (2026-07-08)
- 物理/法术双伤害独立系统
- 指数增长属性公式
- 21 个 PAPI 变量
- HP 上限 1024（MC 限制）

---

**相关项目：**
- [XiuXianCore](https://github.com/Canmender/xiuxian-core) — 核心等级系统
- [XiuXianPill](https://github.com/Canmender/xiuxian-pill) — 丹药系统
- [XiuXianItems](https://github.com/Canmender/xiuxian-items) — 自定义物品系统

## 📄 License

[MIT](LICENSE)
