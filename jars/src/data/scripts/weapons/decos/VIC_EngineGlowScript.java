package data.scripts.weapons.decos;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

//Made by PureTilt for Astarat
public class VIC_EngineGlowScript implements EveryFrameWeaponEffectPlugin {

    boolean runOnce = false;
    ShipAPI ship;
    float currentBrightness = 0.5f;
    final float timeToChange = 0.75f;
    ShipEngineControllerAPI.ShipEngineAPI thruster;
    Color engineColor;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        //some initial setup
        if (!this.runOnce) {
            runOnce = true;
            ship = weapon.getShip();
            for (ShipEngineControllerAPI.ShipEngineAPI e : ship.getEngineController().getShipEngines()) {
                if (MathUtils.isWithinRange(e.getLocation(), weapon.getLocation(), 6)) {
                    thruster = e;
                    break;
                }
            }
            if (thruster != null) engineColor = thruster.getEngineColor();
        }

        //default brightness is idle
        float targetBrightness = 0.4f;

        //change brightness depending on current actions
        if (ship.getEngineController().isAccelerating() || ship.getEngineController().isAcceleratingBackwards()) {
            targetBrightness = 1f;
        } else if (ship.getEngineController().isTurningLeft() || ship.getEngineController().isTurningRight() || ship.getEngineController().isStrafingLeft() || ship.getEngineController().isStrafingRight()) {
            targetBrightness = 0.75f;
        } else if (ship.getEngineController().isDecelerating()){
            targetBrightness = 0.6f;
        }

        //smooth glow change
        if (currentBrightness > targetBrightness) {
            currentBrightness -= amount / timeToChange;
            if (currentBrightness < targetBrightness)
                currentBrightness = targetBrightness;
        } else if (currentBrightness < targetBrightness) {
            currentBrightness += amount / timeToChange;
            if (currentBrightness > targetBrightness)
                currentBrightness = targetBrightness;
        }

        //set glow to 0 if flame out
        if (thruster.isDisabled()) {
            currentBrightness = 0f;
        }
        if (ship.isHulk() || ship.isPhased()) {
            currentBrightness = 0f;
        }

        //make color and apply it to sprite
        //Color colorToUse = new Color(COLOR_NORMAL[0], COLOR_NORMAL[1], COLOR_NORMAL[2], currentBrightness * MAX_OPACITY);
        Color shift = ship.getEngineController().getFlameColorShifter().getCurr();
        float ratio = shift.getAlpha() / 255f;
        int Red = Math.min(255, Math.round(engineColor.getRed() * (1f - ratio) + shift.getRed() * ratio));
        int Green = Math.min(255, Math.round(engineColor.getGreen() * (1f - ratio) + shift.getGreen() * ratio));
        int Blue = Math.min(255, Math.round(engineColor.getBlue() * (1f - ratio) + shift.getBlue() * ratio));
        /*
        if (ship.getVariant().hasHullMod("safetyoverrides")) {
            Red = Math.round((Red * 0.8f) + (255 * 0.2f)) - 1;
            Green = Math.round((Green * 0.8f) + (100 * 0.2f)) - 1;
            Blue = Math.round((Blue * 0.8f) + (255 * 0.2f)) - 1;
        }
         */
        Color colorToUse = new Color(Red, Green, Blue, Math.round(currentBrightness * 255));
        weapon.getSprite().setColor(colorToUse);
    }
}