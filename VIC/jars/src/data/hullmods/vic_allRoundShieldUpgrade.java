package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_allRoundShieldUpgrade extends BaseHullMod {

    public final float
            shieldEff = 13f,
            shieldUpkeep = 25f,
            shieldSpeed = 50f,
            shieldArc = 30f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - shieldEff * 0.01f);
        stats.getShieldUpkeepMult().modifyMult(id,1f - shieldUpkeep * 0.01f);
        stats.getShieldTurnRateMult().modifyPercent(id, shieldSpeed);
        stats.getShieldUnfoldRateMult().modifyPercent(id, shieldSpeed);
        stats.getShieldArcBonus().modifyFlat(id, shieldArc);
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return (ship.getHullSpec().getHullId().startsWith("vic_"));
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(shieldEff) + "%";
        if (index == 1) return Math.round(shieldUpkeep) + "%";
        if (index == 2) return Math.round(shieldSpeed) + "%";
        if (index == 3) return Math.round(shieldSpeed) + "%";
        if (index == 4) return Math.round(shieldArc) + "";
        return null;
    }
}




