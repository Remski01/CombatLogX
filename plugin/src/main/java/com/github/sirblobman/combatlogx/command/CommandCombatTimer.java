package com.github.sirblobman.combatlogx.command;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.language.MultiReplacer;
import com.github.sirblobman.api.language.Replacer;
import com.github.sirblobman.api.language.SimpleReplacer;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.command.CombatLogPlayerCommand;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.object.TagInformation;

public final class CommandCombatTimer extends CombatLogPlayerCommand {
    public CommandCombatTimer(ICombatLogX plugin) {
        super(plugin, "combat-timer");
        setPermissionName("combatlogx.command.combat-timer");
    }

    @Override
    protected List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            Set<String> valueSet = getOnlinePlayerNames();
            return getMatching(args[0], valueSet);
        }

        return Collections.emptyList();
    }

    @Override
    protected boolean execute(Player player, String[] args) {
        if (args.length < 1) {
            checkSelf(player);
            return true;
        }

        Player target = findTarget(player, args[0]);
        if (target == null) {
            return true;
        }

        checkOther(player, target);
        return true;
    }

    private void checkSelf(Player player) {
        ICombatLogX plugin = getCombatLogX();
        ICombatManager combatManager = plugin.getCombatManager();
        LanguageManager languageManager = getLanguageManager();

        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation == null || tagInformation.isExpired()) {
            sendMessageWithPrefix(player, "error.self-not-in-combat", null);
            return;
        }

        double timeLeftMillis = tagInformation.getMillisLeftCombined();
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);

        String timeLeftString = languageManager.formatDecimal(player, timeLeftSeconds);
        Replacer replacer = new SimpleReplacer("{time_left}", timeLeftString);
        sendMessageWithPrefix(player, "command.combat-timer.time-left-self", replacer);
    }

    private void checkOther(Player player, Player target) {
        ICombatLogX plugin = getCombatLogX();
        ICombatManager combatManager = plugin.getCombatManager();
        LanguageManager languageManager = getLanguageManager();
        String targetName = target.getName();

        TagInformation tagInformation = combatManager.getTagInformation(target);
        if (tagInformation == null || tagInformation.isExpired()) {
            Replacer replacer = new SimpleReplacer("{target}", targetName);
            sendMessageWithPrefix(player, "error.target-not-in-combat", replacer);
            return;
        }

        double timeLeftMillis = tagInformation.getMillisLeftCombined();
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);
        String timeLeftString = languageManager.formatDecimal(player, timeLeftSeconds);

        Replacer replacer = new MultiReplacer("{time_left}", timeLeftString)
                .addReplacement("{target}", targetName);
        sendMessageWithPrefix(player, "command.combat-timer.time-left-other", replacer);
    }
}
