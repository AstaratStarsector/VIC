package data.scripts;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

//Made by PureTilt for Astarat
public class VIC_EngineGlowScript implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce = false;
    private ShipAPI SHIP;
    private float currentBrightness = 0.5f;
    private float timeToChange = 0.25f;

    //private static final Color GlowColor = new Color (0, 255, 255, 255);
    private static final float[] COLOR_NORMAL = {0f/255f, 255f/255f, 255f/255f};
    private static final float MAX_OPACITY = 1f;

    private ShipEngineControllerAPI.ShipEngineAPI thruster;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        //some initial setup
        if (!this.runOnce) {
            runOnce = true;
            SHIP = weapon.getShip();
            for(ShipEngineControllerAPI.ShipEngineAPI e : SHIP.getEngineController().getShipEngines()){
                if(MathUtils.isWithinRange(e.getLocation(), weapon.getLocation(), 2)){
                    thruster=e;
                }
            }
        }

        //default brightness is idle
        float targetBrightness = 0.5f;

        //change brightness depending on current actions
        if (SHIP.getEngineController().isAccelerating() || SHIP.getEngineController().isAcceleratingBackwards()){
            targetBrightness = 1f;
        } else if (SHIP.getEngineController().isTurningLeft() || SHIP.getEngineController().isTurningRight()){
            targetBrightness = 0.75f;
        }

        //smooth glow change
        if (currentBrightness > targetBrightness){
            currentBrightness -= amount/timeToChange;
            if (currentBrightness < targetBrightness)
                currentBrightness = targetBrightness;
        } else if (currentBrightness < targetBrightness){
            currentBrightness += amount/timeToChange;
            if (currentBrightness > targetBrightness)
                currentBrightness = targetBrightness;
        }

        //set glow to 0 if flame out
        if (thruster.isDisabled()){
            currentBrightness = 0f;
        }

        //make color and apply it to sprite
        Color colorToUse =  new Color(COLOR_NORMAL[0], COLOR_NORMAL[1], COLOR_NORMAL[2], currentBrightness *MAX_OPACITY);
        weapon.getSprite().setColor(colorToUse);
    }
}