package com.archyx.aureliumskills.skills.levelers;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.abilities.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Source;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ExcavationLeveler extends SkillLeveler implements Listener{

	public ExcavationLeveler(AureliumSkills plugin) {
		super(plugin, Ability.EXCAVATOR);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	@SuppressWarnings("deprecation")
	public void onBlockBreak(BlockBreakEvent event) {
		if (OptionL.isEnabled(Skill.EXCAVATION)) {
			//Check cancelled
			if (OptionL.getBoolean(Option.EXCAVATION_CHECK_CANCELLED)) {
				if (event.isCancelled()) {
					return;
				}
			}
			if (blockXpGainLocation(event.getBlock().getLocation())) return;
			//Check block replace
			if (OptionL.getBoolean(Option.CHECK_BLOCK_REPLACE)) {
				if (event.getBlock().hasMetadata("skillsPlaced")) {
					return;
				}
			}
			Skill s = Skill.EXCAVATION;
			Block b = event.getBlock();
			Player p = event.getPlayer();
			Material mat = event.getBlock().getType();
			if (blockXpGainPlayer(p)) return;
			Leveler leveler = plugin.getLeveler();
			if (mat.equals(Material.SAND)) {
				if (XMaterial.isNewVersion()) {
					leveler.addXp(p, s, getXp(p, Source.SAND));
				}
				else {
					switch (b.getData()) {
						case 0:
							leveler.addXp(p, s, getXp(p, Source.SAND));
							break;
						case 1:
							leveler.addXp(p, s, getXp(p, Source.RED_SAND));
							break;
					}
				}
			}
			else if (mat.equals(XMaterial.RED_SAND.parseMaterial())) {
				leveler.addXp(p, s, getXp(p, Source.RED_SAND));
			}
			else if (mat.equals(XMaterial.GRASS_BLOCK.parseMaterial())) {
				leveler.addXp(p, s, getXp(p, Source.GRASS_BLOCK));
			}
			else if (mat.equals(Material.GRAVEL)) {
				leveler.addXp(p, s, getXp(p, Source.GRAVEL));
			}
			else if (mat.equals(Material.CLAY)) {
				leveler.addXp(p, s, getXp(p, Source.CLAY));
			}
			else if (mat.equals(Material.SOUL_SAND)) {
				leveler.addXp(p, s, getXp(p, Source.SOUL_SAND));
			}
			else if (mat.equals(XMaterial.MYCELIUM.parseMaterial())) {
				leveler.addXp(p, s, getXp(p, Source.MYCELIUM));
			}
			else if (mat.equals(XMaterial.SOUL_SOIL.parseMaterial())) {
				leveler.addXp(p, s, getXp(p, Source.SOUL_SOIL));
			}
			if (XMaterial.isNewVersion()) {
				if (mat.equals(Material.DIRT)) {
					leveler.addXp(p, s, getXp(p, Source.DIRT));
				}
				else if (mat.equals(XMaterial.COARSE_DIRT.parseMaterial())) {
					leveler.addXp(p, s, getXp(p, Source.COARSE_DIRT));
				}
				else if (mat.equals(XMaterial.PODZOL.parseMaterial())) {
					leveler.addXp(p, s, getXp(p, Source.PODZOL));
				}
			}
			else {
				if (mat.equals(Material.DIRT)) {
					switch (b.getData()) {
						case 0:
							leveler.addXp(p, s, getXp(p, Source.DIRT));
							break;
						case 1:
							leveler.addXp(p, s, getXp(p, Source.COARSE_DIRT));
							break;
						case 2:
							leveler.addXp(p, s, getXp(p, Source.PODZOL));
							break;
					}
				}
			}
			// Check custom blocks
			checkCustomBlocks(p, b, s);
		}
	}
}
