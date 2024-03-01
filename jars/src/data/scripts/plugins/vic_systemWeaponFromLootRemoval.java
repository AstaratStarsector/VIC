package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.combat.WeaponAPI;

public class vic_systemWeaponFromLootRemoval extends BaseCampaignEventListener implements ShowLootListener {


    public vic_systemWeaponFromLootRemoval(boolean permaRegister) {
        super(permaRegister);
    }

    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        for (CargoAPI.CargoItemQuantity<String> weapon : loot.getWeapons()) {
            if (Global.getSettings().getWeaponSpec(weapon.getItem()).getAIHints().contains(WeaponAPI.AIHints.SYSTEM)){
                loot.removeWeapons(weapon.getItem(), weapon.getCount());
                Global.getLogger(vic_systemWeaponFromLootRemoval.class).info("removed " + weapon.getItem());
            }
        }
    }

    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        for (CargoAPI.CargoItemQuantity<String> weapon : loot.getWeapons()) {
            if (Global.getSettings().getWeaponSpec(weapon.getItem()).getAIHints().contains(WeaponAPI.AIHints.SYSTEM)){
                loot.removeWeapons(weapon.getItem(), weapon.getCount());
                Global.getLogger(vic_systemWeaponFromLootRemoval.class).info("removed " + weapon.getItem());
            }
        }
    }
}
