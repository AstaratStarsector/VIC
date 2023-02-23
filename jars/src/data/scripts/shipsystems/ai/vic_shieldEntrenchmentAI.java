package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class vic_shieldEntrenchmentAI implements ShipSystemAIScript {

    ShipAPI ship;
    ShipSystemAPI system;

    IntervalUtil timer = new IntervalUtil(0.25f, 0.5f);

    float
            RANGE = 1600,
            PROJ_THRESHOLD = 500;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        timer.advance(amount);
        if (timer.intervalElapsed()) {
            //activation check
            if (!system.isActive()) {
                if (!ship.isRetreating() && AIUtils.canUseSystemThisFrame(ship)) {
                    CombatEngineAPI engine = Global.getCombatEngine();
                    //projectile danger
                    List<DamagingProjectileAPI> danger_projs = getProjectileThreats(ship);

                    float totalIncomingDamage = 0;
                    for (DamagingProjectileAPI projectile : danger_projs) {
                        totalIncomingDamage += projectile.getDamageAmount();
                    }

                    //debug
                    //engine.addFloatingText(new Vector2f(ship.getLocation().x, ship.getLocation().y + 20), "proj threat: " + totalIncomingDamage, 20, Color.GREEN, ship, .1f, 1f);

                    if (totalIncomingDamage >= PROJ_THRESHOLD) {
                        //immediate high damage danger, flicker the system on
                        ship.useSystem();
                        timer.setElapsed(-0.5f);
                        ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON, 1f);
                        return;
                    }

                    float shipDanger = 0;
                    //enemy ships threat
                    for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, RANGE)) {
                        if (enemy.isDrone() || enemy.isFighter()) continue;

                        shipDanger += enemy.getHullSpec().getFleetPoints() * (1.5f - (MathUtils.getDistanceSquared(this.ship, enemy) / 1000000));
                    }
                    //debug
                    //engine.addFloatingText(ship.getLocation(), "ship threat: " + shipDanger, 20, Color.RED, ship, .1f, 1f);

                    //the higher the ship's flux, the more conservative the AI gets, non-linear
                    float flux_rating = ship.getFluxLevel() * 0.75f * ship.getHullSpec().getFleetPoints();

                    //debug
                    //engine.addFloatingText(new Vector2f(ship.getLocation().x, ship.getLocation().y - 20), "flux rating: " + flux_rating, 20, Color.BLUE, ship, .1f, 1f);

                    if (shipDanger + flux_rating > ship.getHullSpec().getFleetPoints()) {
                        ship.useSystem();
                        timer.setElapsed(-1f);
                        ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON, 1f);
                    }
                }
            } else {
                //deactivation logic, pretty simple because most times it will be automatic when the ship lowers its shield
                float deactivationRange = 1000;
                for (WeaponAPI w : ship.getAllWeapons()) {
                    if (w.getType().equals(WeaponAPI.WeaponType.MISSILE)) continue;
                    if (w.getRange() > deactivationRange) {
                        deactivationRange = w.getRange();
                    }
                }
                deactivationRange *= 0.9f;
                deactivationRange *= ship.getMutableStats().getBallisticWeaponRangeBonus().getBonusMult();

                if (getProjectileThreats(ship).isEmpty() && AIUtils.getNearbyEnemies(ship, deactivationRange).isEmpty()) {
                    ship.useSystem();
                }
            }
        }
    }


    //detect high danger missile and shots threats
    private List<DamagingProjectileAPI> getProjectileThreats(ShipAPI ship) {

        List<DamagingProjectileAPI> danger_projs = new ArrayList<>();

        //get high damage proj aimed at the ship
        for (DamagingProjectileAPI projectile : CombatUtils.getProjectilesWithinRange(ship.getLocation(), 800)) {
            if (projectile.getDamageAmount() >= 100 && Math.abs(MathUtils.getShortestRotation(projectile.getFacing(), VectorUtils.getAngle(projectile.getLocation(), ship.getLocation()))) < 45) {
                danger_projs.add(projectile);
            }
        }

        for (MissileAPI missile : AIUtils.getNearbyEnemyMissiles(ship, 800)) {
            if (missile.getDamageAmount() >= 150) {
                if (missile.isGuided()) {
                    danger_projs.add(missile);
                } else if (Math.abs(MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), ship.getLocation()))) < 45) {
                    danger_projs.add(missile);
                }
            }
        }
        return danger_projs;
    }

}