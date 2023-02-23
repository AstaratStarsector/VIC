package data.scripts.utilities;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.weapons.autofireAI.vic_VerliokaAutofireAI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;

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
}
