package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class vic_convoyDrive extends BaseHullMod {

    public static final int ZERO_FLUX_BONUS = 35;
    public static final int RANGE_BONUS = 10;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BONUS);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
    }


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "" + ZERO_FLUX_BONUS;
        if (index == 1) return "" + RANGE_BONUS + "%";
        return null;
    }
}