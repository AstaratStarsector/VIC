package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import org.json.JSONException;
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
            FlickTimer = new IntervalUtil(0.3f, 0.4f),
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
    private float getAngle(ShipAPI ship, Vector2f targetLocation) {
        Vector2f curr = ship.getLocation();
        float angleToTarget = VectorUtils.getAngle(curr, targetLocation);
        //spawnText(MathUtils.getShortestRotation(angleToTarget, ship.getFacing()) + "", 50f);
        return Math.abs(MathUtils.getShortestRotation(angleToTarget, ship.getFacing()));
    }

    public static int orientation(Vector2f p, Vector2f q, Vector2f r) {
        float val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0;  // collinear
        return (val > 0)? 1: 2; // clock or counterclock wise
    }

    public static List<Vector2f> convexHull(List<Vector2f> points) {
        int n = points.size();
        if (n < 3) {
            return points;
        }

        List<Vector2f> hull = new ArrayList<>();

        int l = 0;
        for (int i = 1; i < n; i++) {
            if (points.get(i).x < points.get(l).x) {
                l = i;
            }
        }

        int p = l, q;
        do {
            hull.add(points.get(p));

            q = (p + 1) % n;
            for (int i = 0; i < n; i++) {
                if (orientation(points.get(p), points.get(i), points.get(q)) == 2) {
                    q = i;
                }
            }
            p = q;
        } while (p != l);

        return hull;
    }

    private boolean isPositionInsidePolygon(List<Vector2f> polygon, Vector2f position) {
        float x = position.x;
        float y = position.y;

        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            float xi = polygon.get(i).x;
            float yi = polygon.get(i).y;
            float xj = polygon.get(j).x;
            float yj = polygon.get(j).y;

            boolean intersect = ((yi > y) != (yj > y))
                    && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }

        return inside;
    }

    public float getFlankingScore(ShipAPI ship, ShipAPI target) {
        float flankingScore = 10f;
        if (target == null || ship == null) return -100f;
        //if (target.isCapital() && !(getAngle(target, ship.getLocation()) <= DEGREES)) return -100f;
        if (target.isStation() || target.isFighter()) return -100f;
        if (target.isHulk()) return -100;
        if (target.getHullLevel() < 0.15f) return -100;
        if (target.getFleetMember() == null) return -100;
        if (target.getHullSpec().isPhase()) return -100;
        if (getAngle(target, ship.getLocation()) > 50) return -100;


        float shipSide = ship.getOwner();
        float targetSide = target.getOwner();

        //how fast they rotate
        float timeToMaxTurnRateTarget = target.getMaxTurnRate() / target.getTurnAcceleration();
        float timeToTurnOnTarget = ((120 - (target.getMaxTurnRate() * timeToMaxTurnRateTarget * 0.5f)) / target.getMaxTurnRate()) + timeToMaxTurnRateTarget;
        //Turn advantage add to score
        flankingScore += Math.min(40, timeToTurnOnTarget * TurnMult.get(target.getHullSize()) * 4f);

        // extra adjustments, since flanking a very small ship or ship that is very agile in general is probably not a great idea
        // in turn, the turn multipliers were raised overall
        if (target.getHullSize().equals(ShipAPI.HullSize.FRIGATE)) {
            flankingScore -= 50;
        } else if (target.getHullSize().equals(ShipAPI.HullSize.DESTROYER)) {
            flankingScore -= 25;
        } else if (target.getHullSize().equals(ShipAPI.HullSize.CRUISER)) {
            flankingScore -= 10;
        }

        // avoid trying to flank stinky ship systems that could be used against us before they "finish" getting flanked
        ShipSystemAPI targetSystem = target.getSystem();
        if (   targetSystem != null && (targetSystem.isStateActive()
                || (targetSystem.getCooldown() > 0 && (!targetSystem.isCoolingDown() || targetSystem.getCooldownRemaining() < timeToTurnOnTarget))
                || (targetSystem.getMaxAmmo() > 0 && (!targetSystem.isOutOfAmmo() || targetSystem.getAmmoPerSecond() * timeToTurnOnTarget < 1f - targetSystem.getAmmoReloadProgress())))) {

            String systemAIType = "";
            int activeSpeedIncrease = 0;
            try {
                systemAIType = targetSystem.getSpecAPI().getSpecJson().getString("aiType");
                activeSpeedIncrease = targetSystem.getSpecAPI().getSpecJson().getJSONObject("aiHints").getInt("activeSpeedIncrease");

            }catch (JSONException ignored) {

            }

            //don't try to flank ships with teleports
            if (systemAIType.equals("PHASE_TELEPORTER_2") || systemAIType.equals("PHASE_DISPLACER")){
                return -100;
            }

            if (activeSpeedIncrease > 10 && targetSystem.getCooldownRemaining() <= 3){
                flankingScore -= activeSpeedIncrease / 2f;
            }

            //do more general check on top will have it for now
            // these systems give a lot of mobility, making it harder to flank them
            /*
            if (targetSystem.getId().equals("plasmajets")) {
                flankingScore -= 80;
            }
            if (targetSystem.getId().equals("inferniuminjector")) {
                flankingScore -= 60;
            }
            if (targetSystem.getId().equals("maneuveringjets")) {
                flankingScore -= 60;
            }
             */
        }


        Vector2f shipPos = ship.getLocation();
        Vector2f targetPos = target.getLocation();

        Vector2f MoveDir = VectorUtils.getDirectionalVector(shipPos, targetPos);
        float distPastTarget = (ship.getCollisionRadius() + target.getCollisionRadius()) * 0.75f;
        Vector2f ExitPos = new Vector2f(targetPos.x + (distPastTarget * MoveDir.x), targetPos.y + (distPastTarget * MoveDir.y));
        ExitPos = new Vector2f(ExitPos.x + (400 * MoveDir.x), ExitPos.y + (400 * MoveDir.y));

        //spawnText("there", CheckPos);

        float enemyScore = 0f;
        float allyScore = 0f;

        // this time, only look for allies that could potentially also engage the target by proximity
        List<ShipAPI> shipsInEngagementRange = CombatUtils.getShipsWithinRange(targetPos, 1100);
        for (ShipAPI toCheck : shipsInEngagementRange) {
            if (toCheck.getFleetMember() == null
                    || toCheck.isHulk()
                    || toCheck.isFighter()) continue;

            if (toCheck.getOwner() == shipSide) {
                if (toCheck != ship) {
                    float checkAngle = getAngle(toCheck, targetPos);
                    float checkDistance = MathUtils.getDistance(toCheck.getLocation(), targetPos);

                    allyScore += 5;
                    if (checkDistance < 700) {
                        allyScore += 10;
                    }
                    if (checkAngle < 60) {
                        allyScore += toCheck.getFleetMember().getDeploymentPointsCost() / 3;
                    }
                }
            }
        }
        if (allyScore > 60) {
            allyScore = 60;
        }

        // try to weigh enemies by how close they will be and weigh enemies facing away less
        List<Vector2f> enemyShipPositions = new ArrayList<>();
        List<ShipAPI> shipsInExitRange = CombatUtils.getShipsWithinRange(ExitPos, 1800);
        for (ShipAPI toCheck : shipsInExitRange) {
            if (toCheck.getFleetMember() == null
                    || toCheck.isHulk()
                    || toCheck.isFighter()) continue;

            if (toCheck.getOwner() == targetSide) {
                enemyShipPositions.add(toCheck.getLocation());
                if (toCheck != target) {
                    float checkAngle = getAngle(toCheck, ExitPos);
                    float checkDistance = MathUtils.getDistance(toCheck.getLocation(), ExitPos);


                    if (checkDistance < 1100) {
                        enemyScore += 10;
                    }
                    if (checkDistance < 700) {
                        enemyScore += 10;
                    }
                    if (checkAngle < 120 && checkDistance < 900) {
                        enemyScore += toCheck.getFleetMember().getDeploymentPointsCost() / 4 + 10;
                    }
                    if (checkAngle < 60 && checkDistance < 1300) {
                        enemyScore += toCheck.getFleetMember().getDeploymentPointsCost() / 2;
                    }
                }
            }
        }

        if (ship.getFleetMember().getDeploymentPointsCost() > 0) {
            //allyScore += ship.getFleetMember().getDeploymentPointsCost()/2 + 20;
        }
        float totalScore = allyScore - enemyScore;
        flankingScore += totalScore;

        // if the enemy fleet would surround the exit area, try to avoid it
        List<Vector2f> enemyPolygon = convexHull(enemyShipPositions);
        if (enemyPolygon.size() > 2 && isPositionInsidePolygon(enemyPolygon, ExitPos)) {
            return -100;
        }

        //how much of target's HP left
        float targetDmgMult = 400 / (target.getArmorGrid().getArmorRating() + 400);
        float HullPercent = (target.getHitpoints() / targetDmgMult) / (target.getMaxHitpoints() / targetDmgMult);
        float HPLeft = HullPercent;
        if (target.getShield() != null) {
            float shieldPercent = ((target.getMaxFlux() - target.getCurrFlux()) * target.getShield().getFluxPerPointOfDamage()) / (target.getMaxFlux() * target.getShield().getFluxPerPointOfDamage());
            float HullToShieldRatio = ((target.getMaxFlux() - target.getCurrFlux()) / (target.getMaxHitpoints() / targetDmgMult));
            HPLeft = shieldPercent * HullToShieldRatio + HullPercent * (1 - HullToShieldRatio);
        }

        //flankingScore -= target.getFleetMember().getDeploymentPointsCost() - (target.getFleetMember().getDeploymentPointsCost() * HPLeft);

        // check our own vital statistics
        if (ship.getShield() != null) {
            float usableFlux = ship.getMaxFlux() - ship.getCurrFlux();
            float usableFluxRatio = usableFlux / ship.getMaxFlux();
            ship.getHardFluxLevel();

            flankingScore -= 200 - (Math.min(usableFluxRatio + 0.3, 1f) * 200);
            if (usableFluxRatio < 0.4) {
                return -100;
            }
        }
        if (ship.getHitpoints() / ship.getMaxHitpoints() < 0.3 || ship.getHitpoints() < 6000) {
            return -100;
        }

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
            if (getAngle(ship, targetLocation) <= DEGREES) {
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
            NeededDur = (100 + MathUtils.getDistance(ship.getLocation(), targetLocation) + 0.75f * (ship.getCollisionRadius() + target.getCollisionRadius())) / speed;
            if ((getAngle(ship, targetLocation) <= DEGREES) && NeededDur <= ship.getSystem().getChargeActiveDur() && (getFlankingScore(ship, target) > minPointsToFlank)) {
                useMe = true;
                //spawnText("Flank/" + NeededDur, 0f);
            }
        }


        for (AIFlags f : TOWARDS) {
            if (flags.hasFlag(f) && getAngle(ship, targetLocation) <= DEGREES) {
                //useMe = true;
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
                NeededDur = (100 + MathUtils.getDistance(ship.getLocation(), targetLocation) + 0.75f * (ship.getCollisionRadius() + target.getCollisionRadius())) / speed;
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
