package com.github.sirblobman.combatlogx.api.utility;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.combatlogx.api.ICombatLogX;

public final class CommandHelper {
    public static void runSync(ICombatLogX plugin, Runnable task) {
        JavaPlugin javaPlugin = plugin.getPlugin();
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTask(javaPlugin, task);
    }

    public static void runAsConsole(ICombatLogX plugin, String command) {
        try {
            CommandSender console = Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(console, command);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.SEVERE, "Failed to execute command '/" + command + "' in console:", ex);
        }
    }

    public static void runAsPlayer(ICombatLogX plugin, Player player, String command) {
        try {
            player.performCommand(command);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            String playerName = player.getName();
            logger.log(Level.SEVERE, "Failed to execute command '/" + command + "' as player '"
                    + playerName + "':", ex);
        }
    }

    public static void runAsOperator(ICombatLogX plugin, Player player, String command) {
        if (player.isOp()) {
            runAsPlayer(plugin, player, command);
            return;
        }

        try {
            player.setOp(true);
            player.performCommand(command);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            String playerName = player.getName();
            logger.log(Level.SEVERE, "Failed to execute command '/" + command + "' as player '"
                    + playerName + "' with operator permissions:", ex);
        } finally {
            player.setOp(false);
        }
    }
}
