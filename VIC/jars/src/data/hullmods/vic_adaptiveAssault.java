package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import java.util.HashMap;
import java.util.Map;

public class vic_adaptiveAssault extends BaseHullMod {

    private final Map<WeaponAPI.WeaponSize, Integer> pointsPerSize = new HashMap<>();

    {
        pointsPerSize.put(WeaponAPI.WeaponSize.SMALL, 1);
        pointsPerSize.put(WeaponAPI.WeaponSize.MEDIUM, 2);
        pointsPerSize.put(WeaponAPI.WeaponSize.LARGE, 4);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (ship == null) return null;
        int balScore = 0;
        int energyScore = 0;
        WeaponAPI.WeaponType type;
        WeaponSpecAPI weapon;
        for (String weaponSlot : ship.getVariant().getFittedWeaponSlots()) {
            weapon = ship.getVariant().getWeaponSpec(weaponSlot);
            type = weapon.getType();
            if (type == WeaponAPI.WeaponType.BALLISTIC)
                balScore += pointsPerSize.get(weapon.getSize());
            else if (type == WeaponAPI.WeaponType.ENERGY)
                energyScore += pointsPerSize.get(weapon.getSize());
        }
        float totalScore = balScore + energyScore;
        float balPrec = 0.5f;
        if (totalScore != 0) balPrec = balScore / totalScore;
        if (index == 0) return Math.round((balPrec * 0.5f + 0.5f) * 100f) + "%";
        if (index == 1) return Math.round(((1 / (balPrec * 0.5f + 1.5f)) - 1) * 100f) + "%";
        if (index == 2) return Math.round(((1 - balPrec) * 0.25f + 0.25f) * 100f) + "%";
        return null;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);
    }
}