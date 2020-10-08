package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.Global;

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

    public void advanceInCombat(ShipAPI ship, float amount) {

        String id = "vic_geneticModifications";

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / timeAcellBonus);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }
    }
}