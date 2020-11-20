package data.scripts;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

//Made by PureTilt for Astarat
public class VIC_EngineGlowScript implements EveryFrameWeaponEffectPlugin {

    private final Color GlowColor = new Color (0, 255, 255, 255);
    private final float[] COLOR_NORMAL = {0f / 255f, 255f / 255f, 255f / 255f};
    private final float MAX_OPACITY = 1f;
    private boolean runOnce = false;
    private ShipAPI SHIP;
    private float currentBrightness = 0.5f;
    private float timeToChange = 0.5f;
    private ShipEngineControllerAPI.ShipEngineAPI thruster;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        //some initial setup
        if (!this.runOnce) {
            runOnce = true;
            SHIP = weapon.getShip();
            for (ShipEngineControllerAPI.ShipEngineAPI e : SHIP.getEngineController().getShipEngines()) {
                if (MathUtils.isWithinRange(e.getLocation(), weapon.getLocation(), 4)) {
                    thruster = e;
                }
            }
        }

        //default brightness is idle
        float targetBrightness = 0.5f;

        //change brightness depending on current actions
        if (SHIP.getEngineController().isAccelerating() || SHIP.getEngineController().isAcceleratingBackwards()) {
            targetBrightness = 1f;
        } else if (SHIP.getEngineController().isTurningLeft() || SHIP.getEngineController().isTurningRight()) {
            targetBrightness = 0.75f;
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
        if (SHIP.isHulk() || SHIP.isPhased()){
            currentBrightness = 0f;
        }

        //make color and apply it to sprite
        //Color colorToUse = new Color(COLOR_NORMAL[0], COLOR_NORMAL[1], COLOR_NORMAL[2], currentBrightness * MAX_OPACITY);
        int Red = thruster.getEngineColor().getRed();
        int Green = thruster.getEngineColor().getGreen();
        int Blue = thruster.getEngineColor().getBlue();
        if (SHIP.getVariant().hasHullMod("safetyoverrides")){
            Red = Math.round((Red * 0.8f) + (255 * 0.2f)) - 1;
            Green = Math.round((Green * 0.8f) + (100 * 0.2f)) - 1;
            Blue = Math.round((Blue * 0.8f) + (255 * 0.2f)) - 1;
        }
        Color colorToUse = new Color(Red, Green, Blue, Math.round(currentBrightness * 255));
        weapon.getSprite().setColor(colorToUse);
    }
}