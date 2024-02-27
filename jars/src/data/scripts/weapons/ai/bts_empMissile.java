package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.*;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class bts_empMissile implements MissileAIPlugin, GuidedMissileAI {

    final float ENGINE_DEAD_TIME_MAX = 0.75f; // Max time until engine burn starts
    final float ENGINE_DEAD_TIME_MIN = 0.25f; // Min time until engine burn starts
    final float LEAD_GUIDANCE_FACTOR = 0.6f;
    final float LEAD_GUIDANCE_FACTOR_FROM_ECCM = 0.4f;
    final float FIRE_INACCURACY = 0f; // Set-once for entire shot lifetime leading offset
    final float AIM_THRESHOLD = 0.5f; // Multiplied by collision radius, how much it can be off by when deciding to MIRV
    final float maxMirvAngle = 5;
    final float MIRV_DISTANCE = 350f;
    final float TIME_BEFORE_CAN_MIRV = 1f; // Min time before can MIRV
    final float FLARE_OFFSET = -9f; // Set to engine location matched to missile projectile file
    final Color FLARE_COLOR = new Color(200, 165, 55, 255);
    final Color SMOKE_COLOR = new Color(155, 145, 135, 150);
    final boolean STAGE_ONE_EXPLODE = false;
    final boolean STAGE_ONE_FLARE = true; // Glow particle visual when second stage is litup
    final boolean STAGE_ONE_TRANSFER_DAMAGE = true; // Only used for missile submunitions, which this is not
    final boolean STAGE_ONE_TRANSFER_MOMENTUM = true;
    final float SUBMUNITION_VELOCITY_MOD_MAX = 200f; // Max fudged extra velocity added to the submunitions
    final float SUBMUNITION_VELOCITY_MOD_MIN = 100f; // Min fudged extra velocity added to the submunitions
    final int NUMBER_SUBMUNITIONS = 1;
    final float SUBMUNITION_RELATIVE_OFFSET = 0f; // How much each submunition's aim point is offset relative to others if multiple
    final float SUBMUNITION_INACCURACY = 0f; // How much random offset from the ^ aim point if multiple
    final String STAGE_TWO_WEAPON_ID = "vic_hungruf_sub";
    final String STAGE_TWO_SOUND_ID = "sabot_srm_split";
    final float VELOCITY_DAMPING_FACTOR = 0.5f;
    final float WEAVE_FALLOFF_DISTANCE = 1000f; // Weaving stops entirely at 0 distance
    final float WEAVE_SINE_A_AMPLITUDE = 15f; // Degrees offset
    final float WEAVE_SINE_A_PERIOD = 3f;
    final float WEAVE_SINE_B_AMPLITUDE = 25f; // Degrees offset
    final float WEAVE_SINE_B_PERIOD = 6f;
    final Vector2f ZERO = new Vector2f();
    float engineDeadTimer;
    float timeAccum = 0f;
    final float weaveSineAPhase;
    final float weaveSineBPhase;
    final float inaccuracy;
    boolean readyToFly = false;
    final float eccmMult;
    boolean stopEngineToTurn = false;

    public bts_empMissile(MissileAPI missile, ShipAPI launchingShip) {
        super();

        this.missile = missile;
        this.launchingShip = launchingShip;

        defaultInitialTargetingBehavior(launchingShip);

        weaveSineAPhase = (float) (Math.random() * Math.PI * 2.0);
        weaveSineBPhase = (float) (Math.random() * Math.PI * 2.0);

        engineDeadTimer = MathUtils.getRandomNumberInRange(ENGINE_DEAD_TIME_MIN, ENGINE_DEAD_TIME_MAX);

        eccmMult = 0.5f; // How much ECCM affects FIRE_INACCURACY

        inaccuracy = MathUtils.getRandomNumberInRange(-FIRE_INACCURACY, FIRE_INACCURACY);
    }

    public float getInaccuracyAfterECCM() {
        float eccmEffectMult = 1;
        if (launchingShip != null) {
            eccmEffectMult = 1 - eccmMult * launchingShip.getMutableStats().getMissileGuidance().getModifiedValue();
        }
        if (eccmEffectMult < 0) {
            eccmEffectMult = 0;
        }

        return inaccuracy * eccmEffectMult;
    }

    /**
     * Returns true if a line extrapolated {@code distance} ahead of the
     * missile's current position intersects a circle centered on the target's
     * midpoint. The circle's radius is equal to its collision radius *
     * {@code AIM_THRESHOLD}.
     *
     * @param missilePos \\
     * @param targetPos  Target position (not necessarily the target ship's
     *                   actual position, more usually the computed intercept point)
     * @param distance   Distance between missile and target.
     * @param heading    Missile's heading.
     * @param radius     Target's collision radius.
     * @return           true if intersects
     */
    public boolean isWithinMIRVAngle(Vector2f missilePos, Vector2f targetPos,
                                     float distance, float heading, float radius) {
        Vector2f endpoint = MathUtils.getPointOnCircumference(missilePos, distance, heading);

        //Global.getCombatEngine().addHitParticle(endpoint,missile.getVelocity(),40,1,0,0.05f,Color.RED);
        radius = (radius * AIM_THRESHOLD) + 50f;

        return CollisionUtils.getCollides(missilePos, endpoint, targetPos, radius);
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        if (missile.isFading() || missile.isFizzling()) {
            return;
        }

        boolean mirvNow = false;

        // Do not fly forwards until we have finished engineDeadTimer
        if (!readyToFly) {
            if (engineDeadTimer > 0f) {
                engineDeadTimer -= amount;
                if (engineDeadTimer <= 0f) {
                    readyToFly = true;
                }
            }
        }

        timeAccum += amount;

        // If we have a valid target, turn to face desired intercept point
        if (acquireTarget()) {
            if (engineDeadTimer <= 0f) {
                readyToFly = true;
            }
            float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
            float guidance = LEAD_GUIDANCE_FACTOR;
            if (missile.getSource() != null) {
                guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                        - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * LEAD_GUIDANCE_FACTOR_FROM_ECCM;
            }
            Vector2f guidedTarget = intercept(missile.getLocation(), missile.getVelocity().length(), target.getLocation(), target.getVelocity());
            if (guidedTarget == null) {
                Vector2f projection = new Vector2f(target.getVelocity());
                float scalar = distance / (missile.getVelocity().length() + 1f);
                projection.scale(scalar);
                guidedTarget = Vector2f.add(target.getLocation(), projection, null);
            }
            Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
            guidedTarget.scale(guidance);
            Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

            float weaveSineA = WEAVE_SINE_A_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_A_PERIOD) + weaveSineAPhase);
            float weaveSineB = WEAVE_SINE_B_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_B_PERIOD) + weaveSineBPhase);
            float weaveOffset = (weaveSineA + weaveSineB) * Math.min(1f, distance / WEAVE_FALLOFF_DISTANCE);
            if (MathUtils.isWithinRange(missile.getLocation(),target.getLocation(),MIRV_DISTANCE + target.getCollisionRadius() + 200f)) weaveOffset = 0;

            float angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                    MathUtils.clampAngle(VectorUtils.getAngle(missile.getLocation(), guidedTarget) + getInaccuracyAfterECCM() + weaveOffset));
            float absAngularDistance = Math.abs(angularDistance);

            // Apply thrust, but only if engine dead time is over
            missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

            if (absAngularDistance < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
                missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
            }

            //Global.getCombatEngine().addFloatingText(missile.getLocation(), Math.round(distance) + "", 60, Color.WHITE, null, 0.25f, 0.25f);

            if ((absAngularDistance > 35 && MathUtils.isWithinRange(missile.getLocation(),target.getLocation(),MIRV_DISTANCE + target.getCollisionRadius() + 400f)) ||
                    absAngularDistance > 90){
                stopEngineToTurn = true;
            }
            if ((absAngularDistance <= 10 && MathUtils.isWithinRange(missile.getLocation(),target.getLocation(),MIRV_DISTANCE + target.getCollisionRadius() + 400f)) ||
                    absAngularDistance <= 25){
                stopEngineToTurn = false;
            }

            if (readyToFly && !stopEngineToTurn) {
                //Global.getCombatEngine().addHitParticle(missile.getLocation(),missile.getVelocity(),30,1,0,amount * 3,Color.WHITE);
                missile.giveCommand(ShipCommand.ACCELERATE);
            }

            if ((MathUtils.isWithinRange(missile.getLocation(),target.getLocation(),MIRV_DISTANCE + target.getCollisionRadius()) &&
                    isWithinMIRVAngle(missile.getLocation(),target.getLocation(),distance,missile.getFacing(), target.getCollisionRadius())) ||
                    missile.getFlightTime() > missile.getMaxFlightTime() - 0.1f){
                mirvNow = true;
            }
            /*
            if (MathUtils.isWithinRange(target, missile, MIRV_DISTANCE)){
                if (Math.abs(MathUtils.getShortestRotation(VectorUtils.getAngle(missile.getLocation(), target.getLocation()), missile.getFacing())) <= maxMirvAngle){
                    mirvNow = true;
                }
            }

             */
        }


        // Launch submunitions
        if (mirvNow)
        {
            Global.getLogger(bts_empMissile.class).info("added");
            Global.getCombatEngine().addPlugin(new EveryFrameCombatPlugin() {
                final float waveRange = 600f;
                final float waveSpeed = waveRange / 0.5f;

                float currRange = 0;

                final List<CombatEntityAPI> alreadyHit = new ArrayList<>();

                final IntervalUtil timer = new IntervalUtil(0.05f,0.05f);
                final Vector2f point = new Vector2f(missile.getLocation());

                @Override
                public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

                }

                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    if (Global.getCombatEngine().isPaused()) return;
                    currRange +=  waveSpeed * amount;
                    timer.advance(amount);
                    if (timer.intervalElapsed()) {
                        for (CombatEntityAPI enemy : AIUtils.getNearbyEnemies(missile, currRange)) {
                            if (alreadyHit.contains(enemy)) continue;
                            Vector2f arcLoc = Vector2f.add(point, (Vector2f) Vector2f.sub(enemy.getLocation(), point, null).normalise(null).scale(currRange), null);
                            for (int i = 0; i < 5; i++) {
                            EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArc(missile.getSource(),
                                    arcLoc,
                                    null,
                                    enemy,
                                    DamageType.ENERGY,
                                    100,
                                    500,
                                    999999,
                                    null,
                                    10,
                                    new Color(0, 202, 238,255),
                                    new Color(35, 96, 204,255));
                                arc.setSingleFlickerMode();
                            }
                            alreadyHit.add(enemy);
                        }
                        int numArcs = Math.round(currRange / 10);
                        if (numArcs >= 10) {
                            float anglePerStep = 360f / numArcs;
                            for (int i = 0; i < numArcs; i++) {
                                if (Math.random() <= 0.5f) continue;
                                Vector2f locStart = Vector2f.add(Vector2f.add(point, (Vector2f) Misc.getUnitVectorAtDegreeAngle(anglePerStep * (i + MathUtils.getRandomNumberInRange(-0.2f,0.2f))).scale(currRange), null), new Vector2f(MathUtils.getRandomNumberInRange(-25f,25f),MathUtils.getRandomNumberInRange(-25f,25)),null);
                                Vector2f locEnd = Vector2f.add(Vector2f.add(point, (Vector2f) Misc.getUnitVectorAtDegreeAngle(anglePerStep * (i + 1.5f + MathUtils.getRandomNumberInRange(-0.2f,0.2f))).scale(currRange), null), new Vector2f(MathUtils.getRandomNumberInRange(-25f,25f),MathUtils.getRandomNumberInRange(-25f,25)),null);
                                EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArcVisual(locStart,
                                        null,
                                        locEnd,
                                        null,
                                        10,
                                        new Color(0, 202, 238, 255),
                                        new Color(35, 96, 204, 255));
                                arc.setSingleFlickerMode();
                            }

                        }
                    }
                    if (currRange >= waveRange) Global.getCombatEngine().removePlugin(this);
                }

                @Override
                public void renderInWorldCoords(ViewportAPI viewport) {

                }

                @Override
                public void renderInUICoords(ViewportAPI viewport) {

                }

                @Override
                public void init(CombatEngineAPI engine) {

                }
            });

            Global.getCombatEngine().removeEntity(missile);
        }
    }

    protected boolean acquireTarget() {
        // If our current target is totally invalid, look for a new one
        if (!isTargetValid(target)) {
            if (target instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) target;
                if (ship.isPhased() && ship.isAlive()) {
                    // We were locked onto a ship that has now phased, do not attempt to acquire a new target
                    return true;
                }
            }
            // Look for a target that is not a drone or fighter, if available
            setTarget(findBestTarget(false));
            // No such target, look again except this time we allow drones and fighters
            if (target == null) {
                setTarget(findBestTarget(true));
            }
            return target != null;
        }
        // If our target is valid but a drone or fighter, see if there's a bigger ship we can aim for instead
        else {
            if (isDroneOrFighter(target)) {
                if (target instanceof ShipAPI) {
                    ShipAPI ship = (ShipAPI) target;
                    if (ship.isPhased() && ship.isAlive()) {
                        // We were locked onto a ship that has now phased, do not attempt to acquire a new target
                        return false;
                    }
                }
                CombatEntityAPI newTarget = findBestTarget();
                if (newTarget != null) {
                    target = newTarget;
                }
            }
        }
        return true;
    }

    protected ShipAPI findBestTarget() {
        return findBestTarget(false);
    }

    /**
     * This is some bullshit weighted random picker that favors larger ships
     *
     * @param allowDroneOrFighter True if looking for an alternate target
     *                            (normally it refuses to target fighters or drones)
     * @return
     */
    protected ShipAPI findBestTarget(boolean allowDroneOrFighter) {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod;
            // This is a valid target if:
            //   It is NOT a (drone or fighter), OR we're in alternate mode
            //   It passes the valid target check
            boolean valid = allowDroneOrFighter || !isDroneOrFighter(target);
            valid = valid && isTargetValid(tmp);
            if (!valid) {
                continue;
            } else {
                switch (tmp.getHullSize()) {
                    default:
                    case FIGHTER:
                        mod = 1f;
                        break;
                    case FRIGATE:
                        mod = 10f;
                        break;
                    case DESTROYER:
                        mod = 50f;
                        break;
                    case CRUISER:
                        mod = 100f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 125f;
                        break;
                }
            }
            weight = (4000f / Math.max(MathUtils.getDistance(tmp, missile.getLocation()), 750f)) * mod;
            if (weight > bestWeight) {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    protected boolean isDroneOrFighter(CombatEntityAPI target) {
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            return ship.isFighter() || ship.isDrone();
        }
        return false;
    }

    private static final float RETARGET_TIME = 0f;

    private static Vector2f quad(float a, float b, float c)
    {
        Vector2f solution = null;
        if (Float.compare(Math.abs(a), 0) == 0)
        {
            if (Float.compare(Math.abs(b), 0) == 0)
            {
                solution = (Float.compare(Math.abs(c), 0) == 0) ? new Vector2f(0, 0) : null;
            }
            else
            {
                solution = new Vector2f(-c / b, -c / b);
            }
        }
        else
        {
            float d = b * b - 4 * a * c;
            if (d >= 0)
            {
                d = (float) Math.sqrt(d);
                float e = 2 * a;
                solution = new Vector2f((-b - d) / e, (-b + d) / e);
            }
        }
        return solution;
    }

    static List<ShipAPI> getSortedDirectTargets(ShipAPI launchingShip)
    {
        List<ShipAPI> directTargets = CombatUtils.getShipsWithinRange(launchingShip.getMouseTarget(), 300f);
        if (!directTargets.isEmpty())
        {
            Collections.sort(directTargets, new CollectionUtils.SortEntitiesByDistance(launchingShip.getMouseTarget()));
        }
        return directTargets;
    }

    static Vector2f intercept(Vector2f point, float speed, Vector2f target, Vector2f targetVel)
    {
        final Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        final float a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - speed * speed;
        final float b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        final float c = difference.x * difference.x + difference.y * difference.y;

        final Vector2f solutionSet = quad(a, b, c);

        Vector2f intercept = null;
        if (solutionSet != null)
        {
            float bestFit = Math.min(solutionSet.x, solutionSet.y);
            if (bestFit < 0)
            {
                bestFit = Math.max(solutionSet.x, solutionSet.y);
            }
            if (bestFit > 0)
            {
                intercept = new Vector2f(target.x + targetVel.x * bestFit, target.y + targetVel.y * bestFit);
            }
        }

        return intercept;
    }

    static Vector2f interceptAdvanced(Vector2f point, float speed, float acceleration, float maxspeed, Vector2f target, Vector2f targetVel)
    {
        Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        float s = speed;
        float a = acceleration / 2f;
        float b = speed;
        float c = difference.length();
        Vector2f solutionSet = quad(a, b, c);
        if (solutionSet != null)
        {
            float t = Math.min(solutionSet.x, solutionSet.y);
            if (t < 0)
            {
                t = Math.max(solutionSet.x, solutionSet.y);
            }
            if (t > 0)
            {
                s = acceleration * t;
                s = s / 2f + speed;
                s = Math.min(s, maxspeed);
            }
        }

        a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - s * s;
        b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        c = difference.x * difference.x + difference.y * difference.y;

        solutionSet = quad(a, b, c);

        Vector2f intercept = null;
        if (solutionSet != null)
        {
            float bestFit = Math.min(solutionSet.x, solutionSet.y);
            if (bestFit < 0)
            {
                bestFit = Math.max(solutionSet.x, solutionSet.y);
            }
            if (bestFit > 0)
            {
                intercept = new Vector2f(target.x + targetVel.x * bestFit, target.y + targetVel.y * bestFit);
            }
        }

        return intercept;
    }
    protected ShipAPI launchingShip;
    protected MissileAPI missile;
    protected float retargetTimer = RETARGET_TIME;
    protected CombatEntityAPI target;

    @Override
    public CombatEntityAPI getTarget()
    {
        return target;
    }

    @Override
    public final void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }

    private void defaultInitialTargetingBehavior(ShipAPI launchingShip)
    {
        assignMissileToShipTarget(launchingShip);

        if (target == null)
        {
            setTarget(getMouseTarget(launchingShip));
        }

        if (target == null)
        {
            setTarget(findBestTarget());
        }
    }

    protected void assignMissileToShipTarget(ShipAPI launchingShip)
    {
        if (isTargetValid(launchingShip.getShipTarget()))
        {
            setTarget(launchingShip.getShipTarget());
        }
    }

    protected CombatEntityAPI getMouseTarget(ShipAPI launchingShip)
    {
        for (ShipAPI tmp : getSortedDirectTargets(launchingShip)) {
            if (isTargetValid(tmp)) {
                return tmp;
            }
        }
        return null;
    }

    protected float getRange()
    {
        float maxTime = missile.getMaxFlightTime();
        float speed = missile.getMaxSpeed();
        return speed * maxTime;
    }

    protected float getRemainingRange()
    {
        float time = missile.getMaxFlightTime() - missile.getFlightTime();
        float speed = missile.getMaxSpeed();
        return speed * time;
    }

    protected boolean isTargetValid(CombatEntityAPI target)
    {
        if (target == null || (missile.getOwner() == target.getOwner()) || !Global.getCombatEngine().isEntityInPlay(target))
        {
            return false;
        }

        if (target instanceof ShipAPI)
        {
            ShipAPI ship = (ShipAPI) target;
            if (ship.isPhased() || !ship.isAlive())
            {
                return false;
            }
        }
        else if (target.getCollisionClass() == CollisionClass.NONE)
        {
            return false;
        }

        return true;
    }
}
