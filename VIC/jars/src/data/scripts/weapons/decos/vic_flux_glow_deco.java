package data.scripts.weapons.decos;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.awt.*;

public class vic_flux_glow_deco implements EveryFrameWeaponEffectPlugin {
    private static final float[] COLOR_NORMAL = {255f/255f, 140f/255f, 80f/255f};
    private static final float MAX_OPACITY = 1f;
    private static final float FADE_RATE = 2f;
    private float prevBrightness = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        ShipAPI ship = weapon.getShip();
        if (ship == null) {
            return;
        }

        //Brightness based on flux under normal conditions
        float targetBrightness = ship.getFluxTracker().getFluxLevel() *  1f;


        //Fading the brightness levels
        float currentBrightness;
        if (targetBrightness > prevBrightness) {
            currentBrightness = Math.min(prevBrightness + FADE_RATE*amount,targetBrightness);
        } else {
            currentBrightness = Math.max(prevBrightness - FADE_RATE*amount,targetBrightness);
        }
        prevBrightness = currentBrightness;

        //No glows on wrecks or in refit
        if ( ship.isPiece() || !ship.isAlive() || ship.getOriginalOwner() == -1) {
            currentBrightness = 0f;
        }

        //Switches to the proper sprite
        if (currentBrightness > 0) {
            weapon.getAnimation().setFrame(1);
        } else {
            weapon.getAnimation().setFrame(0);
        }

        //Brightness clamp, cause there's some weird cases with flux level > 1f, I guess
        currentBrightness = Math.max(0f,Math.min(currentBrightness,1f));

        //Now, set the color to the one we want, and include opacity
        Color colorToUse = new Color(COLOR_NORMAL[0], COLOR_NORMAL[1], COLOR_NORMAL[2], currentBrightness*MAX_OPACITY);


        //And finally actually apply the color
        weapon.getSprite().setColor(colorToUse);

        }
    }
