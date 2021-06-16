package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

public class vic_zlydzen_script implements EveryFrameWeaponEffectPlugin {


    //animation values
    private final float delay = 0.025f;
    private float timer = 0;

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

        if (!hidden) {
            float animationSpeed = 0;
            if (weapon.getChargeLevel() != 0) animationSpeed += 10;
            animationSpeed += 30 * weapon.getChargeLevel();
            theAnim.setFrameRate(animationSpeed);
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
                    0.1f,
//                    0.5f+0.5f*weapon.getChargeLevel(),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );
        }



    }
}
