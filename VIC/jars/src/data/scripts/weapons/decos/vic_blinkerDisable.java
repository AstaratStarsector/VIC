package data.scripts.weapons.decos;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.awt.*;

public class vic_blinkerDisable implements EveryFrameWeaponEffectPlugin {

    public AnimationAPI animation;
    public boolean DoOnce = true;
    public boolean alt = false;
    public boolean red = false;
    public float frameCounter = 0;
    public float neededCount = 0;

    public float redCount = 30;
    public float cyanCount = 42;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (DoOnce){
            animation = weapon.getAnimation();
            alt = weapon.getSlot().isHardpoint();
            red = weapon.getId().equals("vic_blonkerRed");
            neededCount = (red ? redCount : cyanCount);
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
        //weapon.getSprite().setAdditiveBlend();
        if (weapon.getShip() != null && weapon.getShip().getOwner() == -1) return;
        if (weapon.getShip() != null && weapon.getShip().isAlive())return;
        weapon.getAnimation().setAlphaMult(0f);
    }
}
