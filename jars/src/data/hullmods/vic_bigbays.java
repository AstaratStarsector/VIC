package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.DefectiveManufactory;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;


public class vic_bigbays extends BaseHullMod {

    public static final int CREW_REQ = 20;
    public static final int ALL_FIGHTER_COST_PERCENT = 100;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
        stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
        stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
        stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (bay.getWing() == null) continue;

            FighterWingSpecAPI spec = bay.getWing().getSpec();

            int addForWing = spec.getNumFighters();
            int maxTotal = addForWing * 2;

            if (ship.getFullTimeDeployed() < 1f){
                bay.setFastReplacements(addForWing);
                bay.setExtraDuration(99999);
            }

            //int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
            bay.setExtraDeployments(addForWing);
            bay.setExtraDeploymentLimit(maxTotal);


            //Global.getCombatEngine().maintainStatusForPlayerShip("info", "graphics/icons/hullsys/ammo_feeder.png", "Debug", ship.getFullTimeDeployed() + "" , false);
        }


    }


    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 2) return "" + CREW_REQ;
        if (index == 3) return "" + ALL_FIGHTER_COST_PERCENT + "%";
        if (index == 4) return "" + ALL_FIGHTER_COST_PERCENT + "%";
        return new DefectiveManufactory().getDescriptionParam(index, hullSize, ship);
    }


    @Override
    public boolean affectsOPCosts() {
        return true;
    }

}