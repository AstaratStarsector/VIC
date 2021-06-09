package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.scripts.util.MagicIncompatibleHullmods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class vic_assault extends BaseHullMod {



    float fluxEfficiency = 20f;

    private final Map<ShipAPI.HullSize, Float> speedBonus = new HashMap<>();

    {
        speedBonus.put(HullSize.FRIGATE, 5f);
        speedBonus.put(HullSize.DESTROYER, 10f);
        speedBonus.put(HullSize.CRUISER, 20f);
        speedBonus.put(HullSize.CAPITAL_SHIP, 25f);
    }

    public float
            projSpeedMult = 1.3f,
            pptPenalty = 0.7f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);


    static {
		BLOCKED_HULLMODS.add("dedicated_targeting_core");
		BLOCKED_HULLMODS.add("targetingunit");
		BLOCKED_HULLMODS.add("unstable_injector");
		BLOCKED_HULLMODS.add("advancedcore");
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				//ship.getVariant().removeMod(tmp);
                MagicIncompatibleHullmods.removeHullmodWithWarning(
                        ship.getVariant(),
                        tmp,
                        "vic_assault"
                );
			}
		}
	}

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().modifyFlat(id, speedBonus.get(hullSize));
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 - (fluxEfficiency * 0.01f)));
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (fluxEfficiency * 0.01f)));
        stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (fluxEfficiency * 0.01f)));
        stats.getMissileWeaponFluxCostMod().modifyMult(id, (1 - (fluxEfficiency * 0.01f)));

        //stats.getMaxSpeed().modifyMult(id, 1 + (fluxEfficiency.get(hullSize) * 0.01f));
        stats.getProjectileSpeedMult().modifyMult(id, projSpeedMult );

        ShipVariantAPI variant = stats.getVariant();
        boolean penaltyOn = false;

        if (variant != null && variant.hasHullMod("vic_allRoundShieldUpgrade")){
            penaltyOn = true;
        }
        if (variant != null && variant.hasHullMod("vic_deathProtocol")){
            penaltyOn = true;
        }
        assert variant != null;
        if (!variant.getHullSpec().getHullId().startsWith("vic_")){
            penaltyOn = true;
        }

        if (penaltyOn) stats.getPeakCRDuration().modifyMult(id, pptPenalty);
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        for (String hmod : BLOCKED_HULLMODS){
            if (ship.getVariant().getHullMods().contains(hmod)) return false;
        }
        return ship.getHullSpec().getHullId().startsWith("vic_");
    }

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.getHullSpec().getHullId().startsWith("vic_"))
            return "Not compatible with non VIC ships";
		if (ship.getVariant().getHullMods().contains("dedicated_targeting_core"))
			return "Incompatible with Dedicated Targeting Core";
		if (ship.getVariant().getHullMods().contains("targetingunit"))
			return "Incompatible with Integrated Targeting Unit";
        if (ship.getVariant().getHullMods().contains("unstable_injector"))
            return "Incompatible with Unstable Injector";
        if (ship.getVariant().getHullMods().contains("advancedcore"))
            return "Incompatible with Advanced Targeting Core";
		return null;
	}

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return Math.round(fluxEfficiency) + "%";
        if (index == 1) return (speedBonus.get(HullSize.FRIGATE)).intValue() + "";
        if (index == 2) return (speedBonus.get(HullSize.DESTROYER)).intValue() + "";
        if (index == 3) return (speedBonus.get(HullSize.CRUISER)).intValue() + "";
        if (index == 4) return (speedBonus.get(HullSize.CAPITAL_SHIP)).intValue() + "";
        if (index == 5) return Math.round((projSpeedMult - 1) * 100)+ "%";
        if (index == 6) return Math.round((1 - pptPenalty) * 100) + "%";
        return null;
    }
}




