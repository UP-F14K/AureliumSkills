package com.archyx.aureliumskills.requirement;

import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.modifier.ModifierType;
import com.archyx.aureliumskills.skills.PlayerSkill;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.SkillLoader;
import com.archyx.aureliumskills.util.ItemUtils;
import com.archyx.aureliumskills.util.LoreUtil;
import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.*;

public class Requirements {

    private final RequirementManager manager;

    public Requirements(RequirementManager manager) {
        this.manager = manager;
    }

    public Map<Skill, Integer> getRequirements(ModifierType type, ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        Map<Skill, Integer> requirements = new HashMap<>();
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        for (String key : compound.getKeys()) {
            try {
                Skill skill = Skill.valueOf(key.toUpperCase());
                Integer value = compound.getInteger(key);
                requirements.put(skill, value);
            }
            catch (Exception ignored) { }
        }
        return requirements;
    }

    public ItemStack addRequirement(ModifierType type, ItemStack item, Skill skill, int level) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        compound.setInteger(getName(skill), level);
        return nbtItem.getItem();
    }

    public ItemStack removeRequirement(ModifierType type, ItemStack item, Skill skill) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        for (String key : compound.getKeys()) {
            if (key.equals(getName(skill))) {
                compound.removeKey(key);
            }
        }
        ItemUtils.removeParentCompounds(compound);
        return nbtItem.getItem();
    }

    public ItemStack removeAllRequirements(ModifierType type, ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        compound.getKeys().forEach(compound::removeKey);
        ItemUtils.removeParentCompounds(compound);
        return nbtItem.getItem();
    }

    public boolean hasRequirement(ModifierType type, ItemStack item, Skill skill) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        for (String key : compound.getKeys()) {
            if (key.equals(getName(skill))) {
                return true;
            }
        }
        return false;
    }

    public ItemStack convertFromLegacy(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound oldRequirementsCompound = nbtItem.getCompound("skillRequirements");
        if (oldRequirementsCompound != null) {
            for (ModifierType type : ModifierType.values()) {
                NBTCompound oldTypeCompound = oldRequirementsCompound.getCompound(type.toString().toLowerCase(Locale.ENGLISH));
                if (oldTypeCompound != null) {
                    NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
                    for (String key : oldTypeCompound.getKeys()) {
                        compound.setInteger(StringUtils.capitalize(key), oldTypeCompound.getInteger(key));
                    }
                }
            }
        }
        nbtItem.removeKey("skillRequirements");
        return nbtItem.getItem();
    }

    public void addLore(ModifierType type, ItemStack item, Skill skill, int level, Locale locale) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String text = LoreUtil.replace(Lang.getMessage(CommandMessage.valueOf(type.name() + "_REQUIREMENT_ADD_LORE"), locale), "{skill}", skill.getDisplayName(locale), "{level}", String.valueOf(level));
            List<String> lore;
            if (meta.hasLore()) lore = meta.getLore();
            else lore = new ArrayList<>();
            if (lore != null) {
                lore.add(text);
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
    }

    public void removeLore(ItemStack item, Skill skill) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (line.contains("Requires") && line.contains(StringUtils.capitalize(skill.name().toLowerCase(Locale.ENGLISH)))) {
                        lore.remove(line);
                    }
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
    }

    @SuppressWarnings("deprecation")
    public boolean meetsRequirements(ModifierType type, ItemStack item, Player player) {
        PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
        if (playerSkill == null) return false;
        // Check global requirements
        for (GlobalRequirement global : manager.getGlobalRequirementsType(type)) {
            if (XMaterial.isNewVersion()) {
                if (global.getMaterial().parseMaterial() == item.getType()) {
                    for (Map.Entry<Skill, Integer> entry : global.getRequirements().entrySet()) {
                        if (playerSkill.getSkillLevel(entry.getKey()) < entry.getValue()) {
                            return false;
                        }
                    }
                }
            }
            else {
                if (global.getMaterial().parseMaterial() == item.getType()) {
                    if (!ItemUtils.isDurable(item.getType())) {
                        MaterialData materialData = item.getData();
                        if (materialData != null) {
                            if (item.getDurability() == global.getMaterial().getData()) {
                                for (Map.Entry<Skill, Integer> entry : global.getRequirements().entrySet()) {
                                    if (playerSkill.getSkillLevel(entry.getKey()) < entry.getValue()) {
                                        return false;
                                    }
                                }
                            }
                        }
                    } else {
                        for (Map.Entry<Skill, Integer> entry : global.getRequirements().entrySet()) {
                            if (playerSkill.getSkillLevel(entry.getKey()) < entry.getValue()) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<Skill, Integer> entry : getRequirements(type, item).entrySet()) {
            if (playerSkill.getSkillLevel(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;

    }

    private String getName(Skill skill) {
        return StringUtils.capitalize(skill.name().toLowerCase(Locale.ENGLISH));
    }

}
