package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class vic_assault extends BaseHullMod {



    private final Map<ShipAPI.HullSize, Float> fluxEfficiency = new HashMap<>();

    {
        fluxEfficiency.put(HullSize.FRIGATE, 5f);
        fluxEfficiency.put(HullSize.DESTROYER, 10f);
        fluxEfficiency.put(HullSize.CRUISER, 20f);
        fluxEfficiency.put(HullSize.CAPITAL_SHIP, 30f);
    }

    private final Map<ShipAPI.HullSize, Float> speedBonus = new HashMap<>();

    {
        speedBonus.put(HullSize.FRIGATE, 10f);
        speedBonus.put(HullSize.DESTROYER, 10f);
        speedBonus.put(HullSize.CRUISER, 20f);
        speedBonus.put(HullSize.CAPITAL_SHIP, 20f);
    }

    public float
            projSpeedMult = 1.33f,
            pptPenalty = 0.33f;

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
				ship.getVariant().removeMod(tmp);
			}
		}
	}

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().modifyFlat(id, speedBonus.get(hullSize));
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, (1 - (fluxEfficiency.get(hullSize) * 0.01f)));
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1 - (fluxEfficiency.get(hullSize) * 0.01f)));
        stats.getBeamWeaponFluxCostMult().modifyMult(id, (1 - (fluxEfficiency.get(hullSize) * 0.01f)));
        stats.getMissileWeaponFluxCostMod().modifyMult(id, (1 - (fluxEfficiency.get(hullSize) * 0.01f)));

        stats.getMaxSpeed().modifyMult(id, 1 + (fluxEfficiency.get(hullSize) * 0.01f));
        stats.getProjectileSpeedMult().modifyMult(id, projSpeedMult );



        ShipVariantAPI variant = stats.getVariant();
        float pptMult = 2f;

        if (variant != null && variant.hasHullMod("vic_allRoundShieldUpgrade")){
            pptMult = 1f;
        }
        if (variant != null && variant.hasHullMod("vic_deathProtocol")){
            pptMult = 1f;
        }
        if (!variant.getHullSpec().getHullId().startsWith("vic_")){
            pptMult =  1f;
        }

        stats.getPeakCRDuration().modifyMult(id, pptPenalty * pptMult);

    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return  (!ship.getVariant().getHullMods().contains("dedicated_targeting_core") &&
                !ship.getVariant().getHullMods().contains("unstable_injector") &&
                !ship.getVariant().getHullMods().contains("advancedcore") &&
                !ship.getVariant().getHullMods().contains("targetingunit"));
    }

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
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



        if (index == 0) return (fluxEfficiency.get(HullSize.FRIGATE)).intValue() + "%";
        if (index == 1) return (fluxEfficiency.get(HullSize.DESTROYER)).intValue() + "%";
        if (index == 2) return (fluxEfficiency.get(HullSize.CRUISER)).intValue() + "%";
        if (index == 3) return (fluxEfficiency.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
        if (index == 4) return (speedBonus.get(HullSize.FRIGATE)).intValue() + "";
        if (index == 5) return (speedBonus.get(HullSize.DESTROYER)).intValue() + "";
        if (index == 6) return (speedBonus.get(HullSize.CRUISER)).intValue() + "";
        if (index == 7) return (speedBonus.get(HullSize.CAPITAL_SHIP)).intValue() + "";
        if (index == 8) return Math.round((projSpeedMult - 1) * 100)+ "%";
        if (index == 9) return Math.round(pptPenalty * 100) + "%";
        return null;
    }
}




