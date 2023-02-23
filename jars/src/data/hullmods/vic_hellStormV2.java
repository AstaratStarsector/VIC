package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_hellStormV2 extends BaseHullMod {

    public final float ROF_BONUS = 2f;
    public final float ENERGY_WEAPON_DAMAGE_BONUS = 2f;
    public final float ENERGY_WEAPON_FLUX_INCREASE = 2f;
    public final float MISSILE_AMMO_BONUS = 2f;
    public final float WEAPON_OP_INCREASE_MULT = 2f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().modifyMult(id, ROF_BONUS);
        stats.getBallisticAmmoRegenMult().modifyMult(id, ROF_BONUS);
        stats.getMissileAmmoRegenMult().modifyMult(id, MISSILE_AMMO_BONUS);
        stats.getEnergyWeaponDamageMult().modifyMult(id, ENERGY_WEAPON_DAMAGE_BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, ENERGY_WEAPON_FLUX_INCREASE);
        stats.getMissileAmmoBonus().modifyMult(id, MISSILE_AMMO_BONUS);
        stats.getDynamic().getMod("small_missile_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
        stats.getDynamic().getMod("medium_missile_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
        stats.getDynamic().getMod("large_missile_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
        stats.getDynamic().getMod("small_ballistic_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
        stats.getDynamic().getMod("medium_ballistic_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
        stats.getDynamic().getMod("large_ballistic_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
        stats.getDynamic().getMod("small_energy_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
        stats.getDynamic().getMod("medium_energy_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
        stats.getDynamic().getMod("large_energy_mod").modifyMult(id, WEAPON_OP_INCREASE_MULT);
    }


    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) ((ROF_BONUS - 1) * 100) + "%";
        if (index == 1) return "" + (int) ((ENERGY_WEAPON_DAMAGE_BONUS - 1) * 100) + "%";
        if (index == 2) return "" + (int) ((MISSILE_AMMO_BONUS - 1) * 100) + "%";
        if (index == 3) return "" + (int) ((WEAPON_OP_INCREASE_MULT - 1) * 100) + "%";
        return null;
    }

    public boolean affectsOPCosts() {
        return true;
    }
}