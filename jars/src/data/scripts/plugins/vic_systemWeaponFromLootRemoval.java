package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class vic_systemWeaponFromLootRemoval extends BaseCampaignEventListener {


    public vic_systemWeaponFromLootRemoval(boolean permaRegister) {
        super(permaRegister);
    }

    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        for (CargoAPI.CargoItemQuantity<String> weapon : loot.getWeapons()) {
            if (Global.getSettings().getWeaponSpec(weapon.getItem()).getAIHints().contains(WeaponAPI.AIHints.SYSTEM)){
                loot.removeWeapons(weapon.getItem(), weapon.getCount());
            }
        }
    }

}
