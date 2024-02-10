package data.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import org.lazywizard.lazylib.MathUtils;

public class vic_raumRangeUpgrade extends BaseHullMod {



    public final float
            weaponRangeMult = 20f;


    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, weaponRangeMult);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, weaponRangeMult);
        stats.getMissileWeaponRangeBonus().modifyPercent(id, weaponRangeMult);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(weaponRangeMult) + "%";
        return null;
    }

}



