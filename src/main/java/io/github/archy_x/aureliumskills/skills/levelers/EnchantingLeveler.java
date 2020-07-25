package io.github.archy_x.aureliumskills.skills.levelers;

import io.github.archy_x.aureliumskills.AureliumSkills;
import io.github.archy_x.aureliumskills.Options;
import io.github.archy_x.aureliumskills.skills.Skill;
import io.github.archy_x.aureliumskills.skills.Source;
import io.github.archy_x.aureliumskills.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class EnchantingLeveler implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEnchant(EnchantItemEvent event) {
		if (Options.isEnabled(Skill.ENCHANTING)) {
			if (event.isCancelled() == false) {
				//Checks if in blocked world
				if (AureliumSkills.worldManager.isInBlockedWorld(event.getEnchantBlock().getLocation())) {
					return;
				}
				//Checks if in blocked region
				if (AureliumSkills.worldGuardEnabled) {
					if (AureliumSkills.worldGuardSupport.isInBlockedRegion(event.getEnchantBlock().getLocation())) {
						return;
					}
				}
				Player p = event.getEnchanter();
				Material mat = event.getItem().getType();
				if (ItemUtils.isArmor(mat)) {
					Leveler.addXp(p, Skill.ENCHANTING, event.getExpLevelCost() * Options.getXpAmount(Source.ARMOR_PER_LEVEL));
				}
				else if (ItemUtils.isWeapon(mat)) {
					Leveler.addXp(p, Skill.ENCHANTING, event.getExpLevelCost() * Options.getXpAmount(Source.WEAPON_PER_LEVEL));
				}
				else if (mat.equals(Material.BOOK)) {
					Leveler.addXp(p, Skill.ENCHANTING, event.getExpLevelCost() * Options.getXpAmount(Source.BOOK_PER_LEVEL));
				}
				else {
					Leveler.addXp(p, Skill.ENCHANTING, event.getExpLevelCost() * Options.getXpAmount(Source.TOOL_PER_LEVEL));
				}
			}
		}
	}
	
}