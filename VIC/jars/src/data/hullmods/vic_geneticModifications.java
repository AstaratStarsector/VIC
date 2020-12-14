package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.launcher.ModManager;
import exerelin.campaign.AllianceManager;

public class vic_geneticModifications extends BaseHullMod {

    private final float timeAcellBonus = 1.1f;
    private final float acellBonus = 50f;
    private final float damageTaken = 5f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getTimeMult().modifyMult(id, timeAcellBonus);
        stats.getAcceleration().modifyPercent(id, acellBonus);
        stats.getTurnAcceleration().modifyPercent(id, acellBonus);
        stats.getShieldDamageTakenMult().modifyPercent(id, damageTaken);
        stats.getArmorDamageTakenMult().modifyPercent(id, damageTaken);
        stats.getHullDamageTakenMult().modifyPercent(id, damageTaken);

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (Global.getSector().getPlayerFleet() == null) return;
        String playerCommission = "player";
        String factionID = "vic";
        String HmodID = "vic_geneticmodifications";
        if (ModManager.getInstance().isModEnabled("nexerelin")) {
            if (Misc.getCommissionFactionId() != null) playerCommission = Misc.getCommissionFactionId();
            if (!(factionID.equals(playerCommission) || AllianceManager.areFactionsAllied(playerCommission, factionID))) {
                ship.getVariant().removeMod(HmodID);
            }
        } else {
            if (!factionID.equals(Misc.getCommissionFactionId())) {
                ship.getVariant().removeMod(HmodID);
            }
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round((timeAcellBonus - 1) * 100) + "";
        if (index == 1) return Math.round(acellBonus) + "%";
        if (index == 2) return Math.round(damageTaken) + "%";
        return null;
    }
}