package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lazywizard.lazylib.MathUtils;

public class vic_dynamicShields extends BaseHullMod {

    public final float arcIncrease = 90f;
    public final float IDEAL_ANGLE = 90f;   // The angle of deflection at which the shield is at max arc

    public final float
            shieldSpeed = 1.5f,
            ballisticRangeMult = 0.9f,
            energyFlatRange = 100f,
            energyFluxMult = 0.9f;


    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyMult(id, ballisticRangeMult);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, energyFluxMult);

        stats.getShieldUnfoldRateMult().modifyMult(id, shieldSpeed);
        stats.getShieldTurnRateMult().modifyMult(id, shieldSpeed);


    }

    //shieldsChanger
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        ShieldAPI shipShield = ship.getShield();
        float radius = shipShield.getRadius();
        String innerSprite;
        String outerSprite;
        if (radius >= 256.0F) {
            innerSprite = "graphics/fx/shield/vic_shields256.png";
            outerSprite = "graphics/fx/shield/vic_shields256ring.png";
        } else if (radius >= 128.0F) {
            innerSprite = "graphics/fx/shield/vic_shields128.png";
            outerSprite = "graphics/fx/shield/vic_shields128ring.png";
        } else {
            innerSprite = "graphics/fx/shield/vic_shields64.png";
            outerSprite = "graphics/fx/shield/vic_shields64ring.png";
        }
        shipShield.setRadius(radius, innerSprite, outerSprite);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        ship.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat("vic_dynamicShields", energyFlatRange * (1 + ship.getMutableStats().getEnergyWeaponRangeBonus().getPercentMod() * 0.01f));
        ship.getMutableStats().getBeamWeaponRangeBonus().modifyFlat("vic_dynamicShields", -energyFlatRange * (1 + ship.getMutableStats().getEnergyWeaponRangeBonus().getPercentMod() * 0.01f));

        if (ship.getShield() == null || ship.getShield().getType() != ShieldAPI.ShieldType.OMNI || ship.getShield().isOff()) {
            return;
        }

        float baseShieldArc =
                (ship.getHullSpec().getShieldSpec().getArc() *
                        (ship.getMutableStats().getShieldArcBonus().getPercentMod() * 0.01f + 1)  +
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
        if (index == 3) return Math.round(energyFlatRange) + "";
        if (index == 4) return Math.round(energyFlatRange) + "";
        if (index == 5) return Math.round((1 - energyFluxMult) * 100) + "%";
        return null;
    }
}



