package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class vic_vBombHmod extends BaseHullMod {

    public static String
            MOD_TO_CHECK = "vic_vBombHmod";

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (Global.getSector().getPlayerFleet() == null) return null;
        int HmodsAmount = 0;
        for (FleetMemberAPI shipToCheck : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (shipToCheck.getVariant().hasHullMod(MOD_TO_CHECK)) {
                HmodsAmount++;
            }
        }
        if (index == 0) return Math.round(1 * Math.pow( 0.9f, HmodsAmount - 1) * 100) + "%" ;
        return null;
    }
}




