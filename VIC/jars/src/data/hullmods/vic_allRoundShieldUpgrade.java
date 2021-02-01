package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.HashSet;
import java.util.Set;

public class vic_allRoundShieldUpgrade extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);

    static {
        BLOCKED_HULLMODS.add("stabilizedshieldemitter");
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("advancedshieldemitter");
        BLOCKED_HULLMODS.add("extendedshieldemitter");
    }

    public final float
            shieldEff = 13f,
            shieldUpkeep = 25f,
            shieldSpeed = 50f,
            shieldArc = 30f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - shieldEff * 0.01f);
        stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, 0.75f);
        stats.getShieldUpkeepMult().modifyMult(id, 1f - shieldUpkeep * 0.01f);
        stats.getShieldTurnRateMult().modifyPercent(id, shieldSpeed);
        stats.getShieldUnfoldRateMult().modifyPercent(id, shieldSpeed);
        stats.getShieldArcBonus().modifyFlat(id, shieldArc);
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
        boolean speedPenalty = false;
        if (variant != null && variant.hasHullMod("vic_deathProtocol")) {
            speedPenalty = true;
        }
        if (variant != null && variant.hasHullMod("vic_assault")) {
            speedPenalty = true;
        }
        if (variant != null && !variant.getHullSpec().getHullId().startsWith("vic_")) {
            speedPenalty = true;
        }

        if (speedPenalty) {
            stats.getMaxSpeed().modifyFlat(id, stats.getMaxSpeed().getModifiedValue() * -0.25f);
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        for (String Hmod : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(Hmod)) return false;
        }
        return true;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {

        if (ship.getVariant().getHullMods().contains("hardenedshieldemitter"))
            return "Incompatible with Hardened Shields";
        if (ship.getVariant().getHullMods().contains("stabilizedshieldemitter"))
            return "Incompatible with Stabilized Shields";
        if (ship.getVariant().getHullMods().contains("advancedshieldemitter"))
            return "Incompatible with Accelerated Shields";
        if (ship.getVariant().getHullMods().contains("extendedshieldemitter"))
            return "Incompatible with Extended Shields";
        return null;
    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return Math.round(shieldEff) + "%";
        if (index == 1) return Math.round(shieldUpkeep) + "%";
        if (index == 2) return Math.round(shieldSpeed) + "%";
        if (index == 3) return Math.round(shieldSpeed) + "%";
        if (index == 4) return Math.round(shieldArc) + "";
        if (index == 5) return Math.round(25f) + "%";
        return null;
    }
}




