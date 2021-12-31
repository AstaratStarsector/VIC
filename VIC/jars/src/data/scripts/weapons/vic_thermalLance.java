package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lwjgl.util.vector.Vector2f;

public class vic_thermalLance implements EveryFrameWeaponEffectPlugin {

    private boolean fired = false;


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) {
            return;
        }

        if (weapon.isFiring()) {
            if (!fired) {
                Global.getSoundPlayer().playSound("vic_rubezahl_shot", 1, 1f, weapon.getShip().getLocation(), new Vector2f(0, 0));
                fired = true;
            }
        } else {
            fired = false;
        }

        if (engine.isPaused()) {
            return;
        }

        if (!(weapon.getSlot().isHidden())) {
			AnimationAPI anim = weapon.getAnimation();
			if (weapon.getShip().isHulk()) {
				anim.setFrame(0);
				return;
			}
            if (weapon.getChargeLevel() > 0.9f) {
                anim.setFrame(1);
            } else {
                anim.setFrame(0);
            }
        }
    }
}


