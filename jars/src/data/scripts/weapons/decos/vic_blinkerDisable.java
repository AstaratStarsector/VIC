package data.scripts.weapons.decos;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.utilities.vic_graphicLibEffects;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_blinkerDisable implements EveryFrameWeaponEffectPlugin {

    public AnimationAPI animation;
    public boolean DoOnce = true;
    public boolean alt = false;
    public boolean red = false;
    public float frameCounter = 0;
    public float neededCount = 0;

    public float redCount = 20;
    public float cyanCount = 30;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (DoOnce){
            animation = weapon.getAnimation();
            alt = weapon.getSlot().isHardpoint();
            red = weapon.getId().equals("vic_blonkerRed");
            neededCount = (red ? redCount : cyanCount) - (alt ? 6 : 0);
            DoOnce = false;
        }
        if (alt && animation.getFrame() == 4) animation.setFrame(8);
        if (animation.getFrame() == 10){
            if (frameCounter < neededCount){
                frameCounter ++;
                animation.setFrame(9);
            } else {
                frameCounter = 0;
                animation.setFrame(0);
            }
        }
        /*
        Color lightColor = red ? new Color(255, 0, 0,255) : new Color(0, 255, 236,255);
        if (animation.getFrame() == 1){
            vic_graphicLibEffects.customLight(weapon.getLocation(), null, 15f,red ? 0.1f:0.05f,lightColor,0f, 0f, 0.1f);
        }
        if (animation.getFrame() == 4 || animation.getFrame() == 6){
            vic_graphicLibEffects.customLight(weapon.getLocation(), null, 25f,red ? 0.2f:0.1f ,lightColor,0f, 0f, 0.2f);
        }
        
         */
        //weapon.getSprite().setAdditiveBlend();
        if (weapon.getShip() != null && weapon.getShip().getOwner() == -1) return;
        if (weapon.getShip() != null && weapon.getShip().isAlive())return;
        weapon.getAnimation().setAlphaMult(0f);
    }
}
