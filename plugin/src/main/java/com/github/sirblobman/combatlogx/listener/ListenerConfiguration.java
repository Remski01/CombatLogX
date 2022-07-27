package com.github.sirblobman.combatlogx.listener;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.object.UntagReason;

public final class ListenerConfiguration extends CombatListener {
    public ListenerConfiguration(ICombatLogX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void beforeTag(PlayerPreTagEvent e) {
        printDebug("Detected PlayerPreTagEvent.");

        Player player = e.getPlayer();
        printDebug("Player: " + player.getName());

        if (checkDisabledWorld(player)) {
            printDebug("Player is in disabled world, cancelling.");
            e.setCancelled(true);
            return;
        }

        if (checkBypass(player)) {
            printDebug("Player has bypass, cancelling.");
            e.setCancelled(true);
            return;
        }

        Entity enemy = e.getEnemy();
        if (isSelfCombatDisabled(player, enemy)) {
            printDebug("Self combat is disabled, cancelling.");
            e.setCancelled(true);
            return;
        }

        printDebug("Finished default beforeTag check without cancellation.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTag(PlayerTagEvent e) {
        Player player = e.getPlayer();
        Entity enemy = e.getEnemy();
        runTagCommands(player, enemy);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        checkDeathUntag(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        checkDeathUntag(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        checkEnemyDeathUntag(entity);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) entity;
        checkEnemyDeathUntag(livingEntity);
    }

    private boolean checkDisabledWorld(Player player) {
        ConfigurationManager configurationManager = getPluginConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");

        List<String> disabledWorldList = configuration.getStringList("disabled-world-list");
        boolean inverted = configuration.getBoolean("disabled-world-list-inverted");

        World world = player.getWorld();
        String worldName = world.getName();
        boolean contains = disabledWorldList.contains(worldName);
        return (inverted != contains);
    }

    private boolean checkBypass(Player player) {
        ICombatManager combatManager = getCombatManager();
        return combatManager.canBypass(player);
    }

    private boolean isSelfCombatDisabled(Player player, Entity enemy) {
        if (enemy == null || doesNotEqual(player, enemy)) {
            return false;
        }

        ConfigurationManager configurationManager = getPluginConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        return !configuration.getBoolean("self-combat");
    }

    private boolean doesNotEqual(Entity entity1, Entity entity2) {
        if (entity1 == entity2) {
            return false;
        }

        UUID entityId1 = entity1.getUniqueId();
        UUID entityId2 = entity2.getUniqueId();
        return !entityId1.equals(entityId2);
    }

    private void checkDeathUntag(Player player) {
        ConfigurationManager configurationManager = getPluginConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        if (!configuration.getBoolean("untag-on-death")) {
            return;
        }

        ICombatManager combatManager = getCombatManager();
        if (combatManager.isInCombat(player)) {
            combatManager.untag(player, UntagReason.SELF_DEATH);
        }
    }

    private void checkEnemyDeathUntag(LivingEntity enemy) {
        ConfigurationManager configurationManager = getPluginConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        if (!configuration.getBoolean("untag-on-enemy-death")) {
            return;
        }

        ICombatManager combatManager = getCombatManager();
        List<Player> playerList = combatManager.getPlayersInCombat();
        for (Player player : playerList) {
            combatManager.untag(player, enemy, UntagReason.ENEMY_DEATH);
        }
    }

    private void runTagCommands(Player player, Entity enemy) {
        ConfigurationManager configurationManager = getPluginConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("commands.yml");
        List<String> tagCommandList = configuration.getStringList("tag-command-list");
        if (tagCommandList.isEmpty()) {
            return;
        }

        ICombatLogX plugin = getCombatLogX();
        IPlaceholderManager placeholderManager = plugin.getPlaceholderManager();
        placeholderManager.runReplacedCommands(player, Collections.singletonList(enemy), tagCommandList);
    }
}
