package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lazywizard.lazylib.MathUtils;

// Written by AxleMC131 for Astarat
public class vic_dynamicShields extends BaseHullMod {

    public final float MAX_ARC_MULT = 2f;    // Maximum multiplier applied to the base shield arc
    public final float IDEAL_ANGLE = 180f;   // The angle of deflection at which the shield is at max arc

    public final float shieldSpeed = 1.5f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        stats.getShieldUnfoldRateMult().modifyMult(id, shieldSpeed);
        stats.getShieldTurnRateMult().modifyMult(id, shieldSpeed);
    }

    //shieldsChanger
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        ShieldAPI shipShield = ship.getShield();
        float radius = shipShield.getRadius();
        String innersprite;
        String outersprite;
        if (radius >= 256.0F) {
            innersprite = "graphics/fx/shield/vic_shields256.png";
            outersprite = "graphics/fx/shield/vic_shields256ring.png";
        } else if (radius >= 128.0F) {
            innersprite = "graphics/fx/shield/vic_shields128.png";
            outersprite = "graphics/fx/shield/vic_shields128ring.png";
        } else {
            innersprite = "graphics/fx/shield/vic_shields64.png";
            outersprite = "graphics/fx/shield/vic_shields64ring.png";
        }
        shipShield.setRadius(radius, innersprite, outersprite);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        if (ship.getShield() == null || ship.getShield().getType() != ShieldAPI.ShieldType.OMNI || ship.getShield().isOff()) {
            return;
        }

        float shipArcMult = 0f;
        switch (ship.getHullSpec().getHullId()) {
            case "vic_thamuz":
            case "vic_samael":
                shipArcMult = 0.5f;
                //do stuff for case 1 and 2
                break;
            default:
                break;
        }

        float shieldBaseArc = ship.getHullSpec().getShieldSpec().getArc();
        float extshield = 0f;
        if (ship.getVariant().hasHullMod("extendedshieldemitter")) extshield += 60;

        float shieldRelFacing = Math.abs(MathUtils.getShortestRotation(ship.getShield().getFacing(), ship.getFacing()));
        //float normalizedAngle = scaleArcToIdealAngle(IDEAL_ANGLE, shieldRelFacing);
        float shieldArcMult = 1 + ((shieldRelFacing / IDEAL_ANGLE) * (MAX_ARC_MULT - shipArcMult - 1f));

        ship.getShield().setArc(shieldBaseArc * shieldArcMult + extshield);
    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {

        float shipArcMult = 0f;
        switch (ship.getHullSpec().getHullId()) {
            case "vic_thamuz":
            case "vic_samael":
            case "vic_kobal":
                shipArcMult = 0.5f;
                //do stuff for case 1 and 2
                break;
            default:
                break;
        }

        if (index == 0)
            return Math.round(ship.getHullSpec().getShieldSpec().getArc() * (MAX_ARC_MULT - shipArcMult)) + "";
        if (index == 1 || index == 2) return ((shieldSpeed - 1) * 100) + "%";
        return null;
    }
}



