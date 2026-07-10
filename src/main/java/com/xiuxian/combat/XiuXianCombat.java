package com.xiuxian.combat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XiuXianCombat extends JavaPlugin implements CommandExecutor, Listener {
    
    private final Map<UUID, CombatData> combatDataCache = new HashMap<>();
    private CombatAttributeManager attrManager;
    private File xiuxianDataDir;
    private boolean showCombatLog;
    
    private static final String MODIFIER_SPEED = "xicombat_speed";
    private static final String MODIFIER_HP = "xicombat_hp";
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        loadConfig();
        
        getCommand("xcombat").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(new CombatEventListener(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new XiuXianCombatExpansion(this).register();
            getLogger().info("PAPI placeholders registered");
        }
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            loadCombatData(p.getUniqueId());
            applyCombatAttributes(p);
        }
        
        // 真元恢复定时器（每秒）
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                CombatData data = combatDataCache.get(p.getUniqueId());
                if (data != null && data.mana < data.maxMana) {
                    data.mana = Math.min(data.maxMana, data.mana + attrManager.getManaRecovery());
                }
            }
        }, 20L, 20L);
        
        // HP action bar display (every 1 second)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                CombatData data = combatDataCache.get(p.getUniqueId());
                if (data != null) {
                    double realHp = p.getHealth();
                    int displayHp = (int) Math.min(realHp, data.maxHp);
                    int maxHp = (int) data.maxHp;
                    int percent = (int)(displayHp * 100.0 / maxHp);
                    String msg = getConfig().getString("hp-actionbar-format",
                        '&' + "c" + '&' + "lHP " + '&' + "f{hp} " + '&' + "7/ " + '&' + "f{max_hp} (" + '&' + "e{percent}%" + '&' + "7)");
                    msg = msg.replace("{hp}", String.valueOf(displayHp))
                             .replace("{max_hp}", String.valueOf(maxHp))
                             .replace("{percent}", String.valueOf(percent));
                    p.sendActionBar(ChatColor.translateAlternateColorCodes('&', msg));
                }
            }
        }, 20L, 20L);
        
        getLogger().info("XiuXianCombat enabled");
    }
    
    @Override
    public void onDisable() {
        combatDataCache.clear();
    }
    
    private void loadConfig() {
        FileConfiguration cfg = getConfig();
        
        String dataPath = cfg.getString("xiuxian-data-dir", "../XiuXianCore/data");
        xiuxianDataDir = new File(getDataFolder(), dataPath);
        if (!xiuxianDataDir.exists()) {
            xiuxianDataDir = new File("D:/MineCraft/server/plugins/XiuXianCore/data");
        }
        
        showCombatLog = cfg.getBoolean("show-combat-log", true);
        
        int realmLevels = 100;
        File xiuxianConfig = new File("plugins/XiuXianCore/config.yml");
        if (xiuxianConfig.exists()) {
            try {
                FileConfiguration xiCfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(xiuxianConfig);
                realmLevels = xiCfg.getInt("realm-levels", 100);
            } catch (Exception e) {
                getLogger().warning("Failed to read XiuXianCore config: " + e.getMessage());
            }
        }
        
        attrManager = new CombatAttributeManager();
        attrManager.loadConfig(cfg, realmLevels);
        
        getLogger().info("XiuXian data dir: " + xiuxianDataDir.getAbsolutePath() + " exists=" + xiuxianDataDir.exists());
    }
    
    // ========== 数据读取 ==========
    
    public void loadCombatData(UUID uuid) {
        CombatData data = readXiuxianData(uuid);
        if (data != null) {
            CombatData calculated = attrManager.calculate(data.activeType, data.liantiLevel, data.xiufaLevel);
            // 保留真元当前值
            calculated.mana = data.mana;
            combatDataCache.put(uuid, calculated);
            getLogger().info("Loaded combat data for " + uuid + ": L=" + data.liantiLevel + " X=" + data.xiufaLevel);
        } else {
            getLogger().warning("No XiuXian data found for " + uuid);
        }
    }
    
    private CombatData readXiuxianData(UUID uuid) {
        if (xiuxianDataDir == null || !xiuxianDataDir.exists()) return null;
        File file = new File(xiuxianDataDir, uuid.toString() + ".json");
        if (!file.exists()) return null;
        try {
            JsonObject json = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
            String activeType = json.has("activeType") ? json.get("activeType").getAsString() : "lianti";
            int liantiLevel = json.has("liantiLevel") ? json.get("liantiLevel").getAsInt() : 0;
            int xiufaLevel = json.has("xiufaLevel") ? json.get("xiufaLevel").getAsInt() : 0;
            double mana = json.has("mana") ? json.get("mana").getAsDouble() : 0;
            CombatData data = new CombatData(activeType, liantiLevel, xiufaLevel);
            data.mana = mana;
            return data;
        } catch (Exception e) {
            getLogger().warning("Failed to read XiuXian data for " + uuid);
            return null;
        }
    }
    
    public CombatData getCombatData(UUID uuid) {
        return combatDataCache.get(uuid);
    }
    
    public boolean isShowCombatLog() {
        return showCombatLog;
    }
    
    // ========== 属性应用 ==========
    
    private void applyCombatAttributes(Player p) {
        CombatData data = combatDataCache.get(p.getUniqueId());
        if (data == null) return;
        
        // 应用HP
        AttributeInstance healthAttr = p.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            double currentHp = p.getHealth();
            applyModifier(p, Attribute.MAX_HEALTH, MODIFIER_HP, data.maxHp - 20);
            // 回满血
            if (currentHp < Math.min(1024, data.maxHp)) p.setHealth(Math.min(1024, data.maxHp));
        }
        
        // 应用攻速
        applyModifier(p, Attribute.ATTACK_SPEED, MODIFIER_SPEED, data.attackSpeed - 2.0);
    }
    
    private void applyModifier(Player p, Attribute attribute, String name, double value) {
        AttributeInstance instance = p.getAttribute(attribute);
        if (instance == null) return;
        NamespacedKey key = new NamespacedKey(this, name);
        instance.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .forEach(instance::removeModifier);
        if (value > 0) {
            instance.addTransientModifier(new AttributeModifier(key, value, AttributeModifier.Operation.ADD_NUMBER));
        }
    }
    
    // ========== 事件 ==========
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            loadCombatData(p.getUniqueId());
            applyCombatAttributes(p);
        }, 40L);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // 保存真元到XiuXianCore的JSON
        CombatData data = combatDataCache.get(uuid);
        if (data != null) {
            saveManaToXiuxian(uuid, data.mana);
        }
        combatDataCache.remove(uuid);
    }
    
    private void saveManaToXiuxian(UUID uuid, double mana) {
        if (xiuxianDataDir == null || !xiuxianDataDir.exists()) return;
        File file = new File(xiuxianDataDir, uuid.toString() + ".json");
        if (!file.exists()) return;
        try {
            JsonObject json = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
            json.addProperty("mana", mana);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(json.toString());
            writer.close();
        } catch (Exception e) {
            getLogger().warning("Failed to save mana for " + uuid);
        }
    }
    
    // ========== 命令 ==========
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equals("reload")) {
            if (!sender.hasPermission("xicombat.admin")) {
                sender.sendMessage("没有权限");
                return true;
            }
            reloadConfig();
            loadConfig();
            for (Player p : Bukkit.getOnlinePlayers()) {
                loadCombatData(p.getUniqueId());
                applyCombatAttributes(p);
            }
            sender.sendMessage(ChatColor.GREEN + "XiuXianCombat 已重载");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以使用此命令");
            return true;
        }
        
        Player p = (Player) sender;
        CombatData data = combatDataCache.get(p.getUniqueId());
        if (data == null) {
            p.sendMessage(ChatColor.RED + "战斗数据未加载");
            return true;
        }
        
        FileConfiguration cfg = getConfig();
        String title = cfg.getString("combat-stats-title", "&6&l━━ 战斗属性 ━━");
        
        p.sendMessage(colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━"));
        p.sendMessage(colorize(title));
        p.sendMessage(colorize(""));
        
        // HP/真元
        p.sendMessage(colorize(" &aHP: &f" + String.format("%.0f", p.getHealth()) + " / " + String.format("%.0f", data.maxHp)));
        p.sendMessage(colorize(" &b真元: &f" + String.format("%.0f", data.mana) + " / " + String.format("%.0f", data.maxMana)));
        p.sendMessage(colorize(""));
        
        // 物理伤害系统
        p.sendMessage(colorize(" &c━━ 物理伤害 ━━"));
        p.sendMessage(colorize(" &7基础伤害: &f" + String.format("%.1f", data.physicalBaseDamage)));
        p.sendMessage(colorize(" &7会心率: &f" + String.format("%.1f%%", data.physicalCritRate) + " &7会心倍率: &f" + String.format("%.0f%%", data.physicalCritMulti * 100)));
        p.sendMessage(colorize(" &7破防: &f" + String.format("%.1f%%", data.physicalPenetration) + " &7格挡: &f" + String.format("%.1f%%", data.physicalBlock)));
        p.sendMessage(colorize(" &7防御: &f" + String.format("%.1f", data.physicalDefense)));
        p.sendMessage(colorize(""));
        
        // 法术伤害系统
        p.sendMessage(colorize(" &9━━ 法术伤害 ━━"));
        p.sendMessage(colorize(" &7基础伤害: &f" + String.format("%.1f", data.magicalBaseDamage)));
        p.sendMessage(colorize(" &7会心率: &f" + String.format("%.1f%%", data.magicalCritRate) + " &7会心倍率: &f" + String.format("%.0f%%", data.magicalCritMulti * 100)));
        p.sendMessage(colorize(" &7破防: &f" + String.format("%.1f%%", data.magicalPenetration) + " &7格挡: &f" + String.format("%.1f%%", data.magicalBlock)));
        p.sendMessage(colorize(" &7防御: &f" + String.format("%.1f", data.magicalDefense)));
        p.sendMessage(colorize(""));
        
        // 防御属性
        p.sendMessage(colorize(" &e━━ 防御 ━━"));
        p.sendMessage(colorize(" &7会心抵抗: &f" + String.format("%.1f%%", data.critResistance) + " &7会心抗性: &f" + String.format("%.1f%%", data.critTenacity)));
        p.sendMessage(colorize(" &e攻速: &f" + String.format("%.2f", data.attackSpeed)));
        p.sendMessage(colorize("&6&l━━━━━━━━━━━━━━━━━━━━━━"));
        
        return true;
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
