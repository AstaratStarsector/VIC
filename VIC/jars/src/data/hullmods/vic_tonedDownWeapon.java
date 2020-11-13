package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_tonedDownWeapon extends BaseHullMod {

    public final float rangeMult = 0.65f;
    public final float fluxMult = 0.75f;
    public final float timeMult = 1.5f;
    public final float phaseCD = 0.75f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponRangeBonus().modifyMult(id, rangeMult);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, fluxMult);
        stats.getTimeMult().modifyMult(id, timeMult);
        stats.getPhaseCloakCooldownBonus().modifyMult(id, phaseCD);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round((timeMult - 1) * 100f) + "%";
        if (index == 1) return Math.round(100f - (fluxMult * 100f)) + "%";
        if (index == 2) return Math.round(100f - (fluxMult * 100f)) + "%";
        if (index == 3) return Math.round(100f - (rangeMult * 100f)) + "%";
        return null;
    }

}