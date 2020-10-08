package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vic_tonedDownWeapon extends BaseHullMod {

	public final float RANGE_DECREASE = 0.5f;
	public final float FLUX_REDUCTION = 0.5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponRangeBonus().modifyMult(id, RANGE_DECREASE);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_REDUCTION);
	}
	
	 public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(100f - (RANGE_DECREASE * 100f)) + "%";
        if (index == 1) return Math.round(100f - (FLUX_REDUCTION *100f)) + "%";
        return null;
    }
}