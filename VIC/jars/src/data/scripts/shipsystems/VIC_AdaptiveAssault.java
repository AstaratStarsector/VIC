package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class VIC_AdaptiveAssault extends BaseShipSystemScript {

    float damageBonus = 75f;
    float minBonus = 25f;

    private final Map<WeaponAPI.WeaponSize, Integer> pointsPerSize = new HashMap<>();

    {
        pointsPerSize.put(WeaponAPI.WeaponSize.SMALL, 1);
        pointsPerSize.put(WeaponAPI.WeaponSize.MEDIUM, 2);
        pointsPerSize.put(WeaponAPI.WeaponSize.LARGE, 4);
    }
    Vector2f split = new Vector2f();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        Vector2f split = WeaponsSplit(ship);

        stats.getEnergyWeaponDamageMult().modifyPercent(id, (((1 - split.x) * (damageBonus - minBonus)) + minBonus) * effectLevel);
        stats.getBallisticWeaponDamageMult().modifyPercent(id, ((split.y * (damageBonus - minBonus)) + minBonus) * effectLevel);

    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);

    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {

        if (index == 0)
            return new StatusData("+" + Math.round((split.y * (damageBonus - minBonus)) + minBonus) + "% ballistic weapon damage", false);
        if (index == 1)
            return new StatusData("+" + Math.round(((1 - split.x) * (damageBonus - minBonus)) + minBonus) + "% energy weapon damage", false);
        return null;

    }

    public Vector2f WeaponsSplit(ShipAPI ship) {
        float balScore = 0f;
        float energyScore = 0f;
        float totalScore = 0f;
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
        if (totalScore == 0){
            return new Vector2f(0.5f, 0.5f);
        }
        float balPrec = balScore / totalScore;
        float energyPrec = energyScore / totalScore;
        return new Vector2f(balPrec, energyPrec);
    }
}
