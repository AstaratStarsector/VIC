package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.entities.Ship;


import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class vic_powerRedistributor extends BaseHullMod {

    //None
    //positive
    float zeroFluxBoost = 30;
    //negative
    float RoF = 20f;
    float noneShieldEff = 10f;

    //Small
    //positive
    float speed = 10f;
    float maneuverability = 25f;
    float projSpeed = 25f;
    //negative
    float smallShieldEff = 15f;

    //Large
    //positive
    float weaponDamage = 10f;
    float weaponFluxCost = 10f;
    float largeShieldEff = 10f;
    //negative
    float turretSpeed = 25f;
    float largeManeuverability = 15f;

    float timeToSwitch = 2;

    enum state {
        none,
        small,
        large
    }

    String
            noTarget = "Cruise ",
            small = "Speed ",
            large = "Engage ";

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String shipID = ship.getId();

        HashMap<state, Float> powers = new HashMap<state, Float>();
        for (state s : state.values()) {
            powers.put(s, 0f);
        }
        if (customCombatData.get("vic_powerRedistributor" + shipID) instanceof HashMap) {
            powers = (HashMap<state, Float>) customCombatData.get("vic_powerRedistributor" + shipID);
        } else {
            customCombatData.put("vic_powerRedistributor" + shipID, powers);
        }

        String id = "vic_targetAnalysis";
        ShipAPI.HullSize target = null;
        if (ship.getShipTarget() != null) {
            target = ship.getShipTarget().getHullSize();
        } else if (ship.getAI() != null){
            try {
                target = ship.getWeaponGroupsCopy().get(0).getAIPlugins().get(0).getTargetShip().getHullSize();
            } catch (Exception e) {
            }
        }
        for (Map.Entry<state, Float> entry : powers.entrySet()) {
            entry.setValue(Math.max(0, entry.getValue() - amount / timeToSwitch));
        }
        amount *= 2 / timeToSwitch;
        String status = "";
        if (target != null) {
            switch (target) {
                case FRIGATE:
                case DESTROYER:
                    powers.put(state.small, Math.min(1f, powers.get(state.small) + amount));
                    status = small + Math.round(powers.get(state.small) * 100) + "%";
                    break;
                case CRUISER:
                case CAPITAL_SHIP:
                    powers.put(state.large, Math.min(1f, powers.get(state.large) + amount));
                    status += large + Math.round(powers.get(state.large) * 100) + "%";
                    break;
                default:
                    powers.put(state.none, Math.min(1f, powers.get(state.none) + amount));
                    status = noTarget + Math.round(powers.get(state.none) * 100) + "%";
                    break;
            }
        } else {
            //effect if no target
            powers.put(state.none, Math.min(1f, powers.get(state.none) + amount));
            status = noTarget + Math.round(powers.get(state.none) * 100) + "%";
        }
        if (ship.getFluxTracker().isOverloaded()){
            for (Map.Entry<state, Float> entry : powers.entrySet()) {
                entry.setValue(0f);
            }
            status = "Overloaded, system shutdown";
        }

        MutableShipStatsAPI stats = ship.getMutableStats();
        //apply

        float powerLevel = 0;
        //none + fighters
        powerLevel = powers.get(state.none);

        String noneID = id + noTarget;
        stats.getZeroFluxSpeedBoost().modifyFlat(noneID, zeroFluxBoost * powerLevel);

        //small frigates + DD
        powerLevel = powers.get(state.small);

        String smallID = id + small;
        stats.getMaxSpeed().modifyFlat(smallID, speed * powerLevel);
        stats.getAcceleration().modifyPercent(smallID, maneuverability * powerLevel * 2f);
        stats.getDeceleration().modifyPercent(smallID, maneuverability * powerLevel);
        stats.getTurnAcceleration().modifyPercent(smallID, maneuverability * powerLevel * 2f);
        stats.getMaxTurnRate().modifyPercent(smallID, maneuverability * powerLevel);
        stats.getProjectileSpeedMult().modifyPercent(smallID, projSpeed * powerLevel);

        stats.getShieldDamageTakenMult().modifyMult(smallID, 1f + smallShieldEff * 0.01f * powerLevel);

        //large cruisers + caps
        powerLevel = powers.get(state.large);
        
        String largeID = id + large;
        stats.getEnergyWeaponDamageMult().modifyMult(largeID, 1 + weaponDamage * 0.01f * powerLevel);
        stats.getEnergyWeaponFluxCostMod().modifyMult(largeID, 1 + weaponFluxCost * 0.01f * powerLevel);
        stats.getBallisticWeaponDamageMult().modifyMult(largeID, 1 + weaponDamage * 0.01f * powerLevel);
        stats.getBallisticWeaponFluxCostMod().modifyMult(largeID, 1 + weaponFluxCost * 0.01f * powerLevel);
        stats.getShieldDamageTakenMult().modifyMult(largeID, 1f - largeShieldEff * 0.01f * powerLevel);

        stats.getAcceleration().modifyPercent(largeID, largeManeuverability * powerLevel * 2f);
        stats.getDeceleration().modifyPercent(largeID, largeManeuverability * powerLevel);
        stats.getTurnAcceleration().modifyPercent(largeID, largeManeuverability * powerLevel * 2f);
        stats.getMaxTurnRate().modifyPercent(largeID, largeManeuverability * powerLevel);
        

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_powerRedistributor", "graphics/icons/hullsys/vic_powerRedistributor.png", "Power Redistributor", status, false);
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        //tooltip.addSectionHeading("Details", Alignment.MID, pad);

        Color good = new Color(74, 144, 7, 255);
        Color bad = new Color(217, 47, 5, 255);
        good = Misc.getPositiveHighlightColor();
        bad = Misc.getNegativeHighlightColor();

        //None
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/vic_powerRedistributorNone.png", 48);
        text.addPara(noTarget + "(no target)",Misc.getHighlightColor(), 2);
        text.addPara("Increases 0-flux boost by %s", 1, good, Math.round(zeroFluxBoost) + " su/sec");
        tooltip.addImageWithText(pad);

        //Small
        text = tooltip.beginImageWithText("graphics/icons/hullsys/vic_powerRedistributorSmall.png", 48);
        text.addPara(small + "(fighter, frigate, destroyer)",Misc.getHighlightColor(), 2);
        text.addPara("Increases max speed by %s", 1, good, Math.round(speed) + " su/sec");
        text.addPara("Increases maneuverability by %s", 1, good, Math.round(maneuverability) + "%");
        text.addPara("Increases projectile speed by %s", 1, good, Math.round(projSpeed) + "%");
        text.addPara("Increases damage taken by shield by %s", 1, bad, Math.round(smallShieldEff) + "%");
        tooltip.addImageWithText(pad);

        //Large
        text = tooltip.beginImageWithText("graphics/icons/hullsys/vic_powerRedistributorLarge.png", 48);
        text.addPara(large + "(cruiser, capital)", Misc.getHighlightColor(), 2);
        text.addPara("Increases ballistic and energy weapon damage and flux cost by %s", 1, good, Math.round(weaponDamage) + "%");
        text.addPara("Reduces damage taken by shield by %s", 1, good, Math.round(largeShieldEff) + "%");
        text.addPara("Decreases weapon turn rate by %s", 1, bad, Math.round(turretSpeed) + "%");
        text.addPara("Decreases maneuverability by %s", 1, bad, Math.round(largeManeuverability) + "%");
        tooltip.addImageWithText(pad);
    }
}
