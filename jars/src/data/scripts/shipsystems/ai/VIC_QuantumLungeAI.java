package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static data.scripts.shipsystems.VIC_QuantumLunge.SPEED_BOOST;

//Base made by Vayra, modified by PureTilt
public class VIC_QuantumLungeAI implements ShipSystemAIScript {

    // setup
    private static final float
            DEGREES = 3f;

    // list of flags to check for using TOWARDS target, using AWAY from target, and NOT USING
    private static final ArrayList<AIFlags>
            TOWARDS = new ArrayList<>(),
    //AWAY = new ArrayList<>(),
    CON = new ArrayList<>();

    static {
        TOWARDS.add(AIFlags.PURSUING);
        TOWARDS.add(AIFlags.HARASS_MOVE_IN);
        //AWAY.add(AIFlags.RUN_QUICKLY);
        //AWAY.add(AIFlags.TURN_QUICKLY);
        //AWAY.add(AIFlags.NEEDS_HELP);
        CON.add(AIFlags.BACK_OFF);
        CON.add(AIFlags.BACK_OFF_MIN_RANGE);
        CON.add(AIFlags.BACKING_OFF);
        CON.add(AIFlags.DO_NOT_PURSUE);
        //CON.add(AIFlags.KEEP_SHIELDS_ON);
    }

    private final IntervalUtil
            FlickTimer = new IntervalUtil(0.2f, 0.4f),
            timer = new IntervalUtil(0.75f, 0.75f);
    private final HashMap<HullSize, Float> TurnMult = new HashMap<>();
    public float
            minPointsToFlank = 0f,
            NeededDur = 0f,
            TimeElapsed = 0f;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;
    private boolean
            DoIFlick = false,
            TargShip = false;

    {
        TurnMult.put(ShipAPI.HullSize.FRIGATE, 0.1f);
        TurnMult.put(ShipAPI.HullSize.DESTROYER, 0.25f);
        TurnMult.put(ShipAPI.HullSize.CRUISER, 0.5f);
        TurnMult.put(ShipAPI.HullSize.CAPITAL_SHIP, 1f);
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }

    // method to check if we're facing within X degrees of target
    private boolean rightDirection(ShipAPI ship, Vector2f targetLocation) {
        Vector2f curr = ship.getLocation();
        float angleToTarget = VectorUtils.getAngle(curr, targetLocation);
        //spawnText(MathUtils.getShortestRotation(angleToTarget, ship.getFacing()) + "", 50f);
        return (Math.abs(MathUtils.getShortestRotation(angleToTarget, ship.getFacing())) <= DEGREES);
    }

