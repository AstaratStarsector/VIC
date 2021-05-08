package data.scripts.weapons.decos;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class vic_blinkerDisable implements EveryFrameWeaponEffectPlugin {

    public AnimationAPI animation;
    public boolean DoOnce = true;
    public boolean alt = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (DoOnce){
            animation = weapon.getAnimation();
            alt = weapon.getSlot().isHardpoint();
        }
        if (alt && animation.getFrame() == 4) animation.setFrame(8);
        //weapon.getSprite().setAdditiveBlend();
        if (weapon.getShip() != null && weapon.getShip().getOwner() == -1) return;
        if (weapon.getShip() != null && weapon.getShip().isAlive())return;
        weapon.getAnimation().setAlphaMult(0f);
    }
}
