package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.HashSet;
import java.util.Set;

public class vic_allRoundShieldUpgrade extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);

    static {
        //BLOCKED_HULLMODS.add("stabilizedshieldemitter");
        BLOCKED_HULLMODS.add(HullMods.HARDENED_SHIELDS);
        BLOCKED_HULLMODS.add(HullMods.SAFETYOVERRIDES);
        //BLOCKED_HULLMODS.add("advancedshieldemitter");
        //BLOCKED_HULLMODS.add("extendedshieldemitter");
    }

    public final float
            shieldEff = 35f,
            shieldUpkeep = 25f,
            shieldSpeed = 50f,
            speedRed = 20f,
            zeroFluxBoost = 15f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().modifyFlat(id, stats.getMaxSpeed().getModifiedValue() * (-speedRed * 0.01f));
        stats.getZeroFluxSpeedBoost().modifyFlat(id, zeroFluxBoost);

        stats.getShieldDamageTakenMult().modifyMult(id, 1f - shieldEff * 0.01f);
        stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, 1 - 0.75f);

        stats.getShieldUpkeepMult().modifyPercent(id, shieldUpkeep);

        stats.getShieldTurnRateMult().modifyMult(id, 1f - shieldSpeed * 0.01f);
        stats.getShieldUnfoldRateMult().modifyMult(id, 1f - shieldSpeed * 0.01f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }

        MutableShipStatsAPI stats = ship.getMutableStats();
        ShipVariantAPI variant = stats.getVariant();
        boolean speedPenalty = variant != null && (variant.hasHullMod("vic_deathProtocol") || variant.hasHullMod("vic_assault"));

        if (speedPenalty) {
            stats.getMaxSpeed().modifyFlat(id + "second", stats.getMaxSpeed().getModifiedValue() * -0.25f);
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        for (String Hmod : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(Hmod)) return false;
        }
        return ship.getHullSpec().getHullId().startsWith("vic_");
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.getHullSpec().getHullId().startsWith("vic_"))
            return "Not compatible with non VIC ships";
        if (ship.getVariant().getHullMods().contains("hardenedshieldemitter"))
            return "Incompatible with Hardened Shields";
        return null;
    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return Math.round(shieldEff) + "%";
        if (index == 1) return Math.round(shieldUpkeep) + "%";
        if (index == 2) return Math.round(shieldSpeed) + "%";
        if (index == 3) return Math.round(shieldSpeed) + "%";
        if (index == 4) return Math.round(speedRed) + "%";
        if (index == 5) return Math.round(zeroFluxBoost) + " SU";
        if (index == 6) return Math.round(25f) + "%";
        return null;
    }
}




