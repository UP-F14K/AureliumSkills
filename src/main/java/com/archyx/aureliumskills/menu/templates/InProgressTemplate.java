package com.archyx.aureliumskills.menu.templates;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menu.MenuLoader;
import com.archyx.aureliumskills.skills.PlayerSkill;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.ItemUtils;
import com.archyx.aureliumskills.util.LoreUtil;
import com.archyx.aureliumskills.util.NumberUtil;
import com.archyx.aureliumskills.util.RomanNumber;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InProgressTemplate extends ProgressLevelItem implements ConfigurableTemplate {

    private final TemplateType TYPE = TemplateType.IN_PROGRESS;

    private ItemStack baseItem;
    private String displayName;
    private List<String> lore;
    private Map<Integer, Set<String>> lorePlaceholders;
    private final String[] definedPlaceholders = new String[] {"level_number", "rewards", "ability", "mana_ability", "progress", "in_progress"};

    public InProgressTemplate(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public TemplateType getType() {
        return TYPE;
    }

    @Override
    public void load(ConfigurationSection config) {
        try {
            baseItem = MenuLoader.parseItem(Objects.requireNonNull(config.getString("material")));
            displayName = LoreUtil.replace(Objects.requireNonNull(config.getString("display_name")),"&", "§");
            lore = new ArrayList<>();
            lorePlaceholders = new HashMap<>();
            int lineNum = 0;
            for (String line : config.getStringList("lore")) {
                Set<String> linePlaceholders = new HashSet<>();
                lore.add(LoreUtil.replace(line,"&", "§"));
                // Find lore placeholders
                for (String placeholder : definedPlaceholders) {
                    if (line.contains("{" + placeholder + "}")) {
                        linePlaceholders.add(placeholder);
                    }
                }
                lorePlaceholders.put(lineNum, linePlaceholders);
                lineNum++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("[AureliumSkills] Error parsing template " + TYPE.toString() + ", check error above for details!");
        }
    }

    public ItemStack getItem(Skill skill, PlayerSkill playerSkill, int level, Locale locale) {
        ItemStack item = baseItem.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LoreUtil.replace(displayName,"{level_in_progress}", LoreUtil.replace(Lang.getMessage(MenuMessage.LEVEL_IN_PROGRESS, locale),"{level}", RomanNumber.toRoman(level))));
            List<String> builtLore = new ArrayList<>();
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                Set<String> placeholders = lorePlaceholders.get(i);
                for (String placeholder : placeholders) {
                    switch (placeholder) {
                        case "level_number":
                            line = LoreUtil.replace(line,"{level_number}", LoreUtil.replace(Lang.getMessage(MenuMessage.LEVEL_NUMBER, locale),"{level}", String.valueOf(level)));
                            break;
                        case "rewards":
                            line = LoreUtil.replace(line,"{rewards}", getRewardsLore(skill, level, locale));
                            break;
                        case "ability":
                            line = LoreUtil.replace(line,"{ability}", getAbilityLore(skill, level, locale));
                            break;
                        case "mana_ability":
                            line = LoreUtil.replace(line, "{mana_ability}", getManaAbilityLore(skill, level, locale));
                            break;
                        case "progress":
                            double currentXp = playerSkill.getXp(skill);
                            double xpToNext = getPlugin().getLeveler().getLevelRequirements().get(level - 2);
                            line = LoreUtil.replace(line,"{progress}", LoreUtil.replace(Lang.getMessage(MenuMessage.PROGRESS, locale)
                                    ,"{percent}", NumberUtil.format2(currentXp / xpToNext * 100)
                                    ,"{current_xp}", NumberUtil.format2(currentXp)
                                    ,"{level_xp}", String.valueOf((int) xpToNext)));
                            break;
                        case "in_progress":
                            line = LoreUtil.replace(line,"{in_progress}", Lang.getMessage(MenuMessage.IN_PROGRESS, locale));
                            break;
                    }
                }
                builtLore.add(line);
            }
            meta.setLore(ItemUtils.formatLore(builtLore));
            item.setItemMeta(meta);
        }
        return item;
    }

}
