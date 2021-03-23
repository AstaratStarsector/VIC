package data.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.TEM_LatticeShield;
import data.scripts.util.TEM_AnamorphicFlare;
import data.scripts.util.TEM_Multi;
import data.scripts.util.TEM_Util;
import data.scripts.weapons.TEM_GalatineOnHitEffect;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class TEM_WeaponScriptPlugin extends BaseEveryFrameCombatPlugin {

    public static final void genJugerGlow(DamagingProjectileAPI proj) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.JUGER_GLOW);
        fx.anchor = proj;
        fx.width = 67;
        fx.height = 80;
        fx.scale = MathUtils.getRandomNumberInRange(0.95f, 1.05f);
        fx.alpha = MathUtils.getRandomNumberInRange(0.75f, 1f);
        fx.angle = proj.getFacing();
        fx.loc = MathUtils.getPointOnCircumference(proj.getLocation(), 30f, proj.getFacing());
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_jugerglow");

        fxData.add(fx);
    }

    public static final void genJugerBlast(Vector2f loc, Vector2f vel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.JUGER_BLAST);
        fx.width = 128;
        fx.height = 128;
        fx.angle = MathUtils.getRandomNumberInRange(85f, 95f);
        fx.scale = MathUtils.getRandomNumberInRange(3f, 3.5f);
        fx.duration = MathUtils.getRandomNumberInRange(0.4f, 0.5f);
        fx.alpha = MathUtils.getRandomNumberInRange(0.7f, 0.75f);
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_jugerblast");

        fxData.add(fx);
    }

    public static final void genSecaceBlast(Vector2f loc, Vector2f vel, float alpha) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.SECACE_BLAST);
        fx.width = 100;
        fx.height = 100;
        fx.angle = MathUtils.getRandomNumberInRange(0f, 360f);
        fx.angVel = MathUtils.getRandomNumberInRange(90f, 120f) * ((Math.random() > 0.5) ? 1f : -1f);
        fx.scale = MathUtils.getRandomNumberInRange(0.9f, 1.1f);
        fx.duration = MathUtils.getRandomNumberInRange(0.1f, 0.15f);
        fx.color = new Color(
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(200, 255)),
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(230, 255)),
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(240, 255)),
                255);
        fx.alpha = MathUtils.getRandomNumberInRange(0.9f, 1f) * alpha;
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_secaceblast");

        fxData.add(fx);
    }

    public static final void genGalatineBlast(Vector2f loc, Vector2f vel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.GALATINE_BLAST);
        fx.width = 105;
        fx.height = 113;
        fx.angle = MathUtils.getRandomNumberInRange(0f, 360f);
        fx.angVel = MathUtils.getRandomNumberInRange(50f, 70f) * ((Math.random() > 0.5) ? 1f : -1f);
        fx.scale = MathUtils.getRandomNumberInRange(1.1f, 1.2f);
        fx.duration = MathUtils.getRandomNumberInRange(0.5f, 0.65f);
        fx.color = new Color(
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(200, 255)),
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(230, 255)),
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(240, 255)),
                255);
        fx.alpha = MathUtils.getRandomNumberInRange(0.7f, 0.8f);
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_galatineblast");

        fxData.add(fx);
    }

    public static final void genArondightBlast(Vector2f loc, Vector2f vel, boolean shieldImpact) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.ARONDIGHT_BLAST);
        fx.width = 105;
        fx.height = 113;
        fx.angle = MathUtils.getRandomNumberInRange(0f, 360f);
        if (shieldImpact) {
            fx.scale = MathUtils.getRandomNumberInRange(1.5f, 1.6f);
            fx.duration = MathUtils.getRandomNumberInRange(0.75f, 0.9f);
            fx.alpha = MathUtils.getRandomNumberInRange(0.9f, 1f);
        } else {
            fx.scale = MathUtils.getRandomNumberInRange(1.4f, 1.5f);
            fx.duration = MathUtils.getRandomNumberInRange(0.7f, 0.8f);
            fx.alpha = MathUtils.getRandomNumberInRange(0.6f, 0.7f);
        }
        fx.angVel = MathUtils.getRandomNumberInRange(40f, 60f) * ((Math.random() > 0.5) ? 1f : -1f);
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_galatineblast");

        fxData.add(fx);

        fx = new FXData(FXType.ARONDIGHT_FLARE);
        fx.width = 248;
        fx.height = 252;
        fx.angle = MathUtils.getRandomNumberInRange(80f, 100f);
        fx.color = new Color(
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(200, 255)),
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(230, 255)),
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(240, 255)),
                255);
        if (shieldImpact) {
            fx.scale = MathUtils.getRandomNumberInRange(2.5f, 2.75f);
            fx.duration = MathUtils.getRandomNumberInRange(0.7f, 0.8f);
            fx.alpha = MathUtils.getRandomNumberInRange(0.9f, 1f);
        } else {
            fx.scale = MathUtils.getRandomNumberInRange(2.25f, 2.5f);
            fx.duration = MathUtils.getRandomNumberInRange(0.8f, 0.7f);
            fx.alpha = MathUtils.getRandomNumberInRange(0.8f, 0.9f);
        }
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_arondightflare");

        fxData.add(fx);
    }

    public static final void genCrucifixBlast(Vector2f loc, Vector2f vel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.CRUCIFIX_BLAST);
        fx.width = 80;
        fx.height = 80;
        fx.angle = MathUtils.getRandomNumberInRange(0f, 360f);
        fx.angVel = MathUtils.getRandomNumberInRange(0f, 30f) * ((Math.random() > 0.5) ? 1f : -1f);
        fx.scale = MathUtils.getRandomNumberInRange(1.2f, 1.4f);
        fx.duration = MathUtils.getRandomNumberInRange(0.2f, 0.35f);
        fx.color = new Color(
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(180, 255)),
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(210, 255)),
                TEM_Util.clamp255(MathUtils.getRandomNumberInRange(240, 255)),
                255);
        fx.alpha = MathUtils.getRandomNumberInRange(0.9f, 1f);
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_crucifixblast");

        fxData.add(fx);
    }

    public static final void genPaxBlast(Vector2f loc, Vector2f vel, float alpha) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.PAX_BLAST);
        fx.width = 128;
        fx.height = 128;
        fx.angle = MathUtils.getRandomNumberInRange(0f, 360f);
        fx.angVel = MathUtils.getRandomNumberInRange(0f, 20f) * ((Math.random() > 0.5) ? 1f : -1f);
        fx.scale = MathUtils.getRandomNumberInRange(0.4f, 0.45f);
        fx.duration = MathUtils.getRandomNumberInRange(0.1f, 0.15f);
        fx.alpha = MathUtils.getRandomNumberInRange(0.5f, 0.6f);
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_jugerblast");

        fxData.add(fx);
    }

    public static final void genMercedBlast(Vector2f loc, Vector2f vel, float alpha) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.MERCED_BLAST);
        fx.width = 128;
        fx.height = 128;
        if (Math.random() > 0.5) {
            fx.width = Math.round(fx.width * MathUtils.getRandomNumberInRange(0.5f, 0.6f));
        } else {
            fx.height = Math.round(fx.width * MathUtils.getRandomNumberInRange(0.5f, 0.6f));
        }
        fx.angle = MathUtils.getRandomNumberInRange(0f, 360f);
        fx.angVel = MathUtils.getRandomNumberInRange(0f, 20f) * ((Math.random() > 0.5) ? 1f : -1f);
        fx.scale = MathUtils.getRandomNumberInRange(0.75f, 0.8f);
        fx.duration = MathUtils.getRandomNumberInRange(0.2f, 0.25f);
        fx.alpha = MathUtils.getRandomNumberInRange(0.7f, 0.9f);
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_jugerblast");

        fxData.add(fx);
    }

    public static final void genSenteniaBlast(Vector2f loc, Vector2f vel, float alpha) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        FXData fx = new FXData(FXType.SENTENIA_BLAST);
        fx.width = 128;
        fx.height = 128;
        if (Math.random() > 0.5) {
            fx.width = Math.round(fx.width * MathUtils.getRandomNumberInRange(0.4f, 0.6f));
        } else {
            fx.height = Math.round(fx.width * MathUtils.getRandomNumberInRange(0.4f, 0.6f));
        }
        fx.angle = MathUtils.getRandomNumberInRange(0f, 360f);
        fx.angVel = MathUtils.getRandomNumberInRange(0f, 15f) * ((Math.random() > 0.5) ? 1f : -1f);
        fx.scale = MathUtils.getRandomNumberInRange(0.8f, 0.85f);
        fx.duration = MathUtils.getRandomNumberInRange(0.2f, 0.25f);
        fx.alpha = MathUtils.getRandomNumberInRange(0.6f, 0.7f);
        fx.loc = new Vector2f(loc);
        fx.vel = new Vector2f(vel);
        fx.additive = true;
        fx.sprite = Global.getSettings().getSprite("weapons", "tem_jugerblast");

        fxData.add(fx);
    }

    private static final float ARONDIGHT_FLUX_DAMAGE = 1000f;
    private static final Color COLOR1 = new Color(255, 255, 200);
    private static final Color COLOR10 = new Color(150, 200, 225);
    private static final Color COLOR11 = new Color(235, 210, 100);
    private static final Color COLOR12 = new Color(245, 235, 125);
    private static final Color COLOR13 = new Color(255, 255, 255);
    private static final Color COLOR2 = new Color(200, 225, 255);
    private static final Color COLOR3 = new Color(200, 225, 255, 200);
    private static final Color COLOR4 = new Color(175, 255, 255);
    private static final Color COLOR5 = new Color(150, 225, 255);
    private static final Color COLOR6 = new Color(150, 225, 255, 50);
    private static final Color COLOR7 = new Color(50, 175, 255);
    private static final Color COLOR8 = new Color(50, 175, 255, 50);
    private static final Color COLOR9 = new Color(50, 175, 255, 200);

    private static final String DATA_KEY = "TEM_WeaponScript";

    private static final Comparator<CombatEntityAPI> ENTITYSIZE = new Comparator<CombatEntityAPI>() {
        @Override
        public int compare(CombatEntityAPI o1, CombatEntityAPI o2) {
            float f1 = o1.getCollisionRadius();
            float f2 = o2.getCollisionRadius();
            if (f1 > f2) {
                return -1;
            } else if (f1 < f2) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    private static final Vector2f ZERO = new Vector2f();

    public static Vector2f intercept(Vector2f point, float speed, Vector2f target, Vector2f targetVel) {
        Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        float a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - speed * speed;
        float b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        float c = difference.x * difference.x + difference.y * difference.y;

        Vector2f solutionSet = quad(a, b, c);

        Vector2f intercept = null;
        if (solutionSet != null) {
            float bestFit = Math.min(solutionSet.x, solutionSet.y);
            if (bestFit < 0) {
                bestFit = Math.max(solutionSet.x, solutionSet.y);
            }
            if (bestFit > 0) {
                intercept = new Vector2f(target.x + targetVel.x * bestFit, target.y + targetVel.y * bestFit);
            }
        }

        return intercept;
    }

    private static ShipAPI findBestTarget(DamagingProjectileAPI proj, float dist) {
        ShipAPI source = proj.getSource();
        if ((source != null) && (source.getShipTarget() != null)) {
            float angleDif = Math.abs(MathUtils.getShortestRotation(VectorUtils.getAngle(proj.getLocation(), source.getShipTarget().getLocation()), proj.getFacing()));
            float distDif = MathUtils.getDistance(proj, source.getShipTarget());
            if (source.getShipTarget().isAlive() && (((angleDif <= 90f) && (distDif <= dist * 0.5f)) || ((angleDif <= 45f) && (distDif <= dist * 0.75f)))) {
                return source.getShipTarget();
            }
        }
        if ((source != null) && (source.getMouseTarget() != null)) {
            float angleDif = Math.abs(MathUtils.getShortestRotation(VectorUtils.getAngle(proj.getLocation(), source.getMouseTarget()), proj.getFacing()));
            float distDif = MathUtils.getDistance(proj.getLocation(), source.getMouseTarget());
            if (((angleDif <= 90f) && (distDif <= dist * 0.3f)) || ((angleDif <= 45f) && (distDif <= dist * 0.6f))) {
                ShipAPI closest = null;
                float distance, closestDistance = Float.MAX_VALUE;

                for (ShipAPI tmp : AIUtils.getEnemiesOnMap(source)) {
                    distance = MathUtils.getDistance(tmp, source.getMouseTarget());
                    if ((distance < closestDistance) && (distance <= 200f)) {
                        closest = tmp;
                        closestDistance = distance;
                    }
                }

                if (closest != null) {
                    return closest;
                }
            }
        }
        ShipAPI closest = null;
        float distance, closestDistance = dist;
        for (ShipAPI tmp : AIUtils.getEnemiesOnMap(proj)) {
            if (tmp.isDrone()) {
                continue;
            }
            distance = MathUtils.getDistance(tmp, proj.getLocation());
            if (distance < closestDistance) {
                closest = tmp;
                closestDistance = distance;
            }
        }
        if (closest == null) {
            for (ShipAPI tmp : AIUtils.getEnemiesOnMap(proj)) {
                distance = MathUtils.getDistance(tmp, proj.getLocation());
                if (distance < closestDistance) {
                    closest = tmp;
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }

    private static Vector2f quad(float a, float b, float c) {
        Vector2f solution = null;
        if (Float.compare(Math.abs(a), 0) == 0) {
            if (Float.compare(Math.abs(b), 0) == 0) {
                solution = (Float.compare(Math.abs(c), 0) == 0) ? new Vector2f(0, 0) : null;
            } else {
                solution = new Vector2f(-c / b, -c / b);
            }
        } else {
            float d = b * b - 4 * a * c;
            if (d >= 0) {
                d = (float) Math.sqrt(d);
                float e = 2 * a;
                solution = new Vector2f((-b - d) / e, (-b + d) / e);
            }
        }
        return solution;
    }

    private final IntervalUtil damageInterval = new IntervalUtil(0.033f, 0.033f);
    private CombatEngineAPI engine;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<DamagingProjectileAPI, ProjectileData> projectileData = localData.projectileData;
        final Map<DamagingProjectileAPI, CombatEntityAPI> projectileTargets = localData.projectileTargets;

        DamagingProjectileAPI[] activeProjectilesCopy = engine.getProjectiles().toArray(
                new DamagingProjectileAPI[engine.getProjectiles().size()]);
        for (DamagingProjectileAPI projectile : activeProjectilesCopy) {
            if (projectile.isFading() && !projectile.didDamage() && projectile.getProjectileSpecId() != null) {
                if (projectile.getProjectileSpecId().contentEquals("tem_galatine_shot")) {
                    TEM_GalatineOnHitEffect.explode(projectile, null, new Vector2f(projectile.getLocation()), engine,
                            true);
                    engine.removeEntity(projectile);
                    continue;
                }
            }

            if (!projectile.isFading() && !projectile.didDamage() && projectile.getProjectileSpecId() != null) {
                if (projectile.getProjectileSpecId().contentEquals("tem_pax_fake_shot")) {
                    float spread = Math.min(projectile.getWeapon().getCurrSpread() / 2f, 45f);
                    float force = (float) Math.tan(Math.toRadians(spread)) * projectile.getWeapon().getProjectileSpeed();
                    Vector2f randomVel = MathUtils.getRandomPointOnCircumference(projectile.getSource().getVelocity(),
                            MathUtils.getRandomNumberInRange(0f,
                                    force));

                    DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(
                            projectile.getSource(), projectile.getWeapon(),
                            "tem_pax_dummy", projectile.getLocation(),
                            projectile.getWeapon().getCurrAngle(), randomVel);
                    Vector2f velocityWithoutShip = Vector2f.sub(newProj.getVelocity(),
                            projectile.getSource().getVelocity(), new Vector2f());
                    newProj.setFacing(VectorUtils.getFacing(velocityWithoutShip));

                    Vector2f trans = VectorUtils.rotate(projectile.getSource().getVelocity(), -newProj.getFacing(),
                            new Vector2f());
                    float realProjectileSpeed = trans.x + projectile.getWeapon().getProjectileSpeed();

                    float lifetime = (projectile.getWeapon().getRange() * 1.3f) / realProjectileSpeed;
                    float scale = lifetime / (1.4427f - 1.4427f * (float) Math.exp(-0.693147f * lifetime));
                    trans = VectorUtils.rotate(newProj.getVelocity(), -newProj.getFacing(), new Vector2f());
                    trans.x *= scale;
                    VectorUtils.rotate(trans, newProj.getFacing(), trans);
                    newProj.getVelocity().set(trans);
                    engine.removeEntity(projectile);
                }
            }
        }

        List<DamagingProjectileAPI> activeProjectiles = engine.getProjectiles();
        int projectilesSize = activeProjectiles.size();
        for (int i = 0; i < projectilesSize; i++) {
            DamagingProjectileAPI projectile = activeProjectiles.get(i);
            if (projectile.getProjectileSpecId() == null || projectile.didDamage() || projectile.isFading()) {
                continue;
            }

            if (projectile.getProjectileSpecId().contentEquals("tem_sentenia_shot")) {
                if (!projectileTargets.containsKey(projectile)) {
                    if (projectile.getProjectileSpecId() != null) {
                        projectileTargets.put(projectile, null);
                    }
                }
            }

            if (projectile.getProjectileSpecId().contentEquals("tem_juger_shot")) {
                if (!projectileData.containsKey(projectile)) {
                    if (projectile.getProjectileSpecId() != null) {
                        double r = Math.random();
                        if (r >= 1.0 / 3.0) {
                            projectileData.put(projectile, new ProjectileData((float) Math.random() * 0.6f, 0));
                        } else if (r > 2.0 / 3.0) {
                            projectileData.put(projectile, new ProjectileData((float) Math.random() * 0.6f, 1));
                        } else {
                            projectileData.put(projectile, new ProjectileData((float) Math.random() * 0.6f, 2));
                        }
                        genJugerGlow(projectile);
                    }
                }
            }

            if (projectile.getProjectileSpecId().contentEquals("tem_joyeuse_fake_shot")) {
                if (!projectileData.containsKey(projectile)) {
                    if (projectile.getProjectileSpecId() != null) {
                        projectile.getVelocity().scale(1f + (float) Math.random() * 0.2f - 0.1f);
                        projectileData.put(projectile, new ProjectileData((float) Math.random() * 0.05f, 0));
                    }
                }
            }

            if (projectile.getProjectileSpecId().contentEquals("tem_arondight_shot")) {
                if (!projectileData.containsKey(projectile)) {
                    if (projectile.getProjectileSpecId() != null) {
                        projectileData.put(projectile, new ProjectileData(0.1f, 0));
                    }
                }
            }

            if (projectile.getProjectileSpecId().contentEquals("tem_pax_shot")) {
                Vector2f trans = VectorUtils.rotate(projectile.getVelocity(), -projectile.getFacing(), new Vector2f());
                trans.x *= (float) Math.pow(0.5, amount);
                VectorUtils.rotate(trans, projectile.getFacing(), trans);
                projectile.getVelocity().set(trans);
            }

            if (projectile.getProjectileSpecId().contentEquals("ron_sanpuheiki_shot")) {
                Vector2f trans = VectorUtils.rotate(projectile.getVelocity(), -projectile.getFacing(), new Vector2f());
                float amt = Math.min(amount, 0.25f);
                if (projectile.getElapsed() <= amount * 2f) {
                    amt = 0.05f;
                }
                trans.x *= MathUtils.getRandomNumberInRange(1f - amt, 1f + amt);
                VectorUtils.rotate(trans, projectile.getFacing(), trans);
                projectile.getVelocity().set(trans);
            }
        }

        if (!projectileTargets.isEmpty()) {
            Iterator<Map.Entry<DamagingProjectileAPI, CombatEntityAPI>> iter = projectileTargets.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<DamagingProjectileAPI, CombatEntityAPI> entry = iter.next();
                DamagingProjectileAPI projectile = entry.getKey();
                CombatEntityAPI target = entry.getValue();

                if (projectile.didDamage() || !engine.isEntityInPlay(projectile)) {
                    iter.remove();
                    continue;
                }

                float accel = 1000f;
                float maxSpeed = 1000f;
                float maxDistance = 1000f;
                if (projectile.getProjectileSpecId().contentEquals("tem_sentenia_shot")) {
                    accel = 2100f;
                    maxSpeed = 700f;
                    maxDistance = 500f;
                }
                if (projectile.getSource() != null) {
                    accel = projectile.getSource().getMutableStats().getMissileAccelerationBonus().computeEffective(
                            accel);
                    maxSpeed = projectile.getSource().getMutableStats().getMissileMaxSpeedBonus().computeEffective(
                            maxSpeed);
                }

                if (target == null || (target instanceof ShipAPI && ((ShipAPI) target).isHulk())
                        || (projectile.getOwner() == target.getOwner())
                        || !engine.isEntityInPlay(target) || Math.random() > 0.95) {
                    ShipAPI tgt = findBestTarget(projectile, maxDistance + 500f);
                    if (tgt != null && MathUtils.getDistance(tgt, projectile) <= maxDistance) {
                        entry.setValue(tgt);
                    }
                    continue;
                }

                float distance = MathUtils.getDistance(target.getLocation(), projectile.getLocation());
                Vector2f guidedTarget = intercept(projectile.getLocation(), projectile.getVelocity().length(),
                        target.getLocation(), target.getVelocity());
                if (guidedTarget == null) {
                    Vector2f projection = new Vector2f(target.getVelocity());
                    float scalar = distance / (projectile.getVelocity().length() + 1f);
                    projection.scale(scalar);
                    guidedTarget = Vector2f.add(target.getLocation(), projection, null);
                }

                Vector2f acceleration = VectorUtils.getDirectionalVector(projectile.getLocation(), guidedTarget);
                acceleration.scale(accel * amount);
                Vector2f.add(acceleration, projectile.getVelocity(), acceleration);
                float speed = acceleration.length();
                if (speed > 0f) {
                    acceleration.scale(maxSpeed / speed);
                }

                projectile.setFacing(VectorUtils.getAngle(ZERO, acceleration));
                projectile.getVelocity().set(acceleration);
            }
        }

        interval.advance(amount);
        boolean intervalElapsed = interval.intervalElapsed();

        damageInterval.advance(amount);
        boolean damageElapsed = damageInterval.intervalElapsed();

        if (!projectileData.isEmpty()) {
            Iterator<Map.Entry<DamagingProjectileAPI, ProjectileData>> iter = projectileData.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<DamagingProjectileAPI, ProjectileData> entry = iter.next();
                DamagingProjectileAPI projectile = entry.getKey();
                ProjectileData data = entry.getValue();
                float clock = data.clock + amount;

                if (projectile.didDamage() || !engine.isEntityInPlay(projectile)) {
                    iter.remove();
                    continue;
                }
                //arondight start
                if (projectile.getProjectileSpecId().contentEquals("tem_arondight_shot")) {
                    projectile.setCollisionClass(CollisionClass.NONE);

                    if (intervalElapsed) {
                        for (int i = 0; i < 4; i++) {
                            Vector2f vel = new Vector2f(projectile.getVelocity());
                            vel.scale(0.3f);
                            Vector2f.add(vel, MathUtils.getRandomPointInCircle(null, 200f), vel);
                            engine.addSmoothParticle(projectile.getLocation(), vel, (float) Math.random() * 5f + 5f, 1f,
                                    (float) Math.random() * 0.25f + 0.25f, COLOR2);
                        }
                    }

                    if (clock >= 0.2f) {
                        clock -= 0.1f;
                        for (int i = 0; i < 25; i++) {
                            Vector2f vel = new Vector2f(Math.random() > 0.5 ? 0.1f : -0.1f, (float) FastTrig.cos(
                                    Math.random() * Math.PI));
                            VectorUtils.rotate(vel, VectorUtils.getFacing(projectile.getVelocity()), vel);
                            vel.scale(300f);
                            engine.addHitParticle(projectile.getLocation(), vel, (float) Math.random() * 5f + 5f, 1f,
                                    (float) Math.random() * 0.3f + 0.4f, COLOR4);
                        }
                    }

                    List<CombatEntityAPI> targets = CombatUtils.getEntitiesWithinRange(projectile.getLocation(), 20f);
                    CombatEntityAPI shield = null;
                    CombatEntityAPI impact = null;

                    Vector2f collisionPoint = null;

                    targets.remove(projectile.getSource());
                    if (projectile.getSource() != null) {
                        for (ShipAPI module : TEM_Multi.getRoot(projectile.getSource()).getChildModulesCopy()) {
                            targets.remove(module);
                        }
                    }
                    ListIterator<CombatEntityAPI> iter2 = targets.listIterator(targets.size());
                    while (iter2.hasPrevious()) {
                        CombatEntityAPI target = iter2.previous();
                        if (target instanceof ShipAPI) {
                            ShipAPI ship = (ShipAPI) target;
                            if (ship.getOwner() == projectile.getOwner() && (ship.isFighter() || ship.isDrone())) {
                                continue;
                            }
                            if (ship.getCollisionClass() == CollisionClass.NONE) {
                                continue;
                            }
                            if (ship.getShield() != null && ship.getShield().isOn() && ship.getShield().isWithinArc(
                                    projectile.getLocation())) {
                                shield = target;
                                float angle = VectorUtils.getAngle(ship.getShield().getLocation(),
                                        projectile.getLocation());
                                collisionPoint = MathUtils.getPointOnCircumference(ship.getShield().getLocation(),
                                        ship.getShield().getRadius(), angle);
                                break;
                            }
                            if (ship.getHullSpec().getHullId().startsWith("tem_")
                                    && TEM_LatticeShield.shieldLevel(ship) > 0f) {
                                shield = target;
                                Vector2f prevLoc = new Vector2f(projectile.getVelocity());
                                prevLoc.scale(-amount);
                                Vector2f.add(prevLoc, projectile.getLocation(), prevLoc);
                                collisionPoint = CollisionUtils.getCollisionPoint(prevLoc, projectile.getLocation(),
                                        target);
                                if (collisionPoint == null) {
                                    if (CollisionUtils.isPointWithinBounds(projectile.getLocation(), target)) {
                                        collisionPoint = projectile.getLocation();
                                    }
                                }
                                break;
                            }
                        }
                        if (target instanceof DamagingProjectileAPI || target instanceof BattleObjectiveAPI
                                || target instanceof MissileAPI) {
                            continue;
                        }

                        Vector2f prevLoc = new Vector2f(projectile.getVelocity());
                        prevLoc.scale(-amount);
                        Vector2f.add(prevLoc, projectile.getLocation(), prevLoc);
                        if (target.getExactBounds() == null) {
                            collisionPoint = projectile.getLocation();
                        } else {
                            collisionPoint = CollisionUtils.getCollisionPoint(prevLoc, projectile.getLocation(), target);
                        }
                        if (collisionPoint == null) {
                            if (CollisionUtils.isPointWithinBounds(projectile.getLocation(), target)) {
                                collisionPoint = projectile.getLocation();
                            }
                        }

                        if (collisionPoint != null) {
                            impact = target;
                            break;
                        }
                    }

                    if (shield != null && collisionPoint != null && !data.targets.contains(shield)) {
                        CombatUtils.applyForce(shield, projectile.getVelocity(), 400f);
                        engine.addHitParticle(collisionPoint, ZERO, 600f, 1f, 1.5f, COLOR1);
                        for (int i = 0; i < 30; i++) {
                            float angle = VectorUtils.getAngle(collisionPoint, shield.getLocation()) + 180f
                                    + (float) Math.random() * 210f - 105f;
                            if (angle >= 360f) {
                                angle -= 360f;
                            } else if (angle < 0f) {
                                angle += 360f;
                            }
                            engine.addHitParticle(collisionPoint, MathUtils.getPointOnCircumference(null,
                                    (float) Math.random()
                                    * 250f + 250f, angle),
                                    (float) Math.random() * 10f + 10f, 1f, (float) Math.random() * 0.4f
                                    + 0.4f, COLOR2);
                        }
                        TEM_GalatineOnHitEffect.explode(projectile, impact, collisionPoint, engine, false);
                        engine.applyDamage(shield, collisionPoint, projectile.getDamageAmount(), DamageType.ENERGY,
                                projectile.getEmpAmount(), false, false,
                                projectile.getSource());
                        if (shield instanceof ShipAPI) {
                            ShipAPI ship = (ShipAPI) shield;
                            ship.getFluxTracker().increaseFlux(ARONDIGHT_FLUX_DAMAGE, true);
                        }
                        RippleDistortion ripple = new RippleDistortion(collisionPoint, ZERO);
                        ripple.setSize(350f);
                        ripple.setIntensity(50f);
                        ripple.setFrameRate(60f / (1f));
                        ripple.fadeInSize(1f);
                        ripple.fadeOutIntensity(1f);
                        DistortionShader.addDistortion(ripple);
                        genArondightBlast(collisionPoint, ZERO, true);
                        engine.removeEntity(projectile);
                        Global.getSoundPlayer().playSound("tem_arondight_impactshield", 1f, 1.35f, collisionPoint,
                                shield.getVelocity());
                        data.targets.add(shield);
                    } else if (impact != null && collisionPoint != null) {
                        if (!data.targets.contains(impact)) {
                            if (impact instanceof ShipAPI) {
                                CombatUtils.applyForce(TEM_Multi.getRoot((ShipAPI) impact), projectile.getVelocity(),
                                        200f);
                            } else {
                                CombatUtils.applyForce(impact, projectile.getVelocity(), 200f);
                            }
                            engine.addHitParticle(collisionPoint, ZERO, 400f, 1f, 1.5f, COLOR5);
                            engine.spawnExplosion(collisionPoint, impact.getVelocity(), COLOR7, 250f, 1f);
                            engine.applyDamage(impact, collisionPoint, projectile.getDamageAmount(), DamageType.ENERGY,
                                    projectile.getEmpAmount(),
                                    false, false, projectile.getSource());
                            if (impact instanceof ShipAPI) {
                                ShipAPI ship = (ShipAPI) impact;
                                ShipAPI empTarget = ship;
                                engine.spawnEmpArc(projectile.getSource(), collisionPoint, empTarget, empTarget,
                                        DamageType.ENERGY,
                                        projectile.getDamageAmount()
                                        / 5f,
                                        projectile.getEmpAmount() / 5f, 10000f, null, 30f, COLOR7, COLOR2);
                                engine.spawnEmpArc(projectile.getSource(), collisionPoint, empTarget, empTarget,
                                        DamageType.ENERGY,
                                        projectile.getDamageAmount()
                                        / 5f,
                                        projectile.getEmpAmount() / 5f, 10000f, null, 30f, COLOR7, COLOR2);
                                engine.spawnEmpArc(projectile.getSource(), collisionPoint, empTarget, empTarget,
                                        DamageType.ENERGY,
                                        projectile.getDamageAmount()
                                        / 5f,
                                        projectile.getEmpAmount() / 5f, 10000f, null, 30f, COLOR7, COLOR2);
                                ship.getFluxTracker().increaseFlux(ARONDIGHT_FLUX_DAMAGE, false);
                            }
                            StandardLight light = new StandardLight(collisionPoint, ZERO, ZERO, null);
                            light.setColor(COLOR10);
                            light.setSize(450f);
                            light.setIntensity(0.5f);
                            light.fadeOut(1f);
                            LightShader.addLight(light);
                            for (int i = 0; i < 30; i++) {
                                float angle = VectorUtils.getAngle(collisionPoint, impact.getLocation()) + 180f
                                        + (float) Math.random() * 210f - 105f;
                                if (angle >= 360f) {
                                    angle -= 360f;
                                } else if (angle < 0f) {
                                    angle += 360f;
                                }
                                engine.addHitParticle(collisionPoint, MathUtils.getPointOnCircumference(null,
                                        (float) Math.random()
                                        * 300f + 300f,
                                        angle),
                                        (float) Math.random() * 10f + 10f, 1f, (float) Math.random()
                                        * 0.3f + 0.3f, COLOR2);
                            }
                            genArondightBlast(collisionPoint, ZERO, false);
                            Global.getSoundPlayer().playSound("tem_arondight_impact", 1f, 1f, collisionPoint,
                                    impact.getVelocity());
                            data.targets.add(impact);
                        } else if (damageElapsed) {
                            if (impact instanceof ShipAPI) {
                                CombatUtils.applyForce(TEM_Multi.getRoot((ShipAPI) impact), projectile.getVelocity(),
                                        1000f * damageInterval.getIntervalDuration());
                            } else {
                                CombatUtils.applyForce(impact, projectile.getVelocity(),
                                        1000f * damageInterval.getIntervalDuration());
                            }
                            engine.addSmoothParticle(projectile.getLocation(), ZERO, 300f, 0.3f, (float) Math.random()
                                    * 0.3f + 0.85f, COLOR6);
                            engine.spawnExplosion(projectile.getLocation(), impact.getVelocity(), COLOR8, 150f,
                                    (float) Math.random()
                                    * 0.2f + 0.4f);
                            engine.applyDamage(impact, projectile.getLocation(), projectile.getDamageAmount() * 5f
                                    * damageInterval.getIntervalDuration(), DamageType.ENERGY,
                                    projectile.getEmpAmount() * 5f * damageInterval.getIntervalDuration(),
                                    false, false, projectile.getSource());
                            if (impact instanceof ShipAPI) {
                                ShipAPI ship = (ShipAPI) impact;
                                ShipAPI empTarget = ship;
                                engine.spawnEmpArc(projectile.getSource(), new Vector2f(projectile.getLocation()),
                                        empTarget, empTarget, DamageType.ENERGY,
                                        projectile.getDamageAmount() / 5f * 5f
                                        * damageInterval.getIntervalDuration(),
                                        projectile.getEmpAmount() / 5f * 5f
                                        * damageInterval.getIntervalDuration(), 10000f, null,
                                        20f, COLOR9, COLOR3);
                                ship.getFluxTracker().increaseFlux(
                                        ARONDIGHT_FLUX_DAMAGE * 5f * damageInterval.getIntervalDuration(), false);
                            }
                        }
                        clock = 0f;
                    }
                }//arondight end

                if (projectile.getProjectileSpecId().contentEquals("tem_juger_shot")) {
                    if (projectile.getSource() != null) {
                        float life = projectile.getWeapon().getRange() / projectile.getWeapon().getProjectileSpeed();
                        float volume = projectile.getElapsed() < life ? 1f : Math.max((life + 0.5f
                                - projectile.getElapsed()) / 0.5f, 0f);
                        switch (data.variation) {
                            case 0:
                                Global.getSoundPlayer().playLoop("tem_juger_loop1", projectile.getSource(), 1f, volume,
                                        new Vector2f(projectile.getLocation()), ZERO);
                                break;
                            case 1:
                                Global.getSoundPlayer().playLoop("tem_juger_loop2", projectile.getSource(), 1f, volume,
                                        new Vector2f(projectile.getLocation()), ZERO);
                                break;
                            case 2:
                                Global.getSoundPlayer().playLoop("tem_juger_loop3", projectile.getSource(), 1f, volume,
                                        new Vector2f(projectile.getLocation()), ZERO);
                                break;
                        }
                    }

                    if (intervalElapsed && Math.random() > 0.95) {
                        for (int i = 0; i < (int) ((float) Math.random() * 3f); i++) {
                            float angle = (float) Math.random() * 360f;
                            float distance = (float) Math.random() * 150f + 25f;
                            Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), 30f,
                                    projectile.getFacing());
                            Vector2f point2 = MathUtils.getPointOnCircumference(projectile.getLocation(), distance,
                                    angle);
                            engine.spawnEmpArc(projectile.getSource(), point2, projectile, new SimpleEntity(point1),
                                    DamageType.ENERGY, 0f, 0f, 1000f, null, 15f,
                                    COLOR11, COLOR12);
                        }
                    }

                    if (clock >= 0.6f) {
                        clock -= 0.6f;

                        float area = 400f;
                        float damage = projectile.getDamageAmount() * 0.3f;
                        float totalDamage = projectile.getDamageAmount() * 0.75f;
                        int maxTargets = 15;

                        List<CombatEntityAPI> targets = CombatUtils.getEntitiesWithinRange(projectile.getLocation(),
                                area);
                        ListIterator<CombatEntityAPI> iter2 = targets.listIterator(targets.size());
                        while (iter2.hasPrevious()) {
                            CombatEntityAPI target = iter2.previous();
                            if (target.getOwner() == projectile.getOwner()) {
                                iter2.remove();
                                continue;
                            }
                            if (target instanceof ShipAPI) {
                                ShipAPI ship = (ShipAPI) target;
                                if (ship.getCollisionClass() == CollisionClass.NONE || !ship.isAlive()) {
                                    iter2.remove();
                                    continue;
                                }
                            }
                            if (!(target instanceof ShipAPI) && !(target instanceof MissileAPI)) {
                                iter2.remove();
                            }
                        }

                        List<ShipAPI> shipTargets = new ArrayList<>(targets.size());
                        iter2 = targets.listIterator();
                        while (iter2.hasNext()) {
                            CombatEntityAPI target = iter2.next();
                            if (target instanceof ShipAPI) {
                                shipTargets.add((ShipAPI) target);
                                iter2.remove();
                            }
                        }
                        targets.addAll(shipTargets);

                        damage = Math.min(damage, totalDamage / Math.min(targets.size(), maxTargets));
                        Collections.sort(targets, ENTITYSIZE);
                        iter2 = targets.listIterator();
                        int index = 0;
                        while (iter2.hasNext() && index < maxTargets) {
                            CombatEntityAPI target = iter2.next();
                            if (target instanceof ShipAPI) {
                                if (!TEM_Util.isWithinEmpRange(projectile.getLocation(), area, (ShipAPI) target)) {
                                    continue;
                                }
                            }
                            index++;
                            Vector2f point1 = new Vector2f(projectile.getLocation());
                            float size = 125f * damage / projectile.getDamageAmount();
                            engine.spawnEmpArc(projectile.getSource(), point1, new SimpleEntity(point1), target,
                                    DamageType.ENERGY, damage, 0f, area, null, size,
                                    COLOR11, COLOR13);
                        }
                        if (index > 0) {
                            Global.getSoundPlayer().playSound("tem_juger_zap", 0.95f + (float) Math.random() * 0.1f,
                                    0.9f + (float) Math.random() * 0.2f,
                                    projectile.getLocation(), ZERO);
                        }
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("tem_joyeuse_fake_shot")) {
                    if (intervalElapsed) {
                        float chance = interval.getIntervalDuration() * 0.5f;
                        if (projectile.getSource() != null) {
                            chance
                                    *= projectile.getSource().getMutableStats().getProjectileSpeedMult().getModifiedValue();
                            chance *= 1200f
                                    / projectile.getSource().getMutableStats().getEnergyWeaponRangeBonus().computeEffective(1200f);
                        }
                        if ((float) Math.random() >= 1f - chance) {
                            float angle = projectile.getFacing();
                            angle += (float) Math.random() * 120f - 60f;
                            if (angle < 0f) {
                                angle += 360f;
                            } else if (angle >= 360f) {
                                angle -= 360f;
                            }
                            engine.spawnProjectile(projectile.getSource(), projectile.getWeapon(), "tem_joyeuse",
                                    new Vector2f(projectile.getLocation()), angle,
                                    ZERO);
                            Global.getSoundPlayer().playSound("tem_joyeuse_split", 1f, 1f, projectile.getLocation(),
                                    projectile.getVelocity());
                            if (projectile.getSource() != null) {
                                TEM_AnamorphicFlare.createFlare(projectile.getSource(), new Vector2f(
                                        projectile.getLocation()), engine, 0.5f, 0.075f, 0f,
                                        (float) Math.random() * 30f - 15f, 4f,
                                        new Color(255,
                                                TEM_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 50f)),
                                                TEM_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 0f))),
                                        new Color(255,
                                                TEM_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 100f)),
                                                TEM_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 100f))));
                            }
                        }
                    }

                    if (clock >= 0.05f) {
                        clock -= 0.05f;

                        Vector2f point1 = new Vector2f(projectile.getVelocity());
                        point1.scale(-1f * amount);
                        Vector2f.add(point1, projectile.getLocation(), point1);
                        Vector2f point2 = new Vector2f(projectile.getLocation());
                        engine.spawnEmpArc(projectile.getSource(), point1, new SimpleEntity(point1), new SimpleEntity(
                                point2), DamageType.ENERGY, 0f, 0f, 1000f,
                                null, (float) Math.random() * 10f + 10f,
                                new Color(255,
                                        TEM_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 50f)),
                                        TEM_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 50f))),
                                new Color(255,
                                        TEM_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 100f)),
                                        TEM_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 100f))));
                    }

                    projectile.setFacing(projectile.getFacing() + ((float) Math.random() * 90f - 45f) * amount);
                    projectile.getVelocity().set(VectorUtils.rotate(new Vector2f(projectile.getVelocity().length(), 0f),
                            projectile.getFacing(), new Vector2f()));
                }

                data.clock = clock;
            }
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<FXData> fxData = localData.fxData;

        float amount = engine.getElapsedInLastFrame();

        Iterator<FXData> iter = fxData.iterator();
        while (iter.hasNext()) {
            FXData fx = iter.next();

            switch (fx.type) {
                case JUGER_GLOW: {
                    DamagingProjectileAPI proj = (DamagingProjectileAPI) fx.anchor;
                    if (!engine.isEntityInPlay(proj)) {
                        iter.remove();
                        continue;
                    }

                    fx.angle = proj.getFacing();
                    fx.loc = MathUtils.getPointOnCircumference(proj.getLocation(), 20f, proj.getFacing());

                    fx.clock += amount;
                    if (fx.clock >= 0.075f) {
                        fx.clock = Math.min(fx.clock - 0.075f, 0.1f);

                        fx.scale = MathUtils.getRandomNumberInRange(0.9f, 1.25f);
                        fx.alpha = MathUtils.getRandomNumberInRange(0.25f, 0.5f);
                    }

                    float timeAfterFade = Math.max(0f,
                            proj.getElapsed() - (proj.getWeapon().getRange() / proj.getWeapon().getProjectileSpeed()));
                    fx.alphaFade = (0.5f - timeAfterFade) / 0.5f;
                    break;
                }

                case JUGER_BLAST: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = 1f - fx.clock / fx.duration;
                    fx.scaleFade = 1f - 0.5f * fx.clock / fx.duration;
                    break;
                }

                case SECACE_BLAST: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = Math.min(1f, 1.5f * (1f - fx.clock / fx.duration));
                    if (fx.clock >= fx.duration * 0.5f) {
                        fx.scaleFade = 0.75f + 0.5f * fx.clock / fx.duration;
                    }
                    break;
                }

                case GALATINE_BLAST: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = Math.min(1f, 1.5f * (1f - fx.clock / fx.duration));
                    if (fx.clock >= fx.duration * 0.5f) {
                        fx.scaleFade = 0.75f + 0.5f * fx.clock / fx.duration;
                    }
                    break;
                }

                case ARONDIGHT_BLAST: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = Math.min(1f, 1.5f * (1f - fx.clock / fx.duration));
                    if (fx.clock >= fx.duration * 0.5f) {
                        fx.scaleFade = 0.75f + 0.5f * fx.clock / fx.duration;
                    }
                    break;
                }

                case ARONDIGHT_FLARE: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = 1f - fx.clock / fx.duration;
                    fx.scaleFade = 1f - 0.5f * fx.clock / fx.duration;
                    break;
                }

                case CRUCIFIX_BLAST: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = Math.min(1f, 1.5f * (1f - fx.clock / fx.duration));
                    fx.scaleFade = 1f + 2f * fx.clock / fx.duration;
                    break;
                }

                case PAX_BLAST: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = 1f - fx.clock / fx.duration;
                    fx.scaleFade = 1f + 0.5f * fx.clock / fx.duration;
                    break;
                }

                case MERCED_BLAST: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = Math.min(1f, 1.5f * (1f - fx.clock / fx.duration));
                    fx.scaleFade = 1f - 0.5f * fx.clock / fx.duration;
                    break;
                }

                case SENTENIA_BLAST: {
                    if (!engine.isPaused()) {
                        fx.clock += amount;
                    }
                    if (fx.clock >= fx.duration) {
                        iter.remove();
                        continue;
                    }

                    if (!engine.isPaused()) {
                        fx.loc.x += fx.vel.x * amount;
                        fx.loc.y += fx.vel.y * amount;
                        fx.angle += fx.angVel * amount;
                    }

                    fx.alphaFade = Math.min(1f, 1.5f * (1f - fx.clock / fx.duration));
                    fx.scaleFade = 1f - 0.5f * fx.clock / fx.duration;
                    break;
                }

                default:
                    iter.remove();
                    continue;
            }

            if (fx.sprite != null) {
                fx.sprite.setAlphaMult(fx.alpha * fx.alphaFade);
                if (fx.additive) {
                    fx.sprite.setAdditiveBlend();
                } else {
                    fx.sprite.setNormalBlend();
                }
                fx.sprite.setColor(fx.color);
                fx.sprite.setAngle(fx.angle - 90f);
                fx.sprite.setWidth(fx.width * fx.scale * fx.scaleFade);
                fx.sprite.setHeight(fx.height * fx.scale * fx.scaleFade);
                fx.sprite.renderAtCenter(fx.loc.x, fx.loc.y);
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
    }

    private static final class LocalData {

        final List<FXData> fxData = new ArrayList<>(500);
        final Map<DamagingProjectileAPI, ProjectileData> projectileData = new LinkedHashMap<>(500);
        final Map<DamagingProjectileAPI, CombatEntityAPI> projectileTargets = new LinkedHashMap<>(500);
    }

    private static final class FXData {

        FXType type;

        int width = 1;
        int height = 1;

        float angle;
        float angVel;
        float scale = 1f;
        float clock = 0f;
        float duration;
        float alpha = 1f;
        float alphaFade = 1f;
        float scaleFade = 1f;

        boolean additive = false;

        Color color = new Color(255, 255, 255, 255);

        CombatEntityAPI anchor = null;

        SpriteAPI sprite = null;

        Vector2f loc;
        Vector2f vel;

        private FXData(FXType type) {
            this.type = type;
        }
    }

    private static final class ProjectileData {

        float clock;
        List<CombatEntityAPI> targets = new ArrayList<>(5);
        int variation;

        private ProjectileData(float clock, int variation) {
            this.clock = clock;
            this.variation = variation;
        }
    }

    private static enum FXType {
        JUGER_GLOW,
        JUGER_BLAST,
        SECACE_BLAST,
        GALATINE_BLAST,
        ARONDIGHT_BLAST,
        ARONDIGHT_FLARE,
        CRUCIFIX_BLAST,
        PAX_BLAST,
        MERCED_BLAST,
        SENTENIA_BLAST
    }
}
