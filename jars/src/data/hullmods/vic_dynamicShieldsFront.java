package data.hullmods;

// Written by AxleMC131

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lazywizard.lazylib.MathUtils;

public class vic_dynamicShieldsFront extends BaseHullMod {

    public final float MAX_ARC_MULT = 2f;    // Maximum multiplier applied to the base shield arc
    public final float IDEAL_ANGLE = 180f;   // The angle of deflection at which the shield is at max arc
    
    // Debugging status messages
    protected Object STATUSKEY1 = new Object();
    protected Object STATUSKEY2 = new Object();
    protected Object STATUSKEY3 = new Object();
	
    /*private float scaleArcToIdealAngle(float idealAngle, float currAngle) {
        if (currAngle <= idealAngle) {
            return currAngle;
        }
        if (currAngle > 180f) {
            return 180f;
        }
        float equivalentLowAngle = (2f * idealAngle) - currAngle;
        if (equivalentLowAngle > 0f) {
            return equivalentLowAngle;
        }
        return 0f;
    }*/
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        
        if (ship.getShield().isOff()) {
            return;
        }
        
        float shieldBaseArc = ship.getHullSpec().getShieldSpec().getArc();
        
        float shieldRelFacing = Math.abs(MathUtils.getShortestRotation(ship.getShield().getFacing(), ship.getFacing()));
        //float normalizedAngle = scaleArcToIdealAngle(IDEAL_ANGLE, shieldRelFacing);
        float shieldArcMult = 1f - ((shieldRelFacing / IDEAL_ANGLE) * MAX_ARC_MULT);
        
        ship.getShield().setArc(shieldBaseArc * shieldArcMult);
        
        
        Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
		null, 
                "base arc: " + (int) shieldBaseArc, 
                "curr arc: " + (int) ship.getShield().getActiveArc() + " / " + (int) ship.getShield().getArc(),
                false);
        
        Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
		null, 
                "shield rel facing: " + (int) shieldRelFacing, 
                "arc multiplier: " + (double) shieldArcMult,
                false);
        
        Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
		null, 
                "ideal angle: " + (int) IDEAL_ANGLE, 
                "-",//"normalized: " + (int) normalizedAngle,
                false);
        
    }
    
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	
    }
	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	//if (index == 0) return "" + (int) REFIT_INCREASE + "%";
	return null;
    }
}



