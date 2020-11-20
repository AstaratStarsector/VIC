package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_deathProtocol extends BaseHullMod {

    public final float
            dmgIncreas = 1.3f,
            dmgTaken = 1.25f,
            cloakCost = 1.25f;


    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponDamageMult().modifyMult(id, dmgIncreas);
        stats.getEnergyWeaponDamageMult().modifyMult(id, dmgIncreas);
        stats.getBeamWeaponDamageMult().modifyMult(id, dmgIncreas);
        stats.getMissileWeaponDamageMult().modifyMult(id, dmgIncreas);

        stats.getShieldDamageTakenMult().modifyMult(id, dmgTaken);
        stats.getArmorDamageTakenMult().modifyMult(id, dmgTaken);
        stats.getHullDamageTakenMult().modifyMult(id, dmgTaken);

        stats.getPhaseCloakActivationCostBonus().modifyMult(id, cloakCost);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, cloakCost);
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return (ship.getHullSpec().getHullId().startsWith("vic_"));
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round((dmgIncreas - 1) * 100) + "%";
        if (index == 1) return Math.round((dmgTaken - 1) * 100) + "%";
        if (index == 2) return Math.round((cloakCost - 1) * 100) + "%";
        return null;
    }
}




