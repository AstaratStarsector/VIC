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
            addFluxPerCap = -200,
            addDisPerCap = 10,
            rangeReduction = 0.75f,
            shieldEffPenalty = 0.3f,
            shieldUpkeepBonus = 0.5f;

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

    private final Map<ShipAPI.HullSize, Float> dissipationBonus = new HashMap<>();
    {
        dissipationBonus.put(ShipAPI.HullSize.FRIGATE, 50f);
        dissipationBonus.put(ShipAPI.HullSize.DESTROYER, 100f);
        dissipationBonus.put(ShipAPI.HullSize.CRUISER, 150f);
        dissipationBonus.put(ShipAPI.HullSize.CAPITAL_SHIP, 250f);
    }

    private final Map<ShipAPI.HullSize, Float> capacityPenalty = new HashMap<>();
    {
        capacityPenalty.put(ShipAPI.HullSize.FRIGATE, 500f);
        capacityPenalty.put(ShipAPI.HullSize.DESTROYER, 1000f);
        capacityPenalty.put(ShipAPI.HullSize.CRUISER, 2000f);
        capacityPenalty.put(ShipAPI.HullSize.CAPITAL_SHIP, 5000f);
    }


    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);
    static {
        BLOCKED_HULLMODS.add("safetyoverrides");

    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getBallisticWeaponRangeBonus().modifyFlat(id , rangeBonus.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyFlat(id , rangeBonus.get(hullSize));
        stats.getWeaponRangeThreshold().modifyFlat(id,rangeForClamp.get(hullSize));
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, 1 - rangeReduction);
        stats.getShieldAbsorptionMult().modifyFlat(id, shieldEffPenalty);
        stats.getShieldUpkeepMult().modifyMult(id, shieldUpkeepBonus);

    }



    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        ShipAPI.HullSize hullSize = ship.getHullSize();
        stats.getFluxCapacity().modifyFlat(id, ship.getVariant().getNumFluxCapacitors() * addFluxPerCap - capacityPenalty.get(hullSize));
        stats.getFluxDissipation().modifyFlat(id, ship.getVariant().getNumFluxCapacitors() * addDisPerCap + dissipationBonus.get(hullSize));


        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        //boolean OK = true;
        if (!ship.getHullSpec().getHullId().startsWith("vic_")) return false;
        if (!ship.getVariant().getHullMods().contains("vic_shturmSolutionDummy")) return false;
        for (String Hmod : BLOCKED_HULLMODS){
            if (ship.getVariant().getHullMods().contains(Hmod)) return false;
        }
        return true;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.getHullSpec().getHullId().startsWith("vic_"))
            return "Must be installed on a VIC ship";
        if (!ship.getVariant().getHullMods().contains("vic_shturmSolutionDummy"))
            return "Must be installed on a VIC Shturm-Type ship";
        if (ship.getVariant().getHullMods().contains("safetyoverrides"))
            return "Incompatible with Safety Overrides";
        return null;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (hullSize == null) hullSize = ShipAPI.HullSize.FRIGATE;
        if (index == 0) return Math.round(addDisPerCap) + "";
        if (index == 1) return Math.round(capacityPenalty.get(hullSize)) + "";
        if (index == 2) return Math.round(dissipationBonus.get(hullSize)) + "";
        if (index == 3) return shieldEffPenalty + "";
        if (index == 4) return Math.round(shieldUpkeepBonus * 100f) + "%";
        if (index == 5) return Math.round(rangeBonus.get(hullSize)) + "";
        if (index == 6) return Math.round(rangeForClamp.get(hullSize)) + "";
        if (index == 7) return Math.round(rangeReduction * 100) + "%";
        if (index ==8) return Math.round(50) + "%";
        return null;
    }
}
