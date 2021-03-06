package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.abilities.DefenseAbilities;
import com.archyx.aureliumskills.api.event.ManaRegenerateEvent;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.SkillLoader;
import com.archyx.aureliumskills.stats.PlayerStat;
import com.archyx.aureliumskills.stats.Stat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManaManager implements Listener {

    private final Map<UUID, Double> mana;
    private final AureliumSkills plugin;
    private final DefenseAbilities defenseAbilities;

    public ManaManager(AureliumSkills plugin, DefenseAbilities defenseAbilities) {
        this.plugin = plugin;
        this.defenseAbilities = defenseAbilities;
        mana = new HashMap<>();
    }

    /**
     * Start regenerating Mana
     */
    public void startRegen() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : mana.keySet()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(id);
                    if (player.isOnline()) {
                        if (SkillLoader.playerStats.containsKey(id)) {
                            PlayerStat stat = SkillLoader.playerStats.get(id);
                            double originalMana = mana.get(id);
                            double maxMana = OptionL.getDouble(Option.BASE_MANA) + 2 * stat.getStatLevel(Stat.WISDOM);
                            if (originalMana < maxMana) {
                                Player onlinePlayer = (Player) player;
                                if (!defenseAbilities.getAbsorptionActivated().contains(player)) { // Make sure absorption is not activated
                                    double regen = OptionL.getDouble(Option.REGENERATION_BASE_MANA_REGEN) + stat.getStatLevel(Stat.REGENERATION) * OptionL.getDouble(Option.REGENERATION_MANA_MODIFIER);
                                    double finalRegen = Math.min(originalMana + regen, maxMana) - originalMana;
                                    //Call Event
                                    ManaRegenerateEvent event = new ManaRegenerateEvent(player.getPlayer(), finalRegen);
                                    Bukkit.getPluginManager().callEvent(event);
                                    if (!event.isCancelled()) {
                                        mana.put(id, originalMana + event.getAmount());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public double getMana(UUID id) {
        if (mana.containsKey(id)) {
            return mana.get(id);
        }
        else {
            mana.put(id, OptionL.getDouble(Option.BASE_MANA));
            return OptionL.getInt(Option.BASE_MANA);
        }
    }

    public double getMaxMana(UUID id) {
        PlayerStat playerStat = SkillLoader.playerStats.get(id);
        if (playerStat != null) {
            return OptionL.getDouble(Option.BASE_MANA) + (OptionL.getDouble(Option.WISDOM_MAX_MANA_PER_WISDOM) * playerStat.getStatLevel(Stat.WISDOM));
        }
        else {
            SkillLoader.playerStats.put(id, new PlayerStat(id, plugin));
            return OptionL.getDouble(Option.BASE_MANA);
        }
    }

    public void setMana(UUID id, double amount) {
        if (OptionL.getBoolean(Option.WISDOM_ALLOW_OVER_MAX_MANA)) {
            mana.put(id, amount);
        } else {
            mana.put(id, Math.min(amount, getMaxMana(id)));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (!mana.containsKey(id)) {
            if (SkillLoader.playerStats.containsKey(id)) {
                mana.put(event.getPlayer().getUniqueId(), OptionL.getDouble(Option.BASE_MANA) + 2 * SkillLoader.playerStats.get(id).getStatLevel(Stat.WISDOM));
            }
        }
    }
}
