package data.scripts.utilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class vic_finders {

    public static ShipAPI nearestEnemyFighterInRange(CombatEntityAPI entity, float range) {

        ShipAPI closest = null;
        float distanceSquared, closestDistanceSquared = Float.MAX_VALUE;

        for (ShipAPI ship : AIUtils.getNearbyEnemies(entity, range)) {

            if (!ship.isFighter()) continue;
            distanceSquared = MathUtils.getDistanceSquared(ship.getLocation(),
                    entity.getLocation());

            if (distanceSquared < closestDistanceSquared) {
                closest = ship;
                closestDistanceSquared = distanceSquared;
            }

        }
        return closest;
    }

    public static ShipAPI nearestEnemyFighterInWeaponArc(WeaponAPI weapon) {

        ShipAPI closest = null;
        float distanceSquared, closestDistanceSquared = Float.MAX_VALUE;
        float range = weapon.getRange();

        for (ShipAPI ship : AIUtils.getNearbyEnemies(weapon.getShip(), range)) {

            if (!ship.isFighter()) continue;
            float angleToTarget = VectorUtils.getAngle(weapon.getLocation(), ship.getLocation());
            float angle = Math.abs(MathUtils.getShortestRotation(angleToTarget, weapon.getSlot().getAngle() + weapon.getShip().getFacing()));
            if (angle > weapon.getArc()) continue;
            distanceSquared = MathUtils.getDistanceSquared(ship.getLocation(), weapon.getShip().getLocation());

            if (distanceSquared < closestDistanceSquared) {
                closest = ship;
                closestDistanceSquared = distanceSquared;
            }

        }
        return closest;
    }

    public static List<CombatEntityAPI> damagableEnemiesInRangeWOAsteroids(Vector2f location, float range, int side) {

        List<CombatEntityAPI> entities = new ArrayList<>();

        for (ShipAPI ship : Global.getCombatEngine().getShips())
        {
            if (ship.getOwner() != side && !ship.isPhased() && ship.getCollisionClass() != CollisionClass.NONE && MathUtils.isWithinRange(ship, location, range))
            {

                entities.add(ship);
            }
        }

        // This also includes missiles
        for (CombatEntityAPI proj : Global.getCombatEngine().getProjectiles())
        {
            if (proj instanceof MissileAPI && proj.getOwner() != side && proj.getCollisionClass() != CollisionClass.NONE &&  MathUtils.isWithinRange(proj, location, range))
            {
                entities.add(proj);
            }
        }

        return entities;
    }
}
