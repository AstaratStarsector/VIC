package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.input.InputEventAPI;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.*;

public class vic_shipExplosion extends BaseEveryFrameCombatPlugin {

    private static final Set<String> APPLICABLE_SHIPS = new HashSet<>(1);

    private static final Color COLOR_ATTACHED_LIGHT = new Color(0, 255, 174);
    private static final Color COLOR_EMP_CORE = new Color(0, 255, 247, 150);
    private static final Color COLOR_EMP_FRINGE = new Color(50, 195, 200, 100);
    private static final Color COLOR_PARTICLE = new Color(50, 255, 197);
    private static final Color COLOR_SUPERBRITE = new Color(200, 255, 255);

    private static final Map<String, Float> CORE_OFFSET = new HashMap<>(1);

    private static final String DATA_KEY = "vic_shipExplosion";

    private static final Map<HullSize, Float> EXPLOSION_AREA_INCREASE = new HashMap<>(5);
    private static final Map<HullSize, Float> EXPLOSION_INTENSITY = new HashMap<>(5);
    private static final Map<HullSize, Float> EXPLOSION_LENGTH = new HashMap<>(5);
    private static final Map<HullSize, Float> PITCH_BEND = new HashMap<>(5);

    private static final Vector2f ZERO = new Vector2f();

    static {
        APPLICABLE_SHIPS.add("vic_apollyon");

        CORE_OFFSET.put("vic_apollyon", -50f);
    }

    static {
        EXPLOSION_LENGTH.put(HullSize.FIGHTER, 1.5f);
        EXPLOSION_INTENSITY.put(HullSize.FIGHTER, 0.5f);
        EXPLOSION_AREA_INCREASE.put(HullSize.FIGHTER, 50f);
        PITCH_BEND.put(HullSize.FIGHTER, 1.2f);

        EXPLOSION_LENGTH.put(HullSize.FRIGATE, 3f);
        EXPLOSION_INTENSITY.put(HullSize.FRIGATE, 1.1f);
        EXPLOSION_AREA_INCREASE.put(HullSize.FRIGATE, 300f);
        PITCH_BEND.put(HullSize.FRIGATE, 1.07f);

        EXPLOSION_LENGTH.put(HullSize.DESTROYER, 4.5f);
        EXPLOSION_INTENSITY.put(HullSize.DESTROYER, 1.225f);
        EXPLOSION_AREA_INCREASE.put(HullSize.DESTROYER, 400f);
        PITCH_BEND.put(HullSize.DESTROYER, 1f);

        EXPLOSION_LENGTH.put(HullSize.CRUISER, 6f);
        EXPLOSION_INTENSITY.put(HullSize.CRUISER, 1.25f);
        EXPLOSION_AREA_INCREASE.put(HullSize.CRUISER, 500f);
        PITCH_BEND.put(HullSize.CRUISER, 0.92f);

        EXPLOSION_LENGTH.put(HullSize.CAPITAL_SHIP, 8f);
        EXPLOSION_INTENSITY.put(HullSize.CAPITAL_SHIP, 1.5f);
        EXPLOSION_AREA_INCREASE.put(HullSize.CAPITAL_SHIP, 650f);
        PITCH_BEND.put(HullSize.CAPITAL_SHIP, 0.85f);

        EXPLOSION_LENGTH.put(HullSize.DEFAULT, 8f);
        EXPLOSION_INTENSITY.put(HullSize.DEFAULT, 1.5f);
        EXPLOSION_AREA_INCREASE.put(HullSize.DEFAULT, 650f);
        PITCH_BEND.put(HullSize.DEFAULT, 1f);
    }

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Set<ShipAPI> deadShips = localData.deadShips;

