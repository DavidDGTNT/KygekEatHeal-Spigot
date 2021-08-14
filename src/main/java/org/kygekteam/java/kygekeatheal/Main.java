package org.kygekteam.java.kygekeatheal;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import org.kygekteam.java.kygekeatheal.commands.EatCommand;
import org.kygekteam.java.kygekeatheal.commands.HealCommand;

public class Main extends JavaPlugin {

    public static final String PREFIX = ChatColor.YELLOW + "[KygekEatHeal] " + ChatColor.RESET;
    public static final String INFO = ChatColor.GREEN.toString();
    public static final String WARNING = ChatColor.RED.toString();

    public boolean economyEnabled = false;
    public Economy econ = null;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getServer().getConsoleSender().sendMessage("Vault plugin or economy plugin is not installed or enabled, all actions will be free");
        } else {
            this.economyEnabled = true;
        }

        this.saveDefaultConfig();
        if (!this.getConfig().getString("config-version").equals("1.0")) {
            getServer().getConsoleSender().sendMessage("Your configuration file is outdated, updating the config.yml...");
            getServer().getConsoleSender().sendMessage("The old configuration file can be found at config_old.yml");
            this.renameConfig();
            this.saveDefaultConfig();
            this.reloadConfig();
        }

        HealCommand healCommand = new HealCommand(this);
        EatCommand eatCommand = new EatCommand(this);

        getCommand("heal").setExecutor(healCommand);
        getCommand("eat").setExecutor(eatCommand);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private double getEatValue(Player player) {
        String config = this.getConfig().getString("eat-value", "max");
        double maxFood = 20;
        double food = maxFood - player.getFoodLevel();

        return (config.equals("max") ? maxFood :
                (Double.parseDouble(config) > food ? maxFood : Double.parseDouble(config) + player.getFoodLevel()));
    }

    private double getHealValue(Player player) {
        String config = this.getConfig().getString("heal-value", "max");
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue();
        double health = maxHealth - player.getHealth();

        return (config.equals("max") ? maxHealth :
                (Double.parseDouble(config) > health ? maxHealth : Double.parseDouble(config) + player.getHealth()));
    }

    public String eatTransaction(Player player, boolean economyEnabled) {
        if (player.getFoodLevel() == 20) return "true";
        Double price = null;

        if (this.economyEnabled && economyEnabled) {
            price = this.getConfig().getDouble("eat-price", 0);
            if (this.econ.getBalance(player) < price) return "false";
            this.econ.withdrawPlayer(player, price);
        }

        double eatValue = this.getEatValue(player);
        player.setFoodLevel((int) eatValue);
        player.setSaturation(20);
        return price == null ? "0" : price.toString();
    }

    public String eatTransaction(Player player) {
        return this.eatTransaction(player, true);
    }

    public String healTransaction(Player player, boolean economyEnabled) {
        if (player.getHealth() == 20) return "true";
        Double price = null;

        if (this.economyEnabled && economyEnabled) {
            price = this.getConfig().getDouble("heal-price", 0);
            if (this.econ.getBalance(player) < price) return "false";
            this.econ.withdrawPlayer(player, price);
        }


        double healValue = this.getHealValue(player);
        player.setHealth((float) healValue);
        return price == null ? "0" : price.toString();
    }

    public String healTransaction(Player player) {
        return this.healTransaction(player, true);
    }

    private void renameConfig() {
        File oldConfig = new File(this.getDataFolder() + "/config.yml");
        File newConfig = new File(this.getDataFolder() + "/config-old.yml");

        if (newConfig.exists()) newConfig.delete();

        oldConfig.renameTo(newConfig);
    }
}
