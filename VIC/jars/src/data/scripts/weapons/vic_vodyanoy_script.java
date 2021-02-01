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

public class vic_vodyanoy_script implements EveryFrameWeaponEffectPlugin {

    private final List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();
    private final IntervalUtil
            checkTime = new IntervalUtil(0.5f, 0.5f);
    //animation values
    private float delay = 0.1f;
    private float timer = 0;
    private float SPINUP = 0.02f;
    private float SPINDOWN = 10f;

    //dont touch
    private boolean runOnce = false;
    private boolean hidden = false;
    private AnimationAPI theAnim;
    private int maxFrame;
    private int frame;
    private float
            firingTime = 0f,
            heat = 0f,
            currentScore = 0f;


    //over heat stuff
    private final float
            timeToStartHeating = 1.5f, //can be 1/x where x is time to rump up
            heatGeneration = 1 / 6f, //can be 1/x where x is time to rump up
            heatFallOffSpeed = 0.25f, //can be 1/x where x is time to cooldown
            scorePerProj = 1 / 8f, // 1/x where every xTh proj replaced
            additionalScore = 1 / 5f - 1 / 8f; // 1/x where every xTh proj replaced

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
            SPINUP = 0.03f;
            SPINDOWN = 7.5f;
        }

        if (weapon.getChargeLevel() >= 1) {

            firingTime += amount;
            if (firingTime > timeToStartHeating) firingTime = timeToStartHeating;

            if (firingTime >= timeToStartHeating) {
                heat += heatGeneration * amount;
                if (heat > 1) heat = 1;
            }

        } else {

            firingTime = 0;
            heat -= heatFallOffSpeed * amount;
            if (heat < 0) heat = 0;

        }

        checkTime.advance(amount);
        if (checkTime.intervalElapsed()) {
            List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
            for (DamagingProjectileAPI proj : cloneList) {
                if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                    alreadyRegisteredProjectiles.remove(proj);
                }
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {

                if (firingTime >= timeToStartHeating) currentScore += scorePerProj + (additionalScore * heat);
                if (currentScore >= 1) {
                    currentScore--;
                    DamagingProjectileAPI spawnedProj = (DamagingProjectileAPI) engine.spawnProjectile(
                            weapon.getShip(),
                            weapon,
                            "vic_vodyanoy_sub",
                            proj.getLocation(),
                            proj.getFacing(),
                            weapon.getShip().getVelocity());
                    alreadyRegisteredProjectiles.add(spawnedProj);
                    engine.removeEntity(proj);
                } else {
                    proj.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));
                    alreadyRegisteredProjectiles.add(proj);
                }
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
                    "vic_vodanoy_shoting",
                    weapon,
                    1f,
                    Math.max(0, 10 * weapon.getChargeLevel() - 9),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );

            Global.getSoundPlayer().playLoop(
                    "vic_vodanoy_spin",
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
