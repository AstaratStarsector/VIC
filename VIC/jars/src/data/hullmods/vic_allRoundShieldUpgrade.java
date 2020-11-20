package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.HashSet;
import java.util.Set;

public class vic_allRoundShieldUpgrade extends BaseHullMod {

    public final float
            shieldEff = 13f,
            shieldUpkeep = 25f,
            shieldSpeed = 50f,
            shieldArc = 30f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - shieldEff * 0.01f);
        stats.getShieldUpkeepMult().modifyMult(id,1f - shieldUpkeep * 0.01f);
        stats.getShieldTurnRateMult().modifyPercent(id, shieldSpeed);
        stats.getShieldUnfoldRateMult().modifyPercent(id, shieldSpeed);
        stats.getShieldArcBonus().modifyFlat(id, shieldArc);
    }

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);
    static {
        BLOCKED_HULLMODS.add("stabilizedshieldemitter");
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("advancedshieldemitter");

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
        boolean OK = true;
        if (!ship.getHullSpec().getHullId().startsWith("vic_")) return false;
        for (String Hmod : BLOCKED_HULLMODS){
            if (ship.getVariant().getHullMods().contains(Hmod)) return false;
        }
        return true;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.getHullSpec().getHullId().startsWith("vic_"))
            return "Must be installed on a VIC ship";
        if (ship.getVariant().getHullMods().contains("hardenedshieldemitter"))
            return "Incompatible with Hardened Shields";
        if (ship.getVariant().getHullMods().contains("stabilizedshieldemitter"))
            return "Incompatible with Stabilized Shields";
        if (ship.getVariant().getHullMods().contains("advancedshieldemitter"))
            return "Incompatible with Accelerated Shields";
        return null;
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(shieldEff) + "%";
        if (index == 1) return Math.round(shieldUpkeep) + "%";
        if (index == 2) return Math.round(shieldSpeed) + "%";
        if (index == 3) return Math.round(shieldSpeed) + "%";
        if (index == 4) return Math.round(shieldArc) + "";
        return null;
    }
}




