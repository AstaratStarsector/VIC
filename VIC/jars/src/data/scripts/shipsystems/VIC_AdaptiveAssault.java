package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import java.util.HashMap;
import java.util.Map;

public class VIC_AdaptiveAssault extends BaseShipSystemScript {

    float damageBonus = 75f;
    float minBonus = 25f;

    private final Map<WeaponAPI.WeaponSize, Integer> pointsPerSize = new HashMap<>();
    public float split = 0;

    {
        pointsPerSize.put(WeaponAPI.WeaponSize.SMALL, 1);
        pointsPerSize.put(WeaponAPI.WeaponSize.MEDIUM, 2);
        pointsPerSize.put(WeaponAPI.WeaponSize.LARGE, 4);
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        split = WeaponsSplit(ship);

        stats.getEnergyWeaponDamageMult().modifyPercent(id, (((1 - split) * (damageBonus - minBonus)) + minBonus) * effectLevel);
        stats.getBallisticWeaponDamageMult().modifyPercent(id, ((split * (damageBonus - minBonus)) + minBonus) * effectLevel);

    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);

    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {

        if (index == 0)
            return new StatusData("+" + Math.round((split * (damageBonus - minBonus)) + minBonus) + "% ballistic weapon damage", false);
        if (index == 1)
            return new StatusData("+" + Math.round(((1 - split) * (damageBonus - minBonus)) + minBonus) + "% energy weapon damage", false);
        return null;

    }

    public float WeaponsSplit(ShipAPI ship) {
        int balScore = 0;
        int energyScore = 0;
        WeaponAPI.WeaponType type;
        WeaponSpecAPI weapon;
        for (String weaponSlot : ship.getVariant().getFittedWeaponSlots()) {
            weapon = ship.getVariant().getWeaponSpec(weaponSlot);
            type = weapon.getType();
            if (type == WeaponAPI.WeaponType.BALLISTIC)
                energyScore += pointsPerSize.get(weapon.getSize());
            else if (type == WeaponAPI.WeaponType.ENERGY)
                balScore += pointsPerSize.get(weapon.getSize());
        }
        float totalScore = balScore + energyScore;
        float balPrec = 0.5f;
        if (totalScore != 0) balPrec = balScore / totalScore;
        return balPrec;
    }
}
