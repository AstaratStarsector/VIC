package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

// Written by AxleMC131 for Astarat
public class vic_dynamicShields extends BaseHullMod {

    public final float arcIncrease = 90f;
    public final float IDEAL_ANGLE = 90f;   // The angle of deflection at which the shield is at max arc

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
        float baseShieldArc =
                (ship.getHullSpec().getShieldSpec().getArc() +
                        ship.getMutableStats().getShieldArcBonus().getFlatBonus()) *
                        ship.getMutableStats().getShieldArcBonus().getMult() *
                        (ship.getMutableStats().getShieldArcBonus().getPercentMod() + 1);

        float shieldRelFacing = Math.abs(MathUtils.getShortestRotation(ship.getShield().getFacing(), ship.getFacing()));
        if (shieldRelFacing > 90) shieldRelFacing = 180 - shieldRelFacing;
        //float normalizedAngle = scaleArcToIdealAngle(IDEAL_ANGLE, shieldRelFacing);
        float shieldArcMult = (shieldRelFacing / IDEAL_ANGLE);

        ship.getShield().setArc(baseShieldArc + (arcIncrease * shieldArcMult));
    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {

        float baseShieldArc =
                (ship.getHullSpec().getShieldSpec().getArc() +
                        ship.getMutableStats().getShieldArcBonus().getFlatBonus()) *
                        ship.getMutableStats().getShieldArcBonus().getMult() *
                        (ship.getMutableStats().getShieldArcBonus().getPercentMod() + 1);

        if (index == 0)
            return Math.round(baseShieldArc + arcIncrease) + "";
        if (index == 1 || index == 2) return Math.round((shieldSpeed - 1) * 100) + "%";
        return null;
    }
}



