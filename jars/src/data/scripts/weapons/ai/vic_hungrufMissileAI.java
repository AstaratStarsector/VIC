package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import data.scripts.weapons.EveryFrameEffects.vic_hungrufSubEffect;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class vic_hungrufMissileAI extends VIC_BaseMissile {

    final float ENGINE_DEAD_TIME_MAX = 0.75f; // Max time until engine burn starts
    final float ENGINE_DEAD_TIME_MIN = 0.25f; // Min time until engine burn starts
    final float LEAD_GUIDANCE_FACTOR = 0.6f;
    final float LEAD_GUIDANCE_FACTOR_FROM_ECCM = 0.4f;
    final float FIRE_INACCURACY = 0f; // Set-once for entire shot lifetime leading offset
    final float AIM_THRESHOLD = 0.5f; // Multiplied by collision radius, how much it can be off by when deciding to MIRV
    final float maxMirvAngle = 5;
    final float MIRV_DISTANCE = 750f;
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

    public vic_hungrufMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);

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
            Vector2f submunitionVelocityMod = new Vector2f(0, MathUtils.getRandomNumberInRange(
                    SUBMUNITION_VELOCITY_MOD_MAX, SUBMUNITION_VELOCITY_MOD_MIN));

            float initialOffset = -(NUMBER_SUBMUNITIONS - 1) / 2f * SUBMUNITION_RELATIVE_OFFSET;
            DamagingProjectileAPI submunition = null;
            for (int i = 0; i < NUMBER_SUBMUNITIONS; i++)
            {
                float angle = missile.getFacing() + initialOffset + i * SUBMUNITION_RELATIVE_OFFSET
                        + MathUtils.getRandomNumberInRange(-SUBMUNITION_INACCURACY, SUBMUNITION_INACCURACY);
                if (angle < 0f)
                {
                    angle += 360f;
                }
                else if (angle >= 360f)
                {
                    angle -= 360f;
                }

                Vector2f vel = STAGE_ONE_TRANSFER_MOMENTUM ? missile.getVelocity() : ZERO;
                Vector2f boost = VectorUtils.rotate(submunitionVelocityMod, missile.getFacing());
                vel.translate(boost.x, boost.y);
                submunition = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(launchingShip,
                        missile.getWeapon(), STAGE_TWO_WEAPON_ID,
                        missile.getLocation(), angle, null);
                submunition.setFromMissile(true);
                float andVel = MathUtils.getRandomNumberInRange(200,300);// * MathUtils.getRandomNumberInRange(-1,1);
                submunition.setAngularVelocity(300);
                //submunition.getVelocity().set(submunition.getVelocity().x * 0.75f + missile.getVelocity().x * 0.25f, submunition.getVelocity().y * 0.75f + missile.getVelocity().y * 0.25f);
                //Global.getCombatEngine().addPlugin(new vic_hungrufSubEffect((MissileAPI) submunition));
                DamagingProjectileAPI submunitionDebris = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(launchingShip,
                        missile.getWeapon(), "vic_hungruf_sub_engine",
                        missile.getLocation(), angle, null);
                submunitionDebris.setAngularVelocity(125);
                submunitionDebris.setOwner(100);
                //submunitionDebris.getVelocity().set(missile.getVelocity().x - submunition.getVelocity().x * 0.5f, missile.getVelocity().y - submunition.getVelocity().y * 0.5f);
            }

            // Only used for missile submunitions, which this is not

            // Transfer any damage the missile has incurred if so desired
            if (STAGE_ONE_TRANSFER_DAMAGE)
            {
                float damageToDeal = missile.getMaxHitpoints() - missile.getHitpoints();
                if (damageToDeal > 0f)
                {
                    Global.getCombatEngine().applyDamage(submunition, missile.getLocation(), damageToDeal,
                            DamageType.FRAGMENTATION, 0f, true, false, missile.getSource());
                }
            }

            Global.getSoundPlayer().playSound(STAGE_TWO_SOUND_ID, 1f, 1f, missile.getLocation(), missile.getVelocity());

            // GFX on the spot of the switcheroo if desired
            // Remove old missile
            if (STAGE_ONE_EXPLODE)
            {
                Global.getCombatEngine().addSmokeParticle(missile.getLocation(), missile.getVelocity(), 60f, 0.75f, 0.75f, SMOKE_COLOR);
                Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 100f,
                        DamageType.FRAGMENTATION, 0f, false, false, missile);
            }

            else if (STAGE_ONE_FLARE)
            {
                Vector2f offset = new Vector2f(FLARE_OFFSET, 0f);
                VectorUtils.rotate(offset, missile.getFacing(), offset);
                Vector2f.add(offset, missile.getLocation(), offset);
                Global.getCombatEngine().addHitParticle(offset, missile.getVelocity(), 100f, 0.5f, 0.25f, FLARE_COLOR);
                Global.getCombatEngine().removeEntity(missile);
            }
            else
            {
                Global.getCombatEngine().removeEntity(missile);
            }
        }
    }

    @Override
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

    @Override
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
}