        List<ShipAPI> ships = engine.getShips();
        for (ShipAPI ship : ships) {
            if (ship == null) {
                continue;
            }

            if (ship.isHulk() && !ship.isPiece()) {
                if (!APPLICABLE_SHIPS.contains(ship.getHullSpec().getHullId())) {
                    continue;
                }

                if (!deadShips.contains(ship)) {
                    deadShips.add(ship);
                    Global.getCombatEngine().addPlugin(createMissileJitterPlugin(ship, EXPLOSION_LENGTH.get(ship.getHullSize())));
                }


            }
        }
    }

    protected EveryFrameCombatPlugin createMissileJitterPlugin(final ShipAPI ship, final float delay) {
        return new BaseEveryFrameCombatPlugin() {
            float elapsed = 0f;
            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                if (Global.getCombatEngine().isPaused()) return;

                elapsed += amount;
                if (elapsed < delay) return;

                Vector2f shipLoc = MathUtils.getPointOnCircumference(ship.getLocation(), CORE_OFFSET.get(
                        ship.getHullSpec().getHullId()), ship.getFacing());
                ship.setOwner(ship.getOriginalOwner());

                float explosionTime = EXPLOSION_LENGTH.get(ship.getHullSize());
                float area = EXPLOSION_AREA_INCREASE.get(ship.getHullSize()) + ship.getCollisionRadius();
                float damage = 5f * (float) Math.sqrt(ship.getFluxTracker().getMaxFlux()) * EXPLOSION_INTENSITY.get(
                        ship.getHullSize());
                float emp = 20f * (float) Math.sqrt(ship.getFluxTracker().getMaxFlux()) * EXPLOSION_INTENSITY.get(
                        ship.getHullSize());

                for (int i = 0; i <= (float) Math.sqrt(ship.getCollisionRadius()) * 8f * EXPLOSION_INTENSITY.get(
                        ship.getHullSize()); i++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * area * 0.5f + area * 0.5f;
                    Vector2f point1 = MathUtils.getPointOnCircumference(shipLoc, distance * (float) Math.random(), angle);
                    Vector2f point2 = MathUtils.getPointOnCircumference(shipLoc, distance * (float) Math.random(), angle + 45f *
                            (float) Math.random());
                    engine.spawnEmpArc(ship, point1, new SimpleEntity(point1), new SimpleEntity(point2), DamageType.ENERGY, 0f,
                            0f, 1000f, null,
                            EXPLOSION_INTENSITY.get(ship.getHullSize()) * 10f + 10f, COLOR_EMP_FRINGE, COLOR_EMP_CORE);
                }
                for (int i = 0; i <= ship.getCollisionRadius() * EXPLOSION_INTENSITY.get(ship.getHullSize()); i++) {
                    if (Math.random() > 0.5) {
                        Vector2f point1 = MathUtils.getRandomPointInCircle(shipLoc, (float) Math.random() * area * 0.5f + area *
                                0.5f);
                        Vector2f point2 = MathUtils.getRandomPointInCircle(shipLoc, ship.getCollisionRadius() * 0.25f);
                        engine.spawnEmpArc(ship, point2, new SimpleEntity(point2), new SimpleEntity(point1), DamageType.ENERGY,
                                0f, 0f, 1000f, null,
                                EXPLOSION_INTENSITY.get(ship.getHullSize()) * 10f + 10f, COLOR_EMP_FRINGE,
                                COLOR_EMP_CORE);
                    }
                }


                engine.spawnExplosion(shipLoc, ZERO, COLOR_SUPERBRITE, area, 0.1f * explosionTime);
                engine.spawnExplosion(shipLoc, ZERO, COLOR_ATTACHED_LIGHT, area * 0.4f, explosionTime * 1.25f);

                //engine.addHitParticle(shipLoc, ZERO, area * 2.5f, 10f, 0.05f * explosionTime, COLOR_SUPERBRITE);
                //engine.addHitParticle(shipLoc, ZERO, area * 0.125f, 10f, explosionTime * 0.75f, COLOR_SUPERBRITE);
                //engine.addHitParticle(shipLoc, ZERO, area * 0.25f, 10f, explosionTime, COLOR_SUPERBRITE);
                //engine.addHitParticle(shipLoc, ZERO, area * 0.50f, 10f, explosionTime * 1.25f, COLOR_SUPERBRITE);
                //engine.addSmoothParticle(shipLoc, ZERO, area * 1.5f, 0.5f, explosionTime * 1.5f, COLOR_EMP_FRINGE);


                /*
                for (int i = 0; i <= (int) ship.getCollisionRadius() * EXPLOSION_INTENSITY.get(ship.getHullSize()); i++) {
                    float radius = ship.getCollisionRadius() * (float) Math.random() * 0.25f;
                    Vector2f direction = MathUtils.getRandomPointOnCircumference(null, ship.getCollisionRadius() *
                            ((float) Math.random() * 0.75f + 0.25f) *
                            EXPLOSION_INTENSITY.get(ship.getHullSize()));
                    Vector2f point = MathUtils.getPointOnCircumference(shipLoc, radius, VectorUtils.getFacing(direction));
                    direction.scale(-1);
                    engine.addHitParticle(point, direction, 10f, 1f, (0f + (float) Math.random()) * (float) Math.sqrt(
                            EXPLOSION_LENGTH.get(ship.getHullSize())),
                            COLOR_PARTICLE);
                }
                */


                List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(shipLoc, area);
                for (ShipAPI thisShip : nearbyShips) {
                    if (thisShip.getCollisionClass() == CollisionClass.NONE) {
                        continue;
                    }

                    Vector2f damagePoint = CollisionUtils.getCollisionPoint(shipLoc, thisShip.getLocation(), thisShip);
                    if (damagePoint == null) {
                        damagePoint = thisShip.getLocation();
                    }
                    Vector2f forward = new Vector2f(damagePoint);
                    forward.normalise();
                    forward.scale(5f);
                    Vector2f.add(forward, damagePoint, damagePoint);
                    float falloff = 1f - MathUtils.getDistance(ship, thisShip) / area;
                    if (ship.getOwner() == thisShip.getOwner() && ship != thisShip) {
                        falloff *= 0.5f;
                    }
                    engine.applyDamage(thisShip, damagePoint, damage * falloff, DamageType.ENERGY, emp * falloff * 0.25f, false,
                            false, ship);

                    for (int i = 0; i <= (int) (damage * (falloff / 250f) * EXPLOSION_INTENSITY.get(ship.getHullSize())); i++) {
                        Vector2f point = MathUtils.getRandomPointInCircle(thisShip.getLocation(),
                                thisShip.getCollisionRadius() * 1.5f);
                        engine.spawnEmpArc(ship, point, thisShip, thisShip, DamageType.ENERGY, damage * falloff * 0.5f, emp *
                                        falloff * 0.5f, 1000f, null,
                                (float) Math.sqrt(damage), COLOR_EMP_FRINGE, COLOR_EMP_CORE);
                    }
                }

                /*
                StandardLight light = new StandardLight(shipLoc, ZERO, ZERO, null);
                light.setColor(COLOR_ATTACHED_LIGHT);
                light.setSize(area * 1.5f);
                light.setIntensity(1f * EXPLOSION_INTENSITY.get(ship.getHullSize()));
                light.fadeOut(explosionTime);
                LightShader.addLight(light);
                 */

                float time = EXPLOSION_INTENSITY.get(ship.getHullSize());
                RippleDistortion ripple = new RippleDistortion(shipLoc, ZERO);
                ripple.setSize(area);
                ripple.setIntensity(100f * EXPLOSION_INTENSITY.get(ship.getHullSize()));
                ripple.setFrameRate(60f / (time));
                ripple.fadeInSize(time);
                ripple.fadeOutIntensity(time);
                DistortionShader.addDistortion(ripple);

                Global.getSoundPlayer().playSound(
                        "vic_quantum_lunge_explosion",
                        PITCH_BEND.get(ship.getHullSize()),
                        EXPLOSION_INTENSITY.get(ship.getHullSize()),
                        shipLoc,
                        ZERO);

                ship.setOwner(100);
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 3); i++) {
                    ship.splitShip();
                }

                Global.getCombatEngine().removePlugin(this);
            }
        };
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
    }

    private static final class ExplodingShip {

        float chargeLevel;
        float chargingTime;
        ShipAPI ship;

        private ExplodingShip(ShipAPI ship, float chargingTime) {
            this.ship = ship;
            this.chargingTime = chargingTime;
            this.chargeLevel = 0f;
        }
    }

    private static final class LocalData {

        final Set<ShipAPI> deadShips = new LinkedHashSet<>(50);
        final List<ExplodingShip> explodingShips = new ArrayList<>(50);
    }
}
