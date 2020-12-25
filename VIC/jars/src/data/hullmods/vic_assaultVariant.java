package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class vic_assaultVariant extends BaseHullMod {

    private final float
            addFluxPerCap = -100,
            addDisPerCap = 5,
            rangeReduction = 0.75f;

    private final Map<ShipAPI.HullSize, Float> rangeForClamp = new HashMap<>();
    {
        rangeForClamp.put(ShipAPI.HullSize.FRIGATE, 500f);
        rangeForClamp.put(ShipAPI.HullSize.DESTROYER, 600f);
        rangeForClamp.put(ShipAPI.HullSize.CRUISER, 800f);
        rangeForClamp.put(ShipAPI.HullSize.CAPITAL_SHIP, 1000f);
    }
    
    private final Map<ShipAPI.HullSize, Float> rangeBonus = new HashMap<>();
    {
        rangeBonus.put(ShipAPI.HullSize.FRIGATE, 100f);
        rangeBonus.put(ShipAPI.HullSize.DESTROYER, 150f);
        rangeBonus.put(ShipAPI.HullSize.CRUISER, 200f);
        rangeBonus.put(ShipAPI.HullSize.CAPITAL_SHIP, 300f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getBallisticWeaponRangeBonus().modifyFlat(id , rangeBonus.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyFlat(id , rangeBonus.get(hullSize));
        stats.getWeaponRangeThreshold().modifyFlat(id,rangeForClamp.get(hullSize));
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, 1 - rangeReduction);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        stats.getFluxCapacity().modifyFlat(id, ship.getVariant().getNumFluxCapacitors() * addFluxPerCap);
        stats.getFluxDissipation().modifyFlat(id, ship.getVariant().getNumFluxCapacitors() * addDisPerCap);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (hullSize == null) hullSize = ShipAPI.HullSize.FRIGATE;
        if (index == 0) return Math.round(-addFluxPerCap) + "";
        if (index == 1) return Math.round(addDisPerCap) + "";
        if (index == 2) return Math.round(rangeBonus.get(hullSize)) + "";
        if (index == 3) return Math.round(rangeForClamp.get(hullSize)) + "";
        if (index == 4) return Math.round(rangeReduction * 100) + "%";
        return null;
    }
}
