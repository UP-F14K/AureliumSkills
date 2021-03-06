package com.archyx.aureliumskills.skills;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.stats.PlayerStat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkillLoader {

	public static ConcurrentHashMap<UUID, PlayerSkill> playerSkills = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<UUID, PlayerStat> playerStats = new ConcurrentHashMap<>();

	public static boolean isSaving;
	private final AureliumSkills plugin;

	public SkillLoader(AureliumSkills plugin) {
		this.plugin = plugin;
		isSaving = false;
	}
	
	public void loadSkillData() {
		Bukkit.getLogger().info("[AureliumSkills] Loading Skill Data...");
		long startTime = System.currentTimeMillis();
		int playersLoaded = 0;
		File file = new File(plugin.getDataFolder(), "data.yml");
		if (file.exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			ConfigurationSection configurationSection = config.getConfigurationSection("skillData");
			if (configurationSection != null) {
				for (String stringId : configurationSection.getKeys(false)) {
					try {
						UUID id = UUID.fromString(stringId);
						String name = config.getString("skillData." + stringId + ".name", stringId);
						PlayerSkill playerSkill = new PlayerSkill(id, name, plugin);
						PlayerStat playerStat = new PlayerStat(id, plugin);
						//Loading skill and stat data
						ConfigurationSection section = config.getConfigurationSection("skillData." + stringId + ".skills");
						if (section != null) {
							for (String skillName : section.getKeys(false)) {
								String skillData = config.getString("skillData." + stringId + ".skills." + skillName);
								int level;
								double xp;
								if (skillData != null) {
									String[] skillDataArray = skillData.split(":");
									level = Integer.parseInt(skillDataArray[0]);
									xp = 0.0;
									if (skillDataArray.length >= 2) {
										xp = Double.parseDouble(skillDataArray[1].replace(",", "."));
									}
								} else {
									level = 0;
									xp = 0.0;
								}
								Skill skill = Skill.valueOf(skillName.toUpperCase());
								playerSkill.setSkillLevel(skill, level);
								playerSkill.setXp(skill, xp);

								playerStat.addStatLevel(skill.getPrimaryStat(), level - 1);
								playerStat.addStatLevel(skill.getSecondaryStat(), level / 2);
							}

						}
						playerSkills.put(id, playerSkill);
						playerStats.put(id, playerStat);
						playersLoaded++;
					} catch (Exception e) {
						Bukkit.getLogger().warning("[AureliumSkills] Error loading skill data for player with uuid " + stringId);
					}
				}
			}
		}
		else {
			plugin.saveResource("data.yml", false);
		}
		long endTime = System.currentTimeMillis();
		Bukkit.getLogger().info("[AureliumSkills] Loaded " + playersLoaded + " Player Skill Data in " + (endTime - startTime) + "ms");
		//Update leaderboards
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getLeaderboard().updateLeaderboards(false);
			}
		}.runTaskAsynchronously(plugin);
	}

	public void saveSkillData(boolean silent) {
		isSaving = true;
		File file = new File(plugin.getDataFolder(), "data.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		if (!silent) {
			Bukkit.getLogger().info("[AureliumSkills] Saving Skill Data...");
		}
		long start = System.currentTimeMillis();
		for (UUID id : playerSkills.keySet()) {
			PlayerSkill playerSkill = playerSkills.get(id);
			if (playerSkill.hasData()) {
				config.set("skillData." + id.toString() + ".name", playerSkill.getPlayerName());
				//Saving skills
				for (Skill skill : playerSkill.getSkillSet()) {
					int level = playerSkill.getSkillLevel(skill);
					double xp = playerSkill.getXp(skill);
					config.set("skillData." + id.toString() + ".skills." + skill, level + ":" + ((double) Math.round(xp * 1000) / 1000));
				}
			}
			else {
				ConfigurationSection section = config.getConfigurationSection("skillData." + id.toString());
				if (section != null) {
					config.set("skillData." + id.toString(), null);
				}
			}
		}
		try {
			if (!silent) {
				long end = System.currentTimeMillis();
				Bukkit.getLogger().info("[AureliumSkills] Mappings saved in " + (end - start) + " ms");
			}
			long fileStart = System.currentTimeMillis();
			//Save config
			config.save(file);
			if (!silent) {
				long fileEnd = System.currentTimeMillis();
				Bukkit.getLogger().info("[AureliumSkills] File saved in " + (fileEnd - fileStart) + " ms");
			}
		}
		catch (IOException e) {
			Bukkit.getLogger().severe("[AureliumSkills] An error occurred while trying to save skill data!");
		}
		isSaving = false;
	}

	public void startSaving() {
		new BukkitRunnable() {
			@Override
			public void run() {
				saveSkillData(true);
			}
		}.runTaskTimerAsynchronously(plugin, OptionL.getInt(Option.DATA_SAVE_PERIOD), OptionL.getInt(Option.DATA_SAVE_PERIOD));
	}
	
}
