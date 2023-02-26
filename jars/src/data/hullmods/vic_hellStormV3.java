package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_hellStormV3 extends BaseHullMod {

    final float bonus = 1.5f,
                OPIncrease = 2f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponDamageMult().modifyMult(id, bonus);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, bonus);
        stats.getBallisticWeaponDamageMult().modifyMult(id, bonus);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, bonus);
        stats.getMissileAmmoBonus().modifyMult(id, bonus);
        stats.getMissileAmmoRegenMult().modifyMult(id, bonus);
        stats.getDynamic().getMod("small_missile_mod").modifyMult(id, OPIncrease);
        stats.getDynamic().getMod("medium_missile_mod").modifyMult(id, OPIncrease);
        stats.getDynamic().getMod("large_missile_mod").modifyMult(id, OPIncrease);
        stats.getDynamic().getMod("small_ballistic_mod").modifyMult(id, OPIncrease);
        stats.getDynamic().getMod("medium_ballistic_mod").modifyMult(id, OPIncrease);
        stats.getDynamic().getMod("large_ballistic_mod").modifyMult(id, OPIncrease);
        stats.getDynamic().getMod("small_energy_mod").modifyMult(id, OPIncrease);
        stats.getDynamic().getMod("medium_energy_mod").modifyMult(id, OPIncrease);
        stats.getDynamic().getMod("large_energy_mod").modifyMult(id, OPIncrease);
    }


    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) ((bonus - 1) * 100) + "%";
        if (index == 1) return "" + (int) ((bonus - 1) * 100) + "%";
        if (index == 2) return "" + (int) ((OPIncrease - 1) * 100) + "%";
        if (index == 3) return "30";
        if (index == 4) return "5";
        if (index == 5) return "+10%";
        if (index == 6) return "+5%";
        if (index == 7) return "+5%";
        return null;
    }

    public boolean affectsOPCosts() {
        return true;
    }
}