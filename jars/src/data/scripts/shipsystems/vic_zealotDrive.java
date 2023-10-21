package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.magiclib.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static com.fs.starfarer.api.util.Misc.ZERO;
import static data.scripts.plugins.vic_combatPlugin.AddHunterDriveTarget;

public class vic_zealotDrive extends BaseShipSystemScript {

    float
            speedBoost = 350,
            accBoost = 200,
            waveRange = 1000,
            waveDuration = 1.2f,
            currWaveDuration = 0f;

    boolean doOnce = true;

    ArrayList<ShipAPI> affectedShips = new ArrayList<>();
    ArrayList<MissileAPI> affectedMissiles = new ArrayList<>();

    HashMap<ShipAPI.HullSize, Float> arcMulti = new HashMap<>();
    {
        arcMulti.put(ShipAPI.HullSize.FIGHTER, 2f);
        arcMulti.put(ShipAPI.HullSize.FRIGATE, 4f);
        arcMulti.put(ShipAPI.HullSize.DESTROYER, 5f);
        arcMulti.put(ShipAPI.HullSize.CRUISER, 7f);
        arcMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 9f);
    }


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (stats.getEntity() == null) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        switch (state) {
            case IN:
            case ACTIVE:
                stats.getMaxSpeed().modifyFlat(id, speedBoost);
                stats.getAcceleration().modifyFlat(id, accBoost);
                doOnce = true;
                break;
            case OUT:
                if (doOnce) {
                    //vic_combatPlugin.AddHunterDriveAnimation(ship);
                    doOnce = false;

                    currWaveDuration = 0f;
                    affectedShips.clear();
                    affectedMissiles.clear();

                    float rotation = (float) Math.random() * 360;
                    Vector2f LocPulse = ship.getLocation();

                    if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {

                        vic_graphicLibEffects.CustomRippleDistortion(
                                LocPulse,
                                ship.getVelocity(),
                                1500f,
                                10f,
                                false,
                                0,
                                360,
                                1f,
                                0.5f,
                                0.0f,
                                0.90f,
                                1.1f,
                                0f
                        );

                    }

                    WaveDistortion wave = new WaveDistortion(LocPulse, ZERO);
                    wave.setIntensity(15f);
                    wave.setSize(250f);
                    wave.flip(false);
                    wave.setLifetime(0f);
                    wave.fadeOutIntensity(0.5f);
                    wave.setLocation(LocPulse);
                    DistortionShader.addDistortion(wave);

                    int particleCount = 33;
                    final Color PARTICLE_COLOR = new Color(206, 13, 13);
                    for (int x = 0; x < particleCount; x++) {
                        Global.getCombatEngine().addSmoothParticle(LocPulse,
                                MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(1000f, 1750f), (float) Math.random() * 360f),
                                MathUtils.getRandomNumberInRange(5f, 25f),
                                MathUtils.getRandomNumberInRange(0.5f, 2f),
                                MathUtils.getRandomNumberInRange(0.5f, 1f),
                                PARTICLE_COLOR);
                    }

                    int particleCount1 = 33;
                    final Color PARTICLE_COLOR1 = new Color(177, 75, 19);
                    for (int x = 0; x < particleCount1; x++) {
                        Global.getCombatEngine().addSmoothParticle(LocPulse,
                                MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(1000f, 1750f), (float) Math.random() * 360f),
                                MathUtils.getRandomNumberInRange(5f, 25f),
                                MathUtils.getRandomNumberInRange(0.5f, 2f),
                                MathUtils.getRandomNumberInRange(0.5f, 1f),
                                PARTICLE_COLOR1);
                    }

                    int particleCount2 = 33;
                    final Color PARTICLE_COLOR2 = new Color(255, 163, 48);
                    for (int x = 0; x < particleCount2; x++) {
                        Global.getCombatEngine().addSmoothParticle(LocPulse,
                                MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(1000f, 1750f), (float) Math.random() * 360f),
                                MathUtils.getRandomNumberInRange(5f, 25f),
                                MathUtils.getRandomNumberInRange(0.5f, 2f),
                                MathUtils.getRandomNumberInRange(0.5f, 1f),
                                PARTICLE_COLOR2);
                    }


                    for (int i = 0; i < 2; i++) {
                        float spin = MathUtils.getRandomNumberInRange(-120, 120);
                        MagicRender.battlespace(
                                Global.getSettings().getSprite("fx", "vic_devotee_pulse_secondary"),
                                LocPulse,
                                ship.getVelocity(),
                                new Vector2f(1f, 1f),
                                (Vector2f) new Vector2f((waveRange) * 8, (waveRange) * 8),
                                rotation,
                                spin,
                                new Color(255, 100, 100, 255),
                                true,
                                0, 0, 0.4f, 0.8f, 0,
                                0f,
                                0f,
                                0.1f,
                                CombatEngineLayers.BELOW_SHIPS_LAYER
                        );

                        MagicRender.battlespace(
                                Global.getSettings().getSprite("fx", "vic_devotee_pulse_secondary"),
                                LocPulse,
                                ship.getVelocity(),
                                new Vector2f(100f, 100f),
                                (Vector2f) new Vector2f((waveRange) * 5f, (waveRange) * 5.5f),
                                rotation,
                                spin,
                                new Color(255, 100, 100, 255),
                                true,
                                0, 0, 0.4f, 0.8f, 0,
                                0f,
                                0f,
                                0.3f,
                                CombatEngineLayers.BELOW_SHIPS_LAYER
                        );

                        MagicRender.battlespace(
                                Global.getSettings().getSprite("fx", "vic_devotee_pulse_main"),
                                LocPulse,
                                ship.getVelocity(),
                                new Vector2f(500f, 500f),
                                (Vector2f) new Vector2f((waveRange) * 3, (waveRange) * 3),
                                rotation,
                                spin * -1,
                                new Color(255, 100, 100, 200),
                                true,
                                0, 0, 0.4f, 0.8f, 0,
                                0.4f,
                                0f,
                                0.7f,
                                CombatEngineLayers.BELOW_SHIPS_LAYER
                        );

                        MagicRender.battlespace(
                                Global.getSettings().getSprite("fx", "vic_devotee_pulse_secondary"),
                                LocPulse,
                                ship.getVelocity(),
                                new Vector2f(750f, 750f),
                                (Vector2f) new Vector2f(250f, 250f),
                                rotation,
                                spin * -1,
                                new Color(255, 100, 100, 50),
                                true,
                                0.5f, 0.5f, 0.8f, 1.6f, 0,
                                0.3f,
                                0f,
                                0.9f,
                                CombatEngineLayers.BELOW_SHIPS_LAYER
                        );
                    }

                    Global.getCombatEngine().addHitParticle(
                            ship.getLocation(),
                            ship.getVelocity(),
                            1000,
                            1f,
                            //0,
                            0.2f,
                            Color.WHITE);

                    Global.getCombatEngine().addHitParticle(
                            ship.getLocation(),
                            ship.getVelocity(),
                            1500,
                            0.6f,
                            //0,
                            0.2f,
                            Color.RED);

                    for (int i = 0; i < arcMulti.get(ship.getHullSize()); i++){
                        Global.getCombatEngine().spawnEmpArcPierceShields(ship,
                                ship.getLocation(),
                                null,
                                ship,
                                DamageType.ENERGY,
                                0,
                                200,
                                10000,
                                null,
                                20,
                                new Color(MathUtils.getRandomNumberInRange(220, 255), MathUtils.getRandomNumberInRange(0, 75), 0, 255),
                                Color.white);
                    }
                    ship.getFluxTracker().beginOverloadWithTotalBaseDuration(2f);
                }


                if (currWaveDuration <= waveDuration){
                    currWaveDuration += amount;
                    float range = waveRange * currWaveDuration / waveDuration;

                    for (ShipAPI target : AIUtils.getNearbyEnemies(ship, range)){
                        if (!affectedShips.contains(target)){
                            affectedShips.add(target);
                            if (target.isPhased()){
                                target.getFluxTracker().beginOverloadWithTotalBaseDuration(1f);
                            }
                            Vector2f empPos = new Vector2f((ship.getLocation().x + target.getLocation().x) * 0.5f, (ship.getLocation().y + target.getLocation().y) * 0.5f);
                            float damage = 500f;
                            if (target.isFighter()) damage = 500f;

                            for (int i = 0; i < arcMulti.get(target.getHullSize()); i++){
                                Global.getCombatEngine().spawnEmpArcPierceShields(ship,
                                        empPos,
                                        null,
                                        target,
                                        DamageType.ENERGY,
                                        damage,
                                        50,
                                        10000,
                                        null,
                                        20,
                                        new Color(MathUtils.getRandomNumberInRange(220, 255), MathUtils.getRandomNumberInRange(0, 75), 0, 255),
                                        Color.white);
                            }
                        }
                    }

                    for (MissileAPI missile : AIUtils.getNearbyEnemyMissiles(ship, range)) {
                        if (!affectedMissiles.contains(missile)) {
                            affectedMissiles.add(missile);
                            Vector2f empPos = new Vector2f((ship.getLocation().x + missile.getLocation().x) * 0.5f, (ship.getLocation().y + missile.getLocation().y) * 0.5f);
                            Global.getCombatEngine().spawnEmpArcPierceShields(ship,
                                    empPos,
                                    null,
                                    missile,
                                    DamageType.ENERGY,
                                    350,
                                    0,
                                    10000,
                                    null,
                                    20,
                                    new Color(MathUtils.getRandomNumberInRange(220, 255), MathUtils.getRandomNumberInRange(0, 75), 0, 255),
                                    Color.white);
                        }
                    }
                }

                stats.getMaxSpeed().unmodify(id);
                stats.getAcceleration().unmodify(id);
                if (!MathUtils.isWithinRange(new Vector2f(), ship.getVelocity(), ship.getMaxSpeed())) {
                    ship.getVelocity().scale(1f - 0.5f * amount);
                }
                break;
        }
    }
}
