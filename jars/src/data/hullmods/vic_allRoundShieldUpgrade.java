package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static data.scripts.shipsystems.vic_shieldHardening.shieldDamageTakenReduction;
import static data.scripts.shipsystems.vic_shieldHardening.weaponRoFReduction;

public class vic_allRoundShieldUpgrade extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);

    static {
        //BLOCKED_HULLMODS.add("stabilizedshieldemitter");
        //BLOCKED_HULLMODS.add(HullMods.HARDENED_SHIELDS);
        //BLOCKED_HULLMODS.add(HullMods.SAFETYOVERRIDES);
        //BLOCKED_HULLMODS.add("advancedshieldemitter");
        //BLOCKED_HULLMODS.add("extendedshieldemitter");
    }

    public final float
            shieldEff = 30f,
            damageReduction = 25f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - shieldEff * 0.01f);
        stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, 1 - 0.75f);

        stats.getEnergyWeaponDamageMult().modifyMult(id, 1 - (damageReduction * 0.01f));
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1 - (damageReduction * 0.01f));

        stats.getBallisticWeaponDamageMult().modifyMult(id, 1 - (damageReduction * 0.01f));
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1 - (damageReduction * 0.01f));
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        for (String Hmod : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(Hmod)) return false;
        }
        return ship.getVariant().hasHullMod("vic_convoyDrive");
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.getVariant().hasHullMod("vic_convoyDrive"))
            return "Can only be installed on ships with Convoy Drive";
        return null;
    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 6) return Math.round(25f) + "%";
        return null;
    }


    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        Color goodHighlight = Misc.getPositiveHighlightColor();
        Color badHighlight = Misc.getNegativeHighlightColor();
        float pad = 10f;
        float padS = 3f;

        tooltip.addSectionHeading("Effects", Alignment.MID, pad);
        tooltip.setBulletedListMode("  • ");
        tooltip.addPara("Reduces shield damage taken by %s", pad, goodHighlight, Math.round(shieldEff) + "%");
        tooltip.addPara("Greatly reduces the chance that shields will be pierced by EMP arcs.", padS, goodHighlight, "Greatly reduces");
        tooltip.addPara("Reduces damage and flux cost of energy and ballistic weapons by %s", padS, badHighlight, Math.round(damageReduction) + "%");
        tooltip.setBulletedListMode(null);

        tooltip.addSectionHeading("New Ship System", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/skills/defensive_systems.png", 64);
        text.addPara("Shield Entrenchment (toggle)", Misc.getTooltipTitleAndLightHighlightColor(), 2);
        text.addPara("Redirect the ship's energy to significantly harden the shield. Reduces shield damage taken by %s but reduces ship's weapons' rate of fire by %s. " +
                        "Has secondary battery which absorbs part of shield hits. While the system is active, battery absorbs up to %s of hits' damage at %s flux down to %s at %s flux. " +
                        "Battery capacity equals %s ships max flux.",
                2, Misc.getHighlightColor(), Math.round(shieldDamageTakenReduction * 100) + "%", Math.round(weaponRoFReduction * 100) + "%", "70%", "90%", "0%", "50%", "150%");
        tooltip.addImageWithText(pad);
    }
}




