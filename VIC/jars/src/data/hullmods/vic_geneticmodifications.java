package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_geneticmodifications extends BaseHullMod {

    private final float timeAccellBonus = 1.1f;
    private final float accellerationBonus = 20f;
    private final float damageTaken = 10f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getTimeMult().modifyMult(id, timeAccellBonus);
        stats.getAcceleration().modifyPercent(id, accellerationBonus);
        stats.getTurnAcceleration().modifyPercent(id, accellerationBonus);
        stats.getShieldDamageTakenMult().modifyPercent(id, damageTaken);
        stats.getArmorDamageTakenMult().modifyPercent(id, damageTaken);
        stats.getHullDamageTakenMult().modifyPercent(id, damageTaken);

    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        String id = "vic_geneticModifications";

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / timeAccellBonus);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(timeAccellBonus*100f-100f) + "%";
        if (index == 1) return Math.round(accellerationBonus)  + "%";
        if (index == 2) return Math.round(damageTaken)  + "%";
        return null;
    }

}