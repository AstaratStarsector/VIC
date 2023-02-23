package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_brandEngineUpgrades extends BaseHullMod {

    private final float burnBoost = 1f;
    private final float FUEL_EFFICIENCY = 0.9f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (hullSize == HullSize.CAPITAL_SHIP)
            stats.getMaxBurnLevel().modifyFlat(id, burnBoost);
        else {
            stats.getFuelUseMod().modifyMult(id, FUEL_EFFICIENCY);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(burnBoost) + "";
        if (index == 1) return Math.round(100f - (FUEL_EFFICIENCY * 100f)) + "%";
        if (index == 2) return 0.25 + "%";
        if (index == 3) return 0.5 + "%";
        if (index == 4) return 0.75 + "%";
        if (index == 5) return 1.5 + "%";
        return null;
    }
}