    public float flankingScore(ShipAPI ship, ShipAPI target) {
        float flankingScore = 10f;
        if (target == null || ship == null) return -100f;
        if (target.isCapital() && !rightDirection(target, ship.getLocation())) return -100f;
        if (target.isStation() || target.isFighter()) return -100f;
        if (target.isHulk()) return -100;
        if (target.getHullLevel() < 0.15f) return -100;
        if (target.getFleetMember() == null) return -100;


        float shipSide = ship.getOwner();
        float targetSide = target.getOwner();

        //how fast we rotate
        float TimeToMaxSpeedYou = ship.getMaxTurnRate() / ship.getTurnAcceleration();
        float TimeToTurn180You = ((180 - (ship.getMaxTurnRate() * TimeToMaxSpeedYou * 0.5f)) / ship.getMaxTurnRate()) + TimeToMaxSpeedYou;
        float TimeToMaxSpeedTarget = target.getMaxTurnRate() / target.getTurnAcceleration();
        float TimeToTurn180Target = ((180 - (target.getMaxTurnRate() * TimeToMaxSpeedTarget * 0.5f)) / target.getMaxTurnRate()) + TimeToMaxSpeedTarget;
        //Turn advantage add to score
        flankingScore += (TimeToTurn180Target - TimeToTurn180You) * TurnMult.get(target.getHullSize());

        Vector2f shipPos = ship.getLocation();
        Vector2f targetPos = target.getLocation();

        Vector2f MoveDir = VectorUtils.getDirectionalVector(shipPos, targetPos);
        float distPastTarget = (ship.getCollisionRadius() + target.getCollisionRadius()) * 0.75f;
        Vector2f ExitPos = new Vector2f(targetPos.x + (distPastTarget * MoveDir.x), targetPos.y + (distPastTarget * MoveDir.y));
        Vector2f CheckPos = new Vector2f(ExitPos.x + (400 * MoveDir.x), ExitPos.y + (400 * MoveDir.y));

        //spawnText("there", CheckPos);

        float enemyScore = 0f;
        float allyScore = 0f;
        List<ShipAPI> shipInExitRange = CombatUtils.getShipsWithinRange(CheckPos, distPastTarget + 1500);
        for (ShipAPI toCheck : shipInExitRange) {
            if (toCheck.getFleetMember() == null) continue;
            if (toCheck.getOwner() == shipSide) {
                if (toCheck != ship) {
                    allyScore += toCheck.getFleetMember().getDeploymentPointsCost();
                }
            } else if (toCheck.getOwner() == targetSide) {
                enemyScore += toCheck.getFleetMember().getDeploymentPointsCost();
            }
        }

        if (ship.getFleetMember().getDeploymentPointsCost() > 0) {
            allyScore += ship.getFleetMember().getDeploymentPointsCost();
        }
        float totalScore = allyScore - enemyScore;
        flankingScore += totalScore;

        //how much of target's HP left

        float targetDmgMult = 400 / (target.getArmorGrid().getArmorRating() + 400);
        float HullPercent = (target.getHitpoints() / targetDmgMult) / (target.getMaxHitpoints() / targetDmgMult);
        float HPLeft = HullPercent;
        if (target.getShield() != null) {
            float shieldPercent = ((target.getMaxFlux() - target.getCurrFlux()) * target.getShield().getFluxPerPointOfDamage()) / (target.getMaxFlux() * target.getShield().getFluxPerPointOfDamage());
            float HullToShieldRatio = ((target.getMaxFlux() - target.getCurrFlux()) / (target.getMaxHitpoints() / targetDmgMult));
            HPLeft = shieldPercent * HullToShieldRatio + HullPercent * (1 - HullToShieldRatio);
        }

        flankingScore -= target.getFleetMember().getDeploymentPointsCost() - (target.getFleetMember().getDeploymentPointsCost() * HPLeft);

        //spawnText(flankingScore + "", 60f);
        return flankingScore;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        // don't check if paused
        if (engine.isPaused()) return;

        if (ship.getSystem().isStateActive() && TargShip) {
            TimeElapsed += amount;
            if (TimeElapsed >= NeededDur) {
                ship.useSystem();
                TargShip = false;
                TimeElapsed = 0f;
            }
        }

        //Disable system if use it not for movement
        if (DoIFlick) {
            FlickTimer.advance(amount);
            if (FlickTimer.intervalElapsed()) {
                ship.useSystem();
                DoIFlick = false;
                FlickTimer.setElapsed(0f);
                //spawnText("flick", 0f);
            } else {
                return;
            }
        }

        // don't check if timer not up
        timer.advance(amount);
        if (!timer.intervalElapsed()) {
            return;
        }

        // don't use if can't use
        if (!AIUtils.canUseSystemThisFrame(ship)) {
            return;
        }

        if (!DoIFlick && ship.getEngineController().isFlamedOut()) {
            ship.useSystem();
            DoIFlick = true;
            //spawnText("DoFlick", 0f);
            return;
        }

        // setup variables
        boolean useMe = false;
        Vector2f targetLocation = null;
        AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
        float speed = SPEED_BOOST;

        // First priority: use to retreat if ordered to retreat. Overrides/ignores the "useMe" system and AI flag checks.
        if (assignment != null && assignment.getType() == CombatAssignmentType.RETREAT) {
            if (ship.getOwner() == 1 || (ship.getOwner() == 0 && engine.getFleetManager(FleetSide.PLAYER).getGoal() == FleetGoal.ESCAPE)) {
                targetLocation = new Vector2f(ship.getLocation().x, ship.getLocation().y + 800f); // if ship is enemy OR in "escape" type battle, target loc is UP
            } else {
                targetLocation = new Vector2f(ship.getLocation().x, ship.getLocation().y - 800f); // if ship is player's, target loc is DOWN
            }
            if (rightDirection(ship, targetLocation)) {
                ship.useSystem();
                //spawnText("retreat", 0f);
            }
            return;  // prevents the AI from activating the ship's system while retreating and facing the wrong direction
            // thanks, Starsector forums user Morathar
        }

        for (AIFlags f : CON) {
            if (flags.hasFlag(f)) {
                return;
            }
        }

        // if we have an assignment, set our target loc to it
        // otherwise, if we have a hostile target, set our target loc to intercept it
        if (assignment != null && assignment.getTarget() != null) {
            targetLocation = assignment.getTarget().getLocation();
            TargShip = true;
        } else if (target != null && target.getOwner() != ship.getOwner()) {
            targetLocation = AIUtils.getBestInterceptPoint(ship.getLocation(), ship.getVelocity().length() + speed, target.getLocation(), target.getVelocity());
            TargShip = true;
        }

        if (targetLocation == null) {
            return;
        }

        if (target != null) {
            NeededDur = (MathUtils.getDistance(ship.getLocation(), targetLocation) + (ship.getCollisionRadius() + target.getCollisionRadius())) / speed;
            if ((rightDirection(ship, targetLocation)) && NeededDur <= ship.getSystem().getChargeActiveDur() && (flankingScore(ship, target) > minPointsToFlank)) {
                useMe = true;
                //spawnText("Flank/" + NeededDur, 0f);
            }
        }


        for (AIFlags f : TOWARDS) {
            if (flags.hasFlag(f) && rightDirection(ship, targetLocation)) {
                useMe = true;
                //spawnText("towards", 0f);
            }
        }

        /*
        for (AIFlags f : AWAY) {
            if (flags.hasFlag(f) && !rightDirection(ship, targetLocation)) {
                useMe = true;
                //spawnText("away",0f);
            }
        }
        */

        if (useMe) {
            ship.useSystem();
            if (TargShip) {
                assert target != null;
                NeededDur = (MathUtils.getDistance(ship.getLocation(), targetLocation) + (ship.getCollisionRadius() + target.getCollisionRadius())) / speed;
                if (NeededDur > ship.getSystem().getChargeActiveDur())
                    NeededDur = ship.getSystem().getChargeActiveDur();
            }
        }
    }

    public void spawnText(String text, Float offset) {
        engine.addFloatingText(new Vector2f(ship.getLocation().x, ship.getLocation().y + offset), text, 60, Color.WHITE, ship, 0.25f, 0.25f);
    }
    public void spawnText(String text, Vector2f pos) {
        engine.addFloatingText(pos, text, 60, Color.WHITE, null, 0.25f, 0.25f);
    }
}
