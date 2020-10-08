package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_hellStorm extends BaseHullMod {

    public final float ROF_BONUS = 1f;
    public final float ENERGY_WEAPON_DAMAGE_BONUS = 1f;
    public final float ENERGY_WEAPON_FLUX_INCREASE = 1f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().modifyMult(id, 1f + ROF_BONUS);
        stats.getEnergyWeaponDamageMult().modifyMult(id, 1f + ENERGY_WEAPON_DAMAGE_BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f + ENERGY_WEAPON_FLUX_INCREASE);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(ROF_BONUS * 100f) + "%";
        if (index == 1) return Math.round(ENERGY_WEAPON_DAMAGE_BONUS * 100f) + "%";
        if (index == 2) return Math.round(ENERGY_WEAPON_FLUX_INCREASE * 100f) + "%";
        return null;
    }
}