//By Tartiflette modified by PureTilt
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class vic_vodyanoy_script implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    private final IntervalUtil
            checkTime = new IntervalUtil(0.5f, 0.5f);
    //animation values
    float delay = 0.1f;
    float timer = 0;
    final float SPINUP = 0.03f;
    final float SPINDOWN = 7.5f;

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

    final Color
            noHeat = new Color(170, 245, 255, 255),
            maxHeat = new Color(253, 137, 137, 255);
    Color a = new Color(255, 77, 22, 255);


    //overheat stuff
    float
            timeToStartHeating = 0f, //can be 1/x where x is time to starting gaining heat and replacing projes
            heatGeneration = 1f / 5f, //can be 1/x where x is time to rump up
            heatFallOffSpeed = 1f / 4f, //can be 1/x where x is time to cooldown
            heatThreshold = 0.2f, //at what heat % start replacing projes
            scorePerProj = 0f, // 1/x where every xTh proj replaced with no heat
            additionalScore = 1f; // 1/x where every xTh proj replaced

    {
        additionalScore -= scorePerProj;
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) {
            return;
        }

        //engine.maintainStatusForPlayerShip("heat", null, "heat", Math.round(heat * 100f) / 100f + "/" + Math.round(currentScore * 100f) / 100f , false);

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


        //change flash colour based on heat
        float heatRevers = 1 - heat;
        Color mix = new Color(limit(noHeat.getRed() * heatRevers + maxHeat.getRed() * heat),
                limit(noHeat.getGreen() * heatRevers + maxHeat.getGreen() * heat),
                limit(noHeat.getBlue() * heatRevers + maxHeat.getBlue() * heat),
                255);
        weapon.getMuzzleFlashSpec().setParticleColor(mix);

        /*
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
         */

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
                    1f + heat * 0.25f,
                    Math.max(0, 10 * weapon.getChargeLevel() - 9),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );

            Global.getSoundPlayer().playLoop(
                    "vic_vodanoy_spin",
                    weapon,
                    0.25f + weapon.getChargeLevel() + heat * 0.25f,
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

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (firingTime >= timeToStartHeating && heat >= heatThreshold)
            currentScore += scorePerProj + (additionalScore * heat);
        if (currentScore >= 1) {
            currentScore--;
            DamagingProjectileAPI spawnedProj = (DamagingProjectileAPI) engine.spawnProjectile(
                    weapon.getShip(),
                    weapon,
                    "vic_vodyanoy_sub",
                    proj.getLocation(),
                    proj.getFacing(),
                    weapon.getShip().getVelocity());
            spawnedProj.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));
            engine.removeEntity(proj);
        } else {
            proj.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));
        }
    }

    public int limit(float value) {
        return Math.max(0, Math.min(Math.round(value), 255));
    }
}
