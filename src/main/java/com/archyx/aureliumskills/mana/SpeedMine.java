package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.skills.PlayerSkill;
import com.archyx.aureliumskills.skills.SkillLoader;
import com.archyx.aureliumskills.skills.levelers.SorceryLeveler;
import com.archyx.aureliumskills.util.NumberUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class SpeedMine implements ManaAbility {

    private final AureliumSkills plugin;
    private final SorceryLeveler sorceryLeveler;

    public SpeedMine(AureliumSkills plugin) {
        this.plugin = plugin;
        this.sorceryLeveler = plugin.getSorceryLeveler();
    }

    @Override
    public AureliumSkills getPlugin() {
        return plugin;
    }

    @Override
    public MAbility getManaAbility() {
        return MAbility.SPEED_MINE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void activate(Player player) {
        PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
        if (playerSkill != null) {
            Locale locale = Lang.getLanguage(player);
            //Apply haste
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (plugin.getManaAbilityManager().getValue(MAbility.SPEED_MINE, playerSkill) * 20), 9, false, false), true);
            //Play sound
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            //Consume mana
            double manaConsumed = plugin.getManaAbilityManager().getManaCost(MAbility.SPEED_MINE, playerSkill);
            plugin.getManaManager().setMana(player.getUniqueId(), plugin.getManaManager().getMana(player.getUniqueId()) - manaConsumed);
            // Level Sorcery
            sorceryLeveler.level(player, manaConsumed);
            plugin.getAbilityManager().sendMessage(player, Lang.getMessage(ManaAbilityMessage.SPEED_MINE_START, locale).replace("{mana}", NumberUtil.format0(manaConsumed)));
        }
    }

    @Override
    public void stop(Player player) {
        plugin.getAbilityManager().sendMessage(player, Lang.getMessage(ManaAbilityMessage.SPEED_MINE_END, Lang.getLanguage(player)));
    }
}
