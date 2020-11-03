package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_geneticModifications extends BaseHullMod {

    private final float timeAcellBonus = 1.1f;
    private final float acellBonus = 50f;
    private final float damageTaken = 10f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getTimeMult().modifyMult(id, timeAcellBonus);
        stats.getAcceleration().modifyPercent(id, acellBonus);
        stats.getTurnAcceleration().modifyPercent(id, acellBonus);
        stats.getShieldDamageTakenMult().modifyPercent(id, damageTaken);
        stats.getArmorDamageTakenMult().modifyPercent(id, damageTaken);
        stats.getHullDamageTakenMult().modifyPercent(id, damageTaken);

    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round((timeAcellBonus - 1) * 100) + "";
        if (index == 1) return Math.round(acellBonus) + "%";
        if (index == 2) return damageTaken + "%";
        return null;
    }
}