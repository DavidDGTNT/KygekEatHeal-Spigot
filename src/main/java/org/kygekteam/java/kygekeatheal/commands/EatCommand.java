package org.kygekteam.java.kygekeatheal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kygekteam.java.kygekeatheal.Main;

public class EatCommand implements CommandExecutor {

    private Main owner;

    public EatCommand(Main owner) {
        this.owner = owner;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("kygekeatheal.eat")) return true;

        owner.reloadConfig();

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Main.PREFIX + Main.INFO + "Usage: /eat <player>");
                return true;
            }

            Object result = owner.eatTransaction((Player) sender);

            if (result.equals("true")) {
                sender.sendMessage(Main.PREFIX + Main.INFO + "You are already full!");
                return true;
            }
            if (result.equals("false")) {
                sender.sendMessage(Main.PREFIX + Main.WARNING + "You do not have enough money to eat!");
                return true;
            }

            String price = owner.economyEnabled ? " for " + result + " " + (owner.econ.currencyNamePlural() != null ? owner.econ.currencyNamePlural() : "money") : "";
            sender.sendMessage(Main.PREFIX + Main.INFO + "You have eaten" + price);
        } else {
            Player player = owner.getServer().getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(Main.PREFIX + Main.WARNING  + "Player is not online!");
                return true;
            }

            boolean isPlayer = sender instanceof Player;
            Object result = owner.eatTransaction(player, isPlayer);

            if (result.equals("true")) {
                sender.sendMessage(Main.PREFIX + Main.INFO + player.getName() + " is already full!");
                return true;
            }
            if (result.equals("false")) {
                sender.sendMessage(Main.PREFIX + Main.WARNING + "You do not have enough money to feed " + player.getName() + "!");
                return true;
            }

            String price = owner.economyEnabled && isPlayer ? " for " + result + " " + (owner.econ.currencyNamePlural() != null ? owner.econ.currencyNamePlural() : "money") : "";
            sender.sendMessage(Main.PREFIX + Main.INFO + "Player " + player.getName() + " has been fed" + price);
            player.sendMessage(Main.PREFIX + Main.INFO + "You have been fed by " + sender.getName());
        }

        return true;
    }
}
