package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class vic_adaptiveAssault extends BaseHullMod {

    private final Map<WeaponAPI.WeaponSize, Integer> pointsPerSize = new HashMap<>();

    {
        pointsPerSize.put(WeaponAPI.WeaponSize.SMALL, 1);
        pointsPerSize.put(WeaponAPI.WeaponSize.MEDIUM, 2);
        pointsPerSize.put(WeaponAPI.WeaponSize.LARGE, 4);
    }

    float damageBonus = 75f;
    float minBonus = 25f;

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (ship == null) return null;
        float balScore = 0f;
        float energyScore = 0f;
        float totalScore = 0f;
        float balPrec = 0.5f;
        float energyPrec = 0.5f;
        WeaponAPI.WeaponType type;
        WeaponSpecAPI weapon;
        for (String weaponSlot : ship.getVariant().getFittedWeaponSlots()) {
            weapon = ship.getVariant().getWeaponSpec(weaponSlot);
            type = weapon.getType();
            totalScore += pointsPerSize.get(weapon.getSize());
            if (type == WeaponAPI.WeaponType.BALLISTIC)
                energyScore += pointsPerSize.get(weapon.getSize());
            else if (type == WeaponAPI.WeaponType.ENERGY)
                balScore += pointsPerSize.get(weapon.getSize());
        }
        if (totalScore != 0){
            balPrec = balScore / totalScore;
            energyPrec = energyScore / totalScore;
        }
        if (index == 0) return (Math.round(balPrec  * (damageBonus - minBonus) + minBonus)) + "%";
        if (index == 1) return (Math.round(energyPrec * (damageBonus - minBonus) + minBonus)) + "%";
        return null;
    }
}