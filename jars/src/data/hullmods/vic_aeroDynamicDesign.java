package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class vic_aeroDynamicDesign extends BaseHullMod {

	private final Map<HullSize, Float> mag = new HashMap<>();
	{
		mag.put(HullSize.FIGHTER, 10f);
		mag.put(HullSize.FRIGATE, 30f);
		mag.put(HullSize.DESTROYER, 60f);
		mag.put(HullSize.CRUISER, 90f);
		mag.put(HullSize.CAPITAL_SHIP, 150f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, mag.get(hullSize));
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + (mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + (mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + (mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}
}




