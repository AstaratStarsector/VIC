package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_geneticModifications extends BaseHullMod {

    private final float timeAcellBonus = 1.1f;
    private final float acellBonus = 50f;
    private final float damageTaken = 5f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getTimeMult().modifyMult(id, timeAcellBonus);
        stats.getAcceleration().modifyPercent(id, acellBonus);
        stats.getTurnAcceleration().modifyPercent(id, acellBonus);
        stats.getShieldDamageTakenMult().modifyPercent(id, damageTaken);
        stats.getArmorDamageTakenMult().modifyPercent(id, damageTaken);
        stats.getHullDamageTakenMult().modifyPercent(id, damageTaken);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return  Math.round(5f) + "%";
        if (index == 1) return  Math.round(10f) + "%";
        if (index == 2) return  Math.round(15f) + "%";
        if (index == 3) return  Math.round(10f) + "%";
        if (index == 4) return  Math.round(10f) + "%";
        if (index == 5) return  "Deployment Points";
        if (index == 6) return  "1500 su";
        if (index == 7) return  "Deployment Points";
        if (index == 8) return  Math.round(5f) + "%";
        return null;
    }
}