package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import java.util.*;

public class vic_loaderOverdrive extends BaseHullMod {

    private final float rangeBonus = -15f;
    private final float ROFBonus = 30f;


    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, -rangeBonus);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, -rangeBonus);

        stats.getBallisticRoFMult().modifyPercent(id, ROFBonus);
        stats.getEnergyRoFMult().modifyPercent(id, ROFBonus);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(ROFBonus) + "%";
        if (index == 1) return Math.round(-rangeBonus) + "%";
        return null;
    }
}




