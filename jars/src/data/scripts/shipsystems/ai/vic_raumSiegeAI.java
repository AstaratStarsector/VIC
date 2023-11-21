package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static data.scripts.shipsystems.vic_raumSiege.siegeRange;
import static org.lazywizard.lazylib.combat.AIUtils.getEnemiesOnMap;

public class vic_raumSiegeAI implements ShipSystemAIScript {

    CombatEngineAPI engine;
    ShipAPI ship;
    ShipSystemAPI system;
    ShipwideAIFlags flags;

    IntervalUtil timer = new IntervalUtil(0.25f, 0.5f);

    float
            maxRange = 0;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.engine = engine;
        this.ship = ship;
        this.system = system;
        this.flags = flags;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getSpec().getType().equals(WeaponAPI.WeaponType.MISSILE)) continue;
            float range = weapon.getRange();
            if (range > maxRange) maxRange = range;
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (ship == null) return;
        timer.advance(amount);
        if (!timer.intervalElapsed() || ship.getSystem().getState() == ShipSystemAPI.SystemState.COOLDOWN) return;
        boolean activate = false;
        boolean siege = false;
        if (ship.getCustomData().get("vic_raumSiedge") instanceof Boolean)
            siege = (boolean) ship.getCustomData().get("vic_raumSiedge");
        List<ShipAPI> farTargets = new ArrayList<>();
        float nearFP = 0;
        float farFP = 0;
        for (ShipAPI enemy : getEnemiesOnMap(ship)) {
            if (MathUtils.isWithinRange(ship.getLocation(), enemy.getLocation(), maxRange * 0.8f)) {
                nearFP += enemy.getHullSpec().getFleetPoints();
            } else if (MathUtils.isWithinRange(ship.getLocation(), enemy.getLocation(), maxRange * (1 + siegeRange) + 300)) {
                farFP += enemy.getHullSpec().getFleetPoints();
                farTargets.add(enemy);
            }
        }
        CombatFleetManagerAPI.AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
        if (siege) {
            if (farTargets.isEmpty() || nearFP >= ship.getHullSpec().getFleetPoints() * 0.5f) {
                activate = true;
            }
            if ((assignment != null) && (assignment.getType() == CombatAssignmentType.RETREAT)) {
                activate = true;
            }
        } else {
            if ((assignment == null) || (assignment.getType() != CombatAssignmentType.RETREAT)) {
                if (nearFP <= ship.getHullSpec().getFleetPoints() * 0.5f && farFP >= ship.getHullSpec().getFleetPoints() * 0.5f) {
                    activate = true;
                }
            }
        }
        if (activate) ship.useSystem();
    }
}
