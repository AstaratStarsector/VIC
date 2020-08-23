package data.hullmods;

// Written by AxleMC131

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import org.lazywizard.lazylib.MathUtils;

public class vic_dynamicshields extends BaseHullMod {

    public final float MAX_ARC_MULT = 2f;    // Maximum multiplier applied to the base shield arc
    public final float IDEAL_ANGLE = 180f;   // The angle of deflection at which the shield is at max arc



    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {



        if (ship.getShield() == null || ship.getShield().getType() != ShieldAPI.ShieldType.OMNI || ship.getShield().isOff()) {
            return;
        }


         float shipArcMult = 0f;
        switch (ship.getHullSpec().getHullId())
        {
            case "vic_thamuz":
            case "vic_samael":
                shipArcMult = 0.5f;
                //do stuff for case 1 and 2
                break;
            default:
                break;
        }


        float shieldBaseArc = ship.getHullSpec().getShieldSpec().getArc();

        float shieldRelFacing = Math.abs(MathUtils.getShortestRotation(ship.getShield().getFacing(), ship.getFacing()));
        //float normalizedAngle = scaleArcToIdealAngle(IDEAL_ANGLE, shieldRelFacing);
        float shieldArcMult = 1 + ((shieldRelFacing / IDEAL_ANGLE) * (MAX_ARC_MULT - shipArcMult - 1f));

        ship.getShield().setArc(shieldBaseArc * shieldArcMult);


    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {

        float shipArcMult = 0f;
        switch (ship.getHullSpec().getHullId())
        {
            case "vic_thamuz":
            case "vic_samael":
                shipArcMult = 0.5f;
                //do stuff for case 1 and 2
                break;
            default:
                break;
        }


        if (index == 0) return Math.round(ship.getHullSpec().getShieldSpec().getArc() * MAX_ARC_MULT - shipArcMult) + "";
        return null;
    }
}



