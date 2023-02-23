package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.plugins.vic_combatPlugin;
import data.scripts.plugins.vic_weaponDamageListener;
import data.scripts.utilities.vic_graphicLibEffects;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_vila_script implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 180f;
    final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.8f;
    final Color CHARGEUP_PARTICLE_COLOR = new Color(114, 221, 255, 200);
    final float CHARGEUP_PARTICLE_COUNT_FACTOR = 15f;
    final float CHARGEUP_PARTICLE_DISTANCE_MAX = 50f;
    final float CHARGEUP_PARTICLE_DISTANCE_MIN = 10f;
    final float CHARGEUP_PARTICLE_DURATION = 0.2f;
    final float CHARGEUP_PARTICLE_SIZE_MAX = 8f;
    final float CHARGEUP_PARTICLE_SIZE_MIN = 4f;
    final Color MUZZLE_FLASH_COLOR = new Color(0, 225, 255, 200);
    final float MUZZLE_FLASH_DURATION = 0.15f;
    final float MUZZLE_FLASH_SIZE = 50.0f;
    final float MUZZLE_OFFSET_HARDPOINT = 8.5f;
    final float MUZZLE_OFFSET_TURRET = 10.0f;

    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
    private float lastChargeLevel = 0.0f;
    private int lastWeaponAmmo = 1;
    private boolean shot = false;
    private boolean doOnce = true;


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        new Color(255, 140, 0, 200);

        float chargeLevel = weapon.getChargeLevel();
        int weaponAmmo = weapon.getAmmo();

        if (doOnce) {
            if (!weapon.getShip().hasListenerOfClass(vic_weaponDamageListener.class)){
                weapon.getShip().addListener(new vic_weaponDamageListener());
            }
            doOnce = false;
        }

        if ((chargeLevel > lastChargeLevel) || (weaponAmmo < lastWeaponAmmo)) {
            Vector2f weaponLocation = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            float shipFacing = weapon.getCurrAngle();
            Vector2f shipVelocity = ship.getVelocity();
            Vector2f muzzleLocation = MathUtils.getPointOnCircumference(weaponLocation,
                    weapon.getSlot().isHardpoint() ? MUZZLE_OFFSET_HARDPOINT : MUZZLE_OFFSET_TURRET, shipFacing);

            interval.advance(amount);

            if (interval.intervalElapsed() && weapon.isFiring()) {
                int particleCount = (int) (CHARGEUP_PARTICLE_COUNT_FACTOR * chargeLevel);
                float distance, size, angle, speed;
                Vector2f particleVelocity;
                for (int i = 0; i < particleCount; ++i) {
                    distance = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_DISTANCE_MIN, CHARGEUP_PARTICLE_DISTANCE_MAX);
                    size = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_SIZE_MIN, CHARGEUP_PARTICLE_SIZE_MAX);
                    angle = MathUtils.getRandomNumberInRange(-0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD, 0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD);
                    Vector2f spawnLocation = MathUtils.getPointOnCircumference(muzzleLocation, distance, (angle + shipFacing));
                    float duration = CHARGEUP_PARTICLE_DURATION * MathUtils.getRandomNumberInRange(0.5f, 1.5f);
                    speed = distance / duration;
                    particleVelocity = MathUtils.getPointOnCircumference(shipVelocity, speed, 180.0f + angle + shipFacing);
                    engine.addHitParticle(spawnLocation, particleVelocity, size, CHARGEUP_PARTICLE_BRIGHTNESS * weapon.getChargeLevel(),
                            duration, CHARGEUP_PARTICLE_COLOR);
                }
            }

            if (weaponAmmo < lastWeaponAmmo) {
                engine.spawnExplosion(muzzleLocation, shipVelocity, MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE, MUZZLE_FLASH_DURATION);
                engine.addSmoothParticle(muzzleLocation, shipVelocity, MUZZLE_FLASH_SIZE * 3f, 1f, MUZZLE_FLASH_DURATION * 2f, MUZZLE_FLASH_COLOR);

                if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
                    vic_graphicLibEffects.CustomRippleDistortion(
                            muzzleLocation,
                            shipVelocity,
                            MUZZLE_FLASH_SIZE,
                            MUZZLE_FLASH_SIZE * 0.1f,
                            false,
                            0,
                            360,
                            1f,
                            0.1f,
                            0.25f,
                            0.15f,
                            MUZZLE_FLASH_DURATION * 2f,
                            0f
                    );
                }
                /*
                RippleDistortion ripple = new RippleDistortion(muzzleLocation, ship.getVelocity());
                ripple.setSize(MUZZLE_FLASH_SIZE);
                ripple.setIntensity(MUZZLE_FLASH_SIZE * 0.1f);
                ripple.setFrameRate(60f / (MUZZLE_FLASH_DURATION * 2f));
                ripple.fadeInSize(MUZZLE_FLASH_DURATION * 2f);
                ripple.fadeOutIntensity(MUZZLE_FLASH_DURATION * 2f);
                DistortionShader.addDistortion(ripple);

                 */

            }
        }

        lastChargeLevel = chargeLevel;
        lastWeaponAmmo = weaponAmmo;
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (weapon == null || weapon.getShip() == null || weapon.getShip().getWeaponGroupFor(weapon) == null || weapon.getShip().getWeaponGroupFor(weapon).getAutofirePlugin(weapon) == null) return;
        final float startingSpeed = MathUtils.getRandomNumberInRange(0.25f, 0.5f);
        final float rangeBeforeCurving = MathUtils.getRandomNumberInRange(0.25f, 0.5f);

        Vector2f target = weapon.getShip().getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTarget();
        projectile.getVelocity().scale(startingSpeed);
        float flightTime = weapon.getRange() / projectile.getMoveSpeed();

        if (target == null && weapon.getShip() == engine.getPlayerShip() && weapon.getShip().getAI() == null) {
            //engine.addHitParticle(mousePosition, new Vector2f(), 20, 1, 1.5f, new Color(1f, 0.3f, 0f));
            Vector2f mousePosition = CombatUtils.toWorldCoordinates(new Vector2f(Mouse.getX(), Mouse.getY()));
            float rangeFromMouse = MathUtils.getDistance(weapon.getLocation(), mousePosition);
            rangeFromMouse = MathUtils.clamp(rangeFromMouse, weapon.getRange() * 0.5f, weapon.getRange());
            target = MathUtils.getPoint(weapon.getLocation(), rangeFromMouse, weapon.getCurrAngle());
        }


        if (target == null) {
            target = MathUtils.getPoint(weapon.getLocation(), weapon.getRange(), weapon.getCurrAngle());
        }
        target = new Vector2f(target.x + weapon.getShip().getVelocity().x * flightTime, target.y + weapon.getShip().getVelocity().y * flightTime);
        projectile.setFacing(VectorUtils.getFacing(projectile.getVelocity()));
        //engine.addHitParticle(target, new Vector2f(), 20, 1, 1.5f, new Color(0.3f, 1f, 0f));
        vic_combatPlugin.AddArcaneMissiles(projectile, target, flightTime * rangeBeforeCurving, rangeBeforeCurving, startingSpeed);
    }

    public static class vic_vilaListner implements DamageDealtModifier {

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (param instanceof DamagingProjectileAPI) {
                if (((DamagingProjectileAPI) param).getProjectileSpecId().equals("vic_arcaneBolt")) {
                    if (target.getOwner() == ((DamagingProjectileAPI) param).getOwner()) {
                        damage.getModifier().modifyMult("vic_damage", 0);
                        return "vic_damage";
                    }
                }
            }
            return null;
        }
    }
}
