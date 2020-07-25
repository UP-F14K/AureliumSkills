package io.github.archy_x.aureliumskills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.github.archy_x.aureliumskills.AureliumSkills;
import io.github.archy_x.aureliumskills.Options;
import io.github.archy_x.aureliumskills.lang.Lang;
import io.github.archy_x.aureliumskills.lang.Message;
import io.github.archy_x.aureliumskills.menu.SkillsMenu;
import io.github.archy_x.aureliumskills.skills.Leaderboard;
import io.github.archy_x.aureliumskills.skills.PlayerSkill;
import io.github.archy_x.aureliumskills.skills.Skill;
import io.github.archy_x.aureliumskills.skills.SkillLoader;
import io.github.archy_x.aureliumskills.skills.levelers.Leveler;
import io.github.archy_x.aureliumskills.stats.Health;
import io.github.archy_x.aureliumskills.stats.Luck;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

@CommandAlias("skills|sk|skill")
public class SkillsCommand extends BaseCommand {
 
	private Plugin plugin;
	private Options options;
	private Lang lang;
	private Leaderboard leaderboard;

	public SkillsCommand(Plugin plugin) {
		this.plugin = plugin;
		options = new Options(plugin);
		lang = new Lang(plugin);
		leaderboard = new Leaderboard();
	}
	
	@Default
	public void onSkills(Player player) {
		SkillsMenu.getInventory(player).open(player);
	}
	
	@Subcommand("xp add")
	@CommandCompletion("@players @skills")
	@CommandPermission("aureliumskills.xp.add")
	public void onXpAdd(CommandSender sender, @Flags("other") Player player, Skill skill, double amount) {
		if (Options.isEnabled(skill)) {
			Leveler.addXp(player, skill, amount);
		}
		else {
			sender.sendMessage(AureliumSkills.tag + ChatColor.YELLOW + Lang.getMessage(Message.UNKNOWN_SKILL));
		}
	}

	@Subcommand("top")
	@CommandCompletion("@skills")
	@CommandPermission("aureliumskills.top")
	public void onTop(CommandSender sender, @Optional Skill skill) {
		if (Options.isEnabled(skill)) {
			if (skill == null) {
				List<PlayerSkill> powerLeaderboard = leaderboard.getPowerLeaderBoard();
				String message = ChatColor.AQUA + "" + ChatColor.BOLD + Lang.getMessage(Message.SKILL_LEADERBOARD) + ChatColor.WHITE + " (" + Lang.getMessage(Message.ALL_SKILLS) + ")" ;
				int num = 1;
				for (PlayerSkill playerSkill : powerLeaderboard) {
					if (num <= 10) {
						message += "\n" + num + ". " + playerSkill.getPlayerName() + " - " + playerSkill.getPowerLevel();
						num++;
					} else {
						break;
					}
				}
				sender.sendMessage(message);
			} else {
				List<PlayerSkill> powerLeaderboard = leaderboard.getSkillLeaderBoard(skill);
				String message = ChatColor.AQUA + "" + ChatColor.BOLD + Lang.getMessage(Message.SKILL_LEADERBOARD) + ChatColor.WHITE + " (" + Lang.getMessage(Message.valueOf(skill.getName().toUpperCase() + "_NAME"))+ ")";
				int num = 1;
				for (PlayerSkill playerSkill : powerLeaderboard) {
					if (num <= 10) {
						message += "\n" + num + ". " + playerSkill.getPlayerName() + " - " + playerSkill.getSkillLevel(skill);
						num++;
					} else {
						break;
					}
				}
				sender.sendMessage(message);
			}
		}
		else {
			sender.sendMessage(AureliumSkills.tag + ChatColor.YELLOW + Lang.getMessage(Message.UNKNOWN_SKILL));
		}
	}


	@Subcommand("lang")
	@CommandCompletion("@lang")
	@CommandPermission("aureliumskills.lang")
	public void onLanguage(Player player, String language) {
		if (lang.setLanguage(language)) {
			player.sendMessage(AureliumSkills.tag + ChatColor.GREEN + Lang.getMessage(Message.LANGUAGE_SET_TO).replace("_", language));
		}
		else {
			player.sendMessage(AureliumSkills.tag + ChatColor.RED + Lang.getMessage(Message.LANGUAGE_NOT_FOUND));
		}
	}
	
	
	@Subcommand("reload")
	@CommandPermission("aureliumskills.reload")
	public void reload(CommandSender sender) {
		plugin.reloadConfig();
		plugin.saveDefaultConfig();
		options.loadConfig();
		lang.loadLanguages();
		AureliumSkills.abilityOptionManager.loadOptions();
		Leveler.loadLevelReqs();
		AureliumSkills.lootTableManager.loadLootTables();
		AureliumSkills.worldManager.loadWorlds();
		if (AureliumSkills.worldGuardEnabled) {
			AureliumSkills.worldGuardSupport.loadRegions();
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			Health.reload(player);
			Luck.reload(player);
		}
		sender.sendMessage(AureliumSkills.tag + ChatColor.GREEN + Lang.getMessage(Message.CONFIG_RELOADED));
	}
	
	@Subcommand("skill setlevel")
	@CommandCompletion("@players @skills")
	@CommandPermission("aureliumskills.skill.setlevel")
	public void onSkillLevelSet(CommandSender sender, @Flags("other") Player player, Skill skill, int level) {
		if (Options.isEnabled(skill)) {
			if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
				if (level > 0) {
					PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
					playerSkill.setSkillLevel(skill, level);
					playerSkill.setXp(skill, 0);
					Leveler.updateStats(player);
					Leveler.updateAbilities(player, skill);
					sender.sendMessage(AureliumSkills.tag + ChatColor.GRAY + "Skill " + ChatColor.AQUA + skill.getDisplayName() + ChatColor.GRAY + " set to level " + ChatColor.AQUA + level + ChatColor.GRAY + " for player " + ChatColor.GOLD + player.getName());
				} else {
					sender.sendMessage(AureliumSkills.tag + ChatColor.YELLOW + "Level must be at least 1!");
				}
			}
		}
		else {
			sender.sendMessage(AureliumSkills.tag + ChatColor.YELLOW + Lang.getMessage(Message.UNKNOWN_SKILL));
		}
	}
	
}