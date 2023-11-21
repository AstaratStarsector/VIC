package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.weapons.decos.vic_decoEnginesController;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_dynamicShields extends BaseHullMod {

    public final float arcIncrease = 90f;
    public final float IDEAL_ANGLE = 90f;   // The angle of deflection at which the shield is at max arc

    public final float
            shieldSpeed = 1.5f,
            ballisticRangeMult = 0.9f,
            ballisticRoFBonus = 15f,
            energyFlatRange = 100f,
            energyFluxMult = 0.9f;


    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyMult(id, ballisticRangeMult);
        stats.getBallisticRoFMult().modifyPercent(id, ballisticRoFBonus);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, energyFluxMult);

        stats.getShieldUnfoldRateMult().modifyMult(id, shieldSpeed);
        stats.getShieldTurnRateMult().modifyMult(id, shieldSpeed);
    }

    //shieldsChanger
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        //shield stuff
        super.applyEffectsAfterShipCreation(ship, id);
        ShieldAPI shipShield = ship.getShield();
        float radius = shipShield.getRadius();
        String innerSprite;
        String outerSprite;
        if (radius >= 256.0F) {
            innerSprite = "graphics/fx/shield/vic_shields256.png";
            outerSprite = "graphics/fx/shields256ring.png";
        } else if (radius >= 128.0F) {
            innerSprite = "graphics/fx/shield/vic_shields128.png";
            outerSprite = "graphics/fx/shields128ringc.png";
        } else {
            innerSprite = "graphics/fx/shield/vic_shields64.png";
            outerSprite = "graphics/fx/shields64ringd.png";
        }
        shipShield.setRadius(radius, innerSprite, outerSprite);
        //range stuff
        ship.addListener(new vic_laidlawTechRangeMod());
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getShield() == null || ship.getShield().getType() != ShieldAPI.ShieldType.OMNI || ship.getShield().isOff()) {
            return;
        }

        float baseShieldArc =
                (ship.getHullSpec().getShieldSpec().getArc() *
                        (ship.getMutableStats().getShieldArcBonus().getPercentMod() * 0.01f + 1) +
                        ship.getMutableStats().getShieldArcBonus().getFlatBonus()) *
                        ship.getMutableStats().getShieldArcBonus().getMult();

        float shieldRelFacing = Math.abs(MathUtils.getShortestRotation(ship.getShield().getFacing(), ship.getFacing()));
        if (shieldRelFacing > 90) shieldRelFacing = 180 - shieldRelFacing;
        //float normalizedAngle = scaleArcToIdealAngle(IDEAL_ANGLE, shieldRelFacing);
        float shieldArcMult = (shieldRelFacing / IDEAL_ANGLE);

        ship.getShield().setArc(Math.min(baseShieldArc + (arcIncrease * shieldArcMult), 360));
    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return Math.round(arcIncrease) + "";
        if (index == 1) return Math.round((shieldSpeed - 1) * 100) + "%";
        if (index == 2) return Math.round((1 - ballisticRangeMult) * 100) + "%";
        if (index == 3) return Math.round(ballisticRoFBonus) + "%";
        if (index == 4) return Math.round(energyFlatRange) + "";
        if (index == 5) return Math.round((1 - energyFluxMult) * 100) + "%";
        return null;
    }

    public static class vic_laidlawTechRangeMod implements WeaponBaseRangeModifier {

        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0;
        }

        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }

        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (weapon.getSlot() == null) {
                return 0f;
            }
            boolean unsuitableForBonus = !(weapon.getType() == WeaponAPI.WeaponType.ENERGY || weapon.getType() == WeaponAPI.WeaponType.HYBRID) || weapon.isBeam();
            if (unsuitableForBonus) {
                return 0f;
            }
            return 100f;
        }
    }

}



