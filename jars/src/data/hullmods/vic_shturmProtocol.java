package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class vic_shturmProtocol extends BaseHullMod {

    final float
            shieldUpKeep = 2f,
            disBonus = 2f,
            systemRechargeBonus = 50f,
            rangeReduction = 0.75f,
            pptReduction = 0.5f;

    private final Map<ShipAPI.HullSize, Float> rangeForClamp = new HashMap<>();

    {
        rangeForClamp.put(ShipAPI.HullSize.FRIGATE, 500f);
        rangeForClamp.put(ShipAPI.HullSize.DESTROYER, 700f);
        rangeForClamp.put(ShipAPI.HullSize.CRUISER, 900f);
        rangeForClamp.put(ShipAPI.HullSize.CAPITAL_SHIP, 1100f);
    }

    private final Map<ShipAPI.HullSize, Float> rangeBonus = new HashMap<>();

    {
        rangeBonus.put(ShipAPI.HullSize.FRIGATE, 100f);
        rangeBonus.put(ShipAPI.HullSize.DESTROYER, 150f);
        rangeBonus.put(ShipAPI.HullSize.CRUISER, 200f);
        rangeBonus.put(ShipAPI.HullSize.CAPITAL_SHIP, 300f);
    }

    private final Map<ShipAPI.HullSize, Float> speedBonus = new HashMap<>();

    {
        speedBonus.put(ShipAPI.HullSize.FRIGATE, 20f);
        speedBonus.put(ShipAPI.HullSize.DESTROYER, 15f);
        speedBonus.put(ShipAPI.HullSize.CRUISER, 10f);
        speedBonus.put(ShipAPI.HullSize.CAPITAL_SHIP, 10f);
    }

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);

    static {
        BLOCKED_HULLMODS.add("safetyoverrides");
        //BLOCKED_HULLMODS.add("stabilizedshieldemitter");
        //BLOCKED_HULLMODS.add("frontemitter");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, rangeBonus.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, rangeBonus.get(hullSize));
        stats.getWeaponRangeThreshold().modifyFlat(id, rangeForClamp.get(hullSize));
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, 1 - rangeReduction);
        stats.getShieldUpkeepMult().modifyMult(id, shieldUpKeep);
        stats.getSystemCooldownBonus().modifyPercent(id, -systemRechargeBonus);
        stats.getSystemRegenBonus().modifyPercent(id, systemRechargeBonus);
        stats.getMaxSpeed().modifyFlat(id, speedBonus.get(hullSize));
        stats.getPeakCRDuration().modifyMult(id, pptReduction);
    }


    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        float shieldReduction = 1;

        for (Map.Entry<String, MutableStat.StatMod> stat : ship.getMutableStats().getShieldUpkeepMult().getMultMods().entrySet()) {
            float value = stat.getValue().getValue();
            if (value < 1) {
                shieldReduction /= value;
            }
        }
        float percent = 100;
        for (Map.Entry<String, MutableStat.StatMod> stat : ship.getMutableStats().getShieldUpkeepMult().getPercentMods().entrySet()) {
            float value = stat.getValue().getValue();
            if (value < 0) {
                percent += value;
            }
        }
        shieldReduction *= 100 / percent;
        ship.getMutableStats().getShieldUpkeepMult().modifyMult("vic_shturmProtocol2", shieldReduction);

        if (ship.getShield() == null || ship.getShield().isOff()) {
            ship.getMutableStats().getFluxDissipation().modifyMult("vic_shturmProtocol", disBonus);
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip("vic_shturmProtocol", "graphics/icons/hullsys/vic_shturmFrameSus.png", "Shturm protocol", "Flux dissipation is doubled", false);
            }
        } else {
            ship.getMutableStats().getFluxDissipation().unmodify("vic_shturmProtocol");
        }

        if (Global.getCombatEngine().isPaused() || ship.getShipAI() == null) return;

        if (ship.getFluxTracker().getFluxLevel() > 0.85f && !ship.getSystem().isActive() && !ship.getFluxTracker().isOverloadedOrVenting()) {
            ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
        }
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
        MutableShipStatsAPI stats = ship.getMutableStats();
        String id = "vic_shturmProtocol";
        float tempStat = 0;
        for (Map.Entry<String, MutableStat.StatMod> entry : stats.getShieldUpkeepMult().getFlatMods().entrySet()) {
            if (entry.getValue().getValue() < 0) {
                tempStat += entry.getValue().getValue();
            }
        }
        stats.getShieldUpkeepMult().modifyFlat(id, -tempStat);

        tempStat = 1;
        for (Map.Entry<String, MutableStat.StatMod> entry : stats.getShieldUpkeepMult().getMultMods().entrySet()) {
            if (entry.getValue().getValue() < 1) {
                tempStat *= entry.getValue().getValue();
            }
        }
        stats.getShieldUpkeepMult().modifyMult(id, 1 / tempStat);

        tempStat = 0;
        for (Map.Entry<String, MutableStat.StatMod> entry : stats.getShieldUpkeepMult().getPercentMods().entrySet()) {
            if (entry.getValue().getValue() < 0) {
                tempStat += entry.getValue().getValue();
            }
        }
        stats.getShieldUpkeepMult().modifyPercent(id, -tempStat);
        if (!ship.getHullSpec().getHullId().startsWith("vic_")) return false;
        if (!ship.getVariant().getHullMods().contains("vic_shturmSolutionDummy")) return false;
        for (String Hmod : BLOCKED_HULLMODS) {
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
//        if (ship.getVariant().getHullMods().contains("stabilizedshieldemitter"))
//            return "Incompatible with Stabilized Shields";
        return null;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (hullSize == null) hullSize = ShipAPI.HullSize.FRIGATE;
        if (index == 0) return Math.round(shieldUpKeep) + "";
        if (index == 1) return Math.round(disBonus) + "";
        if (index == 2) return Math.round(rangeBonus.get(hullSize)) + "";
        if (index == 3) return Math.round(rangeForClamp.get(hullSize)) + "";
        if (index == 4) return Math.round(rangeReduction * 100) + "%";
        if (index == 5) return Math.round(systemRechargeBonus) + "%";
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

        Color highlight = Misc.getHighlightColor();
        Color goodHighlight = Misc.getPositiveHighlightColor();
        Color badHighlight = Misc.getNegativeHighlightColor();
        float pad = 10f;
        float padS = 3f;

        tooltip.addSectionHeading("Effects", Alignment.MID, pad);

        tooltip.setBulletedListMode("  • ");
        //logical
//        tooltip.addPara("Prevents any reduction in shield upkeep", pad, badHighlight, "any reduction");
//
//        tooltip.addPara("Increases shield upkeep by a factor of %s", padS, badHighlight, "x" + Math.round(shieldUpKeep));
//
//        tooltip.addPara("Increases flux dissipation rate by a factor of %s", padS, goodHighlight, "x" + Math.round(disBonus));
//
//        tooltip.addPara("Increases energy and ballistic weapons range by %s/%s/%s/%s", pad, goodHighlight, Math.round(rangeBonus.get(ShipAPI.HullSize.FRIGATE)) + "",
//                Math.round(rangeBonus.get(ShipAPI.HullSize.DESTROYER)) + "", Math.round(rangeBonus.get(ShipAPI.HullSize.CRUISER)) + "", Math.round(rangeBonus.get(ShipAPI.HullSize.CAPITAL_SHIP)) + "");
//
//        tooltip.addPara("Reduces weapon range past %s/%s/%s/%s by %s", padS, badHighlight, Math.round(rangeForClamp.get(ShipAPI.HullSize.FRIGATE)) + "",
//                Math.round(rangeForClamp.get(ShipAPI.HullSize.DESTROYER)) + "", Math.round(rangeForClamp.get(ShipAPI.HullSize.CRUISER)) + "",
//                Math.round(rangeForClamp.get(ShipAPI.HullSize.CAPITAL_SHIP)) + "", Math.round(Math.round(rangeReduction * 100)) + "%");
//
//        tooltip.addPara("Increases the ship's system cooldown recovery and charge generation rate by %s", pad, goodHighlight, Math.round(systemRechargeBonus) + "%");
//
//        tooltip.addPara("Increases the ship's top speed by %s/%s/%s/%s", pad, goodHighlight, Math.round(speedBonus.get(ShipAPI.HullSize.FRIGATE)) + "",
//                Math.round(speedBonus.get(ShipAPI.HullSize.DESTROYER)) + "", Math.round(speedBonus.get(ShipAPI.HullSize.CRUISER)) + "",
//                Math.round(speedBonus.get(ShipAPI.HullSize.CAPITAL_SHIP)) + "");
//        tooltip.addPara("Reduces the peak performance time by %s", padS, badHighlight, Math.round(pptReduction * 100f) + "%");

        //negatives and postitive

        tooltip.addPara("Increases the ship's top speed by %s/%s/%s/%s", padS, goodHighlight, Math.round(speedBonus.get(ShipAPI.HullSize.FRIGATE)) + "",
                Math.round(speedBonus.get(ShipAPI.HullSize.DESTROYER)) + "", Math.round(speedBonus.get(ShipAPI.HullSize.CRUISER)) + "",
                Math.round(speedBonus.get(ShipAPI.HullSize.CAPITAL_SHIP)) + "");

        tooltip.addPara("Increases the ship's system cooldown recovery and charge generation rate by %s", padS, goodHighlight, Math.round(systemRechargeBonus) + "%");

        tooltip.addPara("When shields are disabled increases flux dissipation rate by a factor of %s", padS, goodHighlight, "x" + Math.round(disBonus));

        tooltip.addPara("Increases energy and ballistic weapons range by %s/%s/%s/%s", padS, goodHighlight, Math.round(rangeBonus.get(ShipAPI.HullSize.FRIGATE)) + "",
                Math.round(rangeBonus.get(ShipAPI.HullSize.DESTROYER)) + "", Math.round(rangeBonus.get(ShipAPI.HullSize.CRUISER)) + "", Math.round(rangeBonus.get(ShipAPI.HullSize.CAPITAL_SHIP)) + "");

        tooltip.addPara("Reduces weapon range past %s/%s/%s/%s by %s", padS, badHighlight, Math.round(rangeForClamp.get(ShipAPI.HullSize.FRIGATE)) + "",
                Math.round(rangeForClamp.get(ShipAPI.HullSize.DESTROYER)) + "", Math.round(rangeForClamp.get(ShipAPI.HullSize.CRUISER)) + "",
                Math.round(rangeForClamp.get(ShipAPI.HullSize.CAPITAL_SHIP)) + "", Math.round(Math.round(rangeReduction * 100)) + "%");

        tooltip.addPara("Increases shield upkeep by a factor of %s", padS, badHighlight, "x" + Math.round(shieldUpKeep));

        tooltip.addPara("Prevents any reduction in shield upkeep", padS, badHighlight, "any reduction");

        tooltip.addPara("Reduces the peak performance time by %s", padS, badHighlight, Math.round(pptReduction * 100f) + "%");

        tooltip.setBulletedListMode(null);


        tooltip.addSectionHeading("Incompatibilities", Alignment.MID, pad);

        tooltip.addPara("Incompatible with Safety Overrides", pad);
    }
}
