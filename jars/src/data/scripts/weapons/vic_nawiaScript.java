package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.vic_combatPlugin;
import org.magiclib.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;


public class vic_nawiaScript implements OnHitEffectPlugin, OnFireEffectPlugin {

    final float MUZZLE_OFFSET_HARDPOINT = 8.5f;
    final float MUZZLE_OFFSET_TURRET = 10.0f;

    private final ArrayList<SpriteAPI> ringList = new ArrayList<>();

    {
        ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring1"));
        ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring2"));
    }

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if (!projectile.isFading()) {
            vic_combatPlugin.AddNawiaRain(point, target.getVelocity(), projectile.getSource(), projectile);
        }

        //if (!(target instanceof ShipAPI)) return;
        //if (projectile.isFading()) return;
//        for (int i = 0; i < 4; i++) {
//
//            float toDaysRandom = MathUtils.getRandomNumberInRange(-15, 15);
//            float toDaysRandom2 = MathUtils.getRandomNumberInRange(0.8f, 1.2f);
//            Vector2f Dir = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing() + toDaysRandom);
//            Vector2f SpawnPoint = new Vector2f(point.x + Dir.x * -300 * toDaysRandom2, point.y + Dir.y * -300 * toDaysRandom2);
//
//            DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(projectile.getSource(),
//                    projectile.getWeapon(),
//                    "vic_nawia_sub",
//                    SpawnPoint,
//                    projectile.getFacing() + (toDaysRandom + MathUtils.getRandomNumberInRange(-5, 5)),
//                    target.getVelocity());
//
//            if (projectile.isFading()) {
//                proj.setDamageAmount(projectile.getBaseDamageAmount() * 0.125f);
//            } else {
//                proj.setDamageAmount(projectile.getBaseDamageAmount() * 0.25f);
//            }
//
//            //if (MagicRender.screenCheck (0.5f, SpawnPoint)) engine.addPlugin(new vic_nawiaVisuals(SpawnPoint, proj.getFacing()));
//            if (MagicRender.screenCheck (0.5f, SpawnPoint)) vic_combatPlugin.AddNawiaFX(SpawnPoint, proj.getFacing());
//            if (MagicRender.screenCheck(0.5f, SpawnPoint)) {
//
//                float animTime = MathUtils.getRandomNumberInRange(0.5f, 0.6f);
//
//                engine.addSmoothParticle(
//                        SpawnPoint,
//                        new Vector2f(),
//                        MathUtils.getRandomNumberInRange(20, 30),
//                        1,
//                        animTime,
//                        new Color(MathUtils.getRandomNumberInRange(130, 180), MathUtils.getRandomNumberInRange(20, 60), 255, 255)
//                );
//                for (float I = 0; I < MathUtils.getRandomNumberInRange(4, 8); I++) {
//
//                    Vector2f move = Misc.getUnitVectorAtDegreeAngle(MathUtils.getRandomNumberInRange(-20, 20) + proj.getFacing());
//                    engine.addHitParticle(
//                            SpawnPoint,
//                            new Vector2f(move.x * MathUtils.getRandomNumberInRange(25, 125), move.y * MathUtils.getRandomNumberInRange(25, 125)),
//                            MathUtils.getRandomNumberInRange(5, 20),
//                            MathUtils.getRandomNumberInRange(0.8f, 1f),
//                            animTime * MathUtils.getRandomNumberInRange(0.5f, 1.5f),
//                            new Color(MathUtils.getRandomNumberInRange(90, 180), MathUtils.getRandomNumberInRange(20, 100), 255, 255)
//                    );
//
//                }
//
//                /*
//                SpriteAPI ring1 = ringList.get(MathUtils.getRandomNumberInRange(0, ringList.size() - 1));
//
//                float ring1_grow = MathUtils.getRandomNumberInRange(12, 16);
//                float ring1_Size = MathUtils.getRandomNumberInRange(10, 20);
//                float ring1_RotationSpeed = MathUtils.getRandomNumberInRange(200f, 300f);
//                float ring1_Angle = MathUtils.getRandomNumberInRange(-45f, 45f);
//
//                MagicRender.battlespace(
//                        ring1,
//                        SpawnPoint,
//                        new Vector2f(),
//                        new Vector2f(ring1_Size, ring1_Size),
//                        new Vector2f(-ring1_grow, -ring1_grow),
//                        proj.getFacing() + ring1_Angle,
//                        -ring1_RotationSpeed * animTime,
//                        new Color(255, 255, 255, 255),
//                        false,
//                        0,
//                        0,
//                        animTime
//                );
//
//                SpriteAPI ring2 = ringList.get(MathUtils.getRandomNumberInRange(0, ringList.size() - 1));
//
//                float ring2_grow = MathUtils.getRandomNumberInRange(12, 16);
//                float ring2_Size = ring1_Size * MathUtils.getRandomNumberInRange(1.3f, 1.4f);
//                float ring2_RotationSpeed = MathUtils.getRandomNumberInRange(200f, 300f);
//                float ring2_Angle = MathUtils.getRandomNumberInRange(-45f, 45f);
//
//                MagicRender.battlespace(
//                        ring2,
//                        SpawnPoint,
//                        new Vector2f(),
//                        new Vector2f(ring2_Size, ring2_Size),
//                        new Vector2f(-ring2_grow, -ring2_grow),
//                        proj.getFacing() + ring2_Angle,
//                        ring2_RotationSpeed * animTime,
//                        new Color(255, 255, 255, 255),
//                        false,
//                        0,
//                        0,
//                        animTime
//                );
//
//                 */
//            }
//
//        }
    }


    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        engine.spawnProjectile(weapon.getShip(),
                weapon,
                "vic_nawia_real",
                projectile.getLocation(),
                projectile.getFacing(),
                weapon.getShip().getVelocity());
        engine.removeEntity(projectile);

        ShipAPI ship = weapon.getShip();
        Vector2f weaponLocation = weapon.getLocation();
        float shipFacing = weapon.getCurrAngle();
        Vector2f shipVelocity = ship.getVelocity();
        Vector2f muzzleLocation = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isHardpoint() ? MUZZLE_OFFSET_HARDPOINT : MUZZLE_OFFSET_TURRET, shipFacing);

        if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
            vic_graphicLibEffects.CustomRippleDistortion(
                    muzzleLocation,
                    shipVelocity,
                    50f,
                    3f,
                    false,
                    weapon.getCurrAngle() + 180f,
                    90,
                    1f,
                    0.1f,
                    0.25f,
                    0.15f,
                    0.25f,
                    0f
            );
        }

        WaveDistortion wave = new WaveDistortion(muzzleLocation, shipVelocity);
        wave.setIntensity(3f);
        wave.setSize(35f);
        wave.flip(false);
        wave.setLifetime(0f);
        wave.fadeOutIntensity(0.3f);
        wave.setLocation(muzzleLocation);
        DistortionShader.addDistortion(wave);

            weapon.getShip().getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.5f);
        }
    }

