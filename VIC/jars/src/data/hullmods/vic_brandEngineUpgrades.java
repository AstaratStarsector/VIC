package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class vic_brandEngineUpgrades extends BaseHullMod {

    private final float burnBoost = 1f;
    private final float FUEL_EFFICIENCY = 0.9f;
    private final float PROFILE_PENALTY = 25f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (hullSize == HullSize.CAPITAL_SHIP)
            stats.getMaxBurnLevel().modifyFlat(id, burnBoost);
        stats.getFuelUseMod().modifyMult(id, FUEL_EFFICIENCY);
        stats.getSensorProfile().modifyPercent(id,  PROFILE_PENALTY);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getVariant().addMod("vic_geneticmodifications");
    }

    public String getDescriptionParam ( int index, HullSize hullSize){
        if (index == 0) return Math.round(burnBoost) + "";
        if (index == 1) return Math.round(100f - (FUEL_EFFICIENCY * 100f)) + "%";
        if (index == 2) return PROFILE_PENALTY + "%";
        return null;
    }

}
