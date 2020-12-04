package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class vic_emergencyDefenseAI implements ShipSystemAIScript {

    private final IntervalUtil
            timer = new IntervalUtil(0.5f, 1f);
    boolean allBombers = true;
    private ShipAPI ship;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (!bay.getWing().getSpec().isBomber()) allBombers = false;
            if (!allBombers) break;
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        timer.advance(amount);
        if (!timer.intervalElapsed()) {
            return;
        }
        boolean nonReturning = true;
        if (allBombers && ship.getSystem().isStateActive()) {
            for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()){
                if (bay.getWing().isReturning(bay.getWing().getLeader())) nonReturning = false;
                if (!nonReturning) break;
            }
            if (nonReturning && ship.getFluxLevel() < 0.3) ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
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

        boolean allReturns = true;
        if (allBombers) {
            for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()){
                if (!bay.getWing().isReturning(bay.getWing().getLeader())) allReturns = false;
                if (!allReturns) break;
            }
        } else {
            allReturns = false;
        }
        if (allReturns) useMe = true;

        if (enemyScore >= 10f) useMe = true;

        if (useMe) {
            ship.useSystem();
        }
    }

}
