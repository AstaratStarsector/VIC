package data.scripts.weapons.decos;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MagicAnim;

import java.awt.*;

public class vic_oriaxSystemGlow implements EveryFrameWeaponEffectPlugin {

    public AnimationAPI animation;
    public boolean DoOnce = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (DoOnce){
            animation = weapon.getAnimation();
        }
        Color alpha = new Color(255, 255, 255, 0);
        if (weapon.getShip() != null && weapon.getShip().getOwner() != -1) {
            animation.setFrame(1);
            alpha = new Color(255, 255, 255, Math.round(255 * MagicAnim.smooth(weapon.getShip().getSystem().getEffectLevel())));
        }
        weapon.getSprite().setColor(alpha);
    }
}
