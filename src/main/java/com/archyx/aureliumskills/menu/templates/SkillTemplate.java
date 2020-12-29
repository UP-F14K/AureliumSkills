package com.archyx.aureliumskills.menu.templates;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.menu.MenuLoader;
import com.archyx.aureliumskills.skills.PlayerSkill;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.LoreUtil;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SkillTemplate extends SkillInfoItem implements ConfigurableTemplate {

    private final TemplateType TYPE = TemplateType.SKILL;

    private final Map<Skill, SlotPos> positions = new HashMap<>();
    private final Map<Skill, ItemStack> baseItems = new HashMap<>();
    private String displayName;
    private List<String> lore;
    private Map<Integer, Set<String>> lorePlaceholders;
    private final String[] definedPlaceholders = new String[] {"skill_desc", "primary_stat", "secondary_stat", "ability_levels", "mana_ability", "level", "progress_to_level", "max_level", "skill_click"};
    
    public SkillTemplate(AureliumSkills plugin) {
        super(plugin);
    }
    
    @Override
    public TemplateType getType() {
        return TYPE;
    }

    @Override
    public void load(ConfigurationSection config) {
        try {
            for (String posInput : config.getStringList("pos")) {
                String[] splitInput = posInput.split(" ");
                Skill skill = Skill.valueOf(splitInput[0]);
                int row = Integer.parseInt(splitInput[1]);
                int column = Integer.parseInt(splitInput[2]);
                positions.put(skill, SlotPos.of(row, column));
            }
            // Load base items
            for (String materialInput : config.getStringList("material")) {
                String[] splitInput = materialInput.split(" ", 2);
                Skill skill;
                try {
                    skill = Skill.valueOf(splitInput[0].toUpperCase());
                }
                catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[AureliumSkills] Error while loading SKILL template, " + splitInput[0].toUpperCase() + " is not a valid skill! Using FARMING as a default");
                    skill = Skill.FARMING;
                }
                baseItems.put(skill, MenuLoader.parseItem(splitInput[1]));
            }
            displayName = LoreUtil.replace(Objects.requireNonNull(config.getString("display_name")),"&", "§");
            // Load lore
            List<String> lore = new ArrayList<>();
            Map<Integer, Set<String>> lorePlaceholders = new HashMap<>();
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
            this.lore = lore;
            this.lorePlaceholders = lorePlaceholders;
        }
        catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("[AureliumSkills] Error parsing template " + TYPE.toString() + ", check error above for details!");
        }
    }

    public ItemStack getItem(Skill skill, PlayerSkill playerSkill, Locale locale) {
        return getItem(baseItems.get(skill), skill, playerSkill, locale, displayName, lore, lorePlaceholders);
    }

    public SlotPos getPosition(Skill skill) {
        return positions.get(skill);
    }

}
