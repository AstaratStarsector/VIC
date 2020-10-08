package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class vic_aeroDynamicDesign extends BaseHullMod {

    //list of values with "names" of the values in our case ships sizes put values there
	private final Map<HullSize, Float> mag = new HashMap<>();
	{
		mag.put(HullSize.FRIGATE, 10f);
		mag.put(HullSize.DESTROYER, 20f);
		mag.put(HullSize.CRUISER, 30f);
		mag.put(HullSize.CAPITAL_SHIP, 40f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	    //add bonus to ground support
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




