package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import java.util.*;

public class vic_DPDCore extends BaseHullMod {

    private final Map<HullSize, Float> mag = new HashMap<>();
    private final float speedBonus = 20f;


    {
        mag.put(HullSize.FRIGATE, 30f);
        mag.put(HullSize.DESTROYER, 40f);
        mag.put(HullSize.CRUISER, 50f);
        mag.put(HullSize.CAPITAL_SHIP, 60f);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, mag.get(hullSize));
        stats.getBeamPDWeaponRangeBonus().modifyPercent(id, mag.get(hullSize));
    }


    /* ITD DTC ban
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);

    static {
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
        BLOCKED_HULLMODS.add("targetingunit");
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return (!ship.getVariant().getHullMods().contains("dedicated_targeting_core") &&
                !ship.getVariant().getHullMods().contains("targetingunit"));
    }

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains("dedicated_targeting_core"))
			return "Incompatible with Dedicated Targeting Core";
		if (ship.getVariant().getHullMods().contains("targetingunit"))
			return "Incompatible with Integrated Targeting Unit";
		return null;
	}
     */

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return (mag.get(HullSize.FRIGATE)).intValue() + "%";
        if (index == 1) return (mag.get(HullSize.DESTROYER)).intValue() + "%";
        if (index == 2) return (mag.get(HullSize.CRUISER)).intValue() + "%";
        if (index == 3) return (mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
        if (index == 4) return "" + Math.round(speedBonus);
        return null;
    }
}




