//By Tartiflette modified by PureTilt
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.ArrayList;
import java.util.List;

public class vic_zlydzenScript implements EveryFrameWeaponEffectPlugin {


    //animation values
    private float delay = 0.1f;
    private float timer = 0;
    private float SPINUP = 2f;
    private float SPINDOWN = 5f;

    //dont touch
    private boolean runOnce = false;
    private boolean hidden = false;
    private AnimationAPI theAnim;
    private int maxFrame;
    private int frame;




    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) {
            return;
        }

        if (!runOnce) {
            runOnce = true;
            if (weapon.getSlot().isHidden()) {
                hidden = true;
            } else {
                theAnim = weapon.getAnimation();
                maxFrame = theAnim.getNumFrames();
                frame = MathUtils.getRandomNumberInRange(0, maxFrame - 1);
            }
        }


        timer += amount;
        if (timer >= delay) {
            timer -= delay;
            if (weapon.getChargeLevel() > 0) {
                delay = Math.max(
                        delay - SPINUP,
                        0.02f
                );
            } else {
                delay = Math.min(
                        delay + delay / SPINDOWN,
                        0.1f
                );
            }
            if (!hidden && delay != 0.1f) {
                frame++;
                if (frame == maxFrame) {
                    frame = 0;
                }
            }
        }
        if (weapon.getChargeLevel() > 0) {

            Global.getSoundPlayer().playLoop(
                    "vic_zlydzen_loop",
                    weapon,
                    1f,
                    Math.max(0, 10 * weapon.getChargeLevel() - 9),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );

            Global.getSoundPlayer().playLoop(
                    "vic_zlydzen_spin",
                    weapon,
                    0.25f + weapon.getChargeLevel(),
                    0.15f,
//                    0.5f+0.5f*weapon.getChargeLevel(),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );
        }

        if (!hidden) {
            theAnim.setFrame(frame);
        }

    }
}
