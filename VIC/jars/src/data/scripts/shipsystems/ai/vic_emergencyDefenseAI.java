package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class vic_emergencyDefenseAI implements ShipSystemAIScript {

    private final IntervalUtil
            timer = new IntervalUtil(1f, 2f);
    private ShipAPI ship;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        timer.advance(amount);
        if (!timer.intervalElapsed()) {
            return;
        }
        // setup variables
        boolean useMe = false;
        float shipSide = ship.getOwner();

        float enemyScore = 0f;
        List<ShipAPI> shipInExitRange = CombatUtils.getShipsWithinRange(ship.getLocation(), 850);
        for (ShipAPI toCheck : shipInExitRange) {
            if (toCheck.getOwner() != shipSide) {
                if (toCheck != ship) {
                    if (toCheck.getFleetMember() != null)
                        enemyScore += toCheck.getFleetMember().getDeploymentPointsCost();
                }
            }
        }

        if (enemyScore >= 10f) {
            useMe = true;
        }

        if (useMe) {
            ship.useSystem();
        }
    }

}
