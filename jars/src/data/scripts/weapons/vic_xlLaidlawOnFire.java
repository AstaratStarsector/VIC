// By Nicke535
// Spawns particles from a weapon in different firing states, as determined by the user.
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.vic_weaponDamageListener;
import org.magiclib.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.util.Misc.ZERO;
import static data.scripts.plugins.vic_combatPlugin.AddXLLaidlawProj;

public class vic_xlLaidlawOnFire implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {


    boolean weaponChargedownBlowback = false;
    boolean emitTrailBlowback = false;

    float
            durationBlowback = 6f,
            timeBlowback = 0f;

    private final IntervalUtil trailTracker = new IntervalUtil(0.05f, 0.05f);


    final float MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE = 15f;
    final float MUZZLE_OFFSET_TURRET_SHOCKWAVE = 15f;
    final float MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE_SPRITE1 = 80f;
    final float MUZZLE_OFFSET_TURRET_SHOCKWAVE_SPRITE1 = 80f;
    final float MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE_SPRITE2 = 100f;
    final float MUZZLE_OFFSET_TURRET_SHOCKWAVE_SPRITE2 = 100f;
    final float MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE_SPRITE3 = 120f;
    final float MUZZLE_OFFSET_TURRET_SHOCKWAVE_SPRITE3 = 120f;

    final float MUZZLE_OFFSET_TURRET_TRAIL = -37f;
    final float MUZZLE_OFFSET_HARDPOINT_TRAIL = -37f;
    final float MUZZLE_OFFSET_TURRET_TRAIL2 = -25f;
    final float MUZZLE_OFFSET_HARDPOINT_TRAIL2 = -25f;

    private boolean light = false;

    /*
        HOW TO USE:
        USED_IDS specifies which IDs to use for the rest of the script; any ID is valid EXCEPT the unique ID "default". Each ID should only be used once on the same weapon
        The script will spawn one particle "system" for each ID in this list, with the specific attributes of that ID.

        All the different Maps<> specify the attributes of each of the particle "systems"; they MUST have something defined as "default", and can have specific fields for specific IDs
        in the USED_IDS list; any field not filled in for a specific ID will revert to "default" instead.
    */
    private static final List<String> USED_IDS = new ArrayList<>();

    static {
        USED_IDS.add("MUZZLE_PARTICLE_BURST");
        USED_IDS.add("ELECTROPARTICLES_BURST_LEFT");
        USED_IDS.add("ELECTROPARTICLES_BURST_RIGHT");
        USED_IDS.add("SMOKE_BURST_LEFT");
        USED_IDS.add("SMOKE_BURST_RIGHT");
        USED_IDS.add("BLOWBACK_LEFT_UPPER");
        USED_IDS.add("BLOWBACK_LEFT_LOWER");
        USED_IDS.add("BLOWBACK_RIGHT_UPPER");
        USED_IDS.add("BLOWBACK_RIGHT_LOWER");

    }

    //The amount of particles spawned immediately when the weapon reaches full charge level
    //  -For projectile weapons, this is when the projectile is actually fired
    //  -For beam weapons, this is when the beam has reached maximum brightness
    private static final Map<String, Integer> ON_SHOT_PARTICLE_COUNT = new HashMap<>();

    static {
        ON_SHOT_PARTICLE_COUNT.put("default", 0);
        ON_SHOT_PARTICLE_COUNT.put("MUZZLE_PARTICLE_BURST", 160);
        ON_SHOT_PARTICLE_COUNT.put("ELECTROPARTICLES_BURST_LEFT", 25);
        ON_SHOT_PARTICLE_COUNT.put("ELECTROPARTICLES_BURST_RIGHT", 25);
        ON_SHOT_PARTICLE_COUNT.put("SMOKE_BURST_LEFT", 7);
        ON_SHOT_PARTICLE_COUNT.put("SMOKE_BURST_RIGHT", 7);
        ON_SHOT_PARTICLE_COUNT.put("BLOWBACK_LEFT_UPPER", 15);
        ON_SHOT_PARTICLE_COUNT.put("BLOWBACK_LEFT_LOWER", 15);
        ON_SHOT_PARTICLE_COUNT.put("BLOWBACK_RIGHT_UPPER", 15);
        ON_SHOT_PARTICLE_COUNT.put("BLOWBACK_RIGHT_LOWER", 15);


    }

    //How many particles are spawned each second the weapon is firing, on average
    private static final Map<String, Float> PARTICLES_PER_SECOND = new HashMap<>();

    static {
        PARTICLES_PER_SECOND.put("default", 0f);
        PARTICLES_PER_SECOND.put("BLOWBACK_LEFT_UPPER", 35f);
        PARTICLES_PER_SECOND.put("BLOWBACK_LEFT_LOWER", 25f);
        PARTICLES_PER_SECOND.put("BLOWBACK_RIGHT_UPPER", 35f);
        PARTICLES_PER_SECOND.put("BLOWBACK_RIGHT_LOWER", 25f);
    }

    //Does the PARTICLES_PER_SECOND field get multiplied by the weapon's current chargeLevel?
    private static final Map<String, Boolean> AFFECTED_BY_CHARGELEVEL = new HashMap<>();

    static {
        AFFECTED_BY_CHARGELEVEL.put("default", false);
        AFFECTED_BY_CHARGELEVEL.put("BLOWBACK_LEFT_UPPER", true);
        AFFECTED_BY_CHARGELEVEL.put("BLOWBACK_LEFT_LOWER", true);
        AFFECTED_BY_CHARGELEVEL.put("BLOWBACK_RIGHT_UPPER", true);
        AFFECTED_BY_CHARGELEVEL.put("BLOWBACK_RIGHT_LOWER", true);

    }

    //When are the particles spawned (only used for PARTICLES_PER_SECOND)? Valid values are "CHARGEUP", "FIRING", "CHARGEDOWN", "READY" (not on cooldown or firing) and "COOLDOWN".
    //  Multiple of these values can be combined via "-" inbetween; "CHARGEUP-CHARGEDOWN" is for example valid
    private static final Map<String, String> PARTICLE_SPAWN_MOMENT = new HashMap<>();

    static {
        PARTICLE_SPAWN_MOMENT.put("default", "FIRING");
        PARTICLE_SPAWN_MOMENT.put("BLOWBACK_LEFT_UPPER", "CHARGEDOWN");
        PARTICLE_SPAWN_MOMENT.put("BLOWBACK_LEFT_LOWER", "CHARGEDOWN");
        PARTICLE_SPAWN_MOMENT.put("BLOWBACK_RIGHT_UPPER", "CHARGEDOWN");
        PARTICLE_SPAWN_MOMENT.put("BLOWBACK_RIGHT_LOWER", "CHARGEDOWN");
    }

    //If this is set to true, the particles spawn with regard to *barrel*, not *center*. Only works for ALTERNATING barrel types on weapons: for LINKED barrels you
    //  should instead set up their coordinates manually with PARTICLE_SPAWN_POINT_TURRET and PARTICLE_SPAWN_POINT_HARDPOINT
    private static final Map<String, Boolean> SPAWN_POINT_ANCHOR_ALTERNATION = new HashMap<>();

    static {
        SPAWN_POINT_ANCHOR_ALTERNATION.put("default", false);
    }

    //The position the particles are spawned (or at least where their arc originates when using offsets) compared to their weapon's center [or shot offset, see
    //SPAWN_POINT_ANCHOR_ALTERNATION above], if the weapon is a turret (or HIDDEN)
    private static final Map<String, Vector2f> PARTICLE_SPAWN_POINT_TURRET = new HashMap<>();

    static {
        PARTICLE_SPAWN_POINT_TURRET.put("default", new Vector2f(0f, 0f));
        PARTICLE_SPAWN_POINT_TURRET.put("ELECTROPARTICLES_BURST_LEFT", new Vector2f(10f, 40f));
        PARTICLE_SPAWN_POINT_TURRET.put("ELECTROPARTICLES_BURST_RIGHT", new Vector2f(-10f, 40f));
        PARTICLE_SPAWN_POINT_TURRET.put("SMOKE_BURST_LEFT", new Vector2f(10f, 40f));
        PARTICLE_SPAWN_POINT_TURRET.put("SMOKE_BURST_RIGHT", new Vector2f(-10f, 40f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_LEFT_UPPER", new Vector2f(27f, -32f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_LEFT_LOWER", new Vector2f(22f, -52f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_RIGHT_UPPER", new Vector2f(-27f, -32f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_RIGHT_LOWER", new Vector2f(-22f, -52f));

    }

    //The position the particles are spawned (or at least where their arc originates when using offsets) compared to their weapon's center [or shot offset, see
    //SPAWN_POINT_ANCHOR_ALTERNATION above], if the weapon is a hardpoint
    private static final Map<String, Vector2f> PARTICLE_SPAWN_POINT_HARDPOINT = new HashMap<>();

    static {
        PARTICLE_SPAWN_POINT_HARDPOINT.put("default", new Vector2f(0f, 0f));
    }

    //Which kind of particle is spawned (valid values are "SMOOTH", "BRIGHT" , "SMOKE", "NEBULA", "NEBULA_SMOKE", "NEBULA_SWIRLY")
    private static final Map<String, String> PARTICLE_TYPE = new HashMap<>();

    static {
        PARTICLE_TYPE.put("default", "SMOOTH");
        PARTICLE_TYPE.put("ELECTROPARTICLES_BURST_LEFT", "BRIGHT");
        PARTICLE_TYPE.put("ELECTROPARTICLES_BURST_RIGHT", "BRIGHT");
        PARTICLE_TYPE.put("SMOKE_BURST_LEFT", "NEBULA");
        PARTICLE_TYPE.put("SMOKE_BURST_RIGHT", "NEBULA");
        PARTICLE_TYPE.put("BLOWBACK_LEFT_UPPER", "NEBULA");
        PARTICLE_TYPE.put("BLOWBACK_LEFT_LOWER", "NEBULA");
        PARTICLE_TYPE.put("BLOWBACK_RIGHT_UPPER", "NEBULA");
        PARTICLE_TYPE.put("BLOWBACK_RIGHT_LOWER", "NEBULA");
    }

    //What color does the particles have?
    private static final Map<String, Color> PARTICLE_COLOR = new HashMap<>();

    static {
        PARTICLE_COLOR.put("default", new Color(155, 255, 255, 165));
        PARTICLE_COLOR.put("MUZZLE_PARTICLE_BURST", new Color(155, 255, 255, 165));
        PARTICLE_COLOR.put("ELECTROPARTICLES_BURST_LEFT", new Color(120, 120, 255, 255));
        PARTICLE_COLOR.put("ELECTROPARTICLES_BURST_RIGHT", new Color(120, 120, 255, 255));
        PARTICLE_COLOR.put("SMOKE_BURST_LEFT", new Color(200, 200, 255, 155));
        PARTICLE_COLOR.put("SMOKE_BURST_RIGHT", new Color(200, 200, 255, 155));
        PARTICLE_COLOR.put("BLOWBACK_LEFT_UPPER", new Color(155, 255, 255, 200));
        PARTICLE_COLOR.put("BLOWBACK_LEFT_LOWER", new Color(155, 255, 255, 200));
        PARTICLE_COLOR.put("BLOWBACK_RIGHT_UPPER", new Color(155, 255, 255, 200));
        PARTICLE_COLOR.put("BLOWBACK_RIGHT_LOWER", new Color(155, 255, 255, 200));
    }

    //What's the smallest size the particles can have?
    private static final Map<String, Float> PARTICLE_SIZE_MIN = new HashMap<>();

    static {
        PARTICLE_SIZE_MIN.put("default", 5f);
        PARTICLE_SIZE_MIN.put("MUZZLE_PARTICLE_BURST", 20f);
        PARTICLE_SIZE_MIN.put("ELECTROPARTICLES_BURST_LEFT", 6f);
        PARTICLE_SIZE_MIN.put("ELECTROPARTICLES_BURST_RIGHT", 6f);
        PARTICLE_SIZE_MIN.put("SMOKE_BURST_LEFT", 10f);
        PARTICLE_SIZE_MIN.put("SMOKE_BURST_RIGHT", 10f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_LEFT_UPPER", 15f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_LEFT_LOWER", 10f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_RIGHT_UPPER", 15f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_RIGHT_LOWER", 10f);
    }

    //What's the largest size the particles can have?
    private static final Map<String, Float> PARTICLE_SIZE_MAX = new HashMap<>();

    static {
        PARTICLE_SIZE_MAX.put("default", 10f);
        PARTICLE_SIZE_MAX.put("MUZZLE_PARTICLE_BURST", 50f);
        PARTICLE_SIZE_MAX.put("ELECTROPARTICLES_BURST_LEFT", 12f);
        PARTICLE_SIZE_MAX.put("ELECTROPARTICLES_BURST_RIGHT", 12f);
        PARTICLE_SIZE_MAX.put("SMOKE_BURST_LEFT", 15f);
        PARTICLE_SIZE_MAX.put("SMOKE_BURST_RIGHT", 15f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_LEFT_UPPER", 25f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_LEFT_LOWER", 15f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_RIGHT_UPPER", 25f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_RIGHT_LOWER", 15f);
    }

    //What's the lowest velocity a particle can spawn with (can be negative)?
    private static final Map<String, Float> PARTICLE_VELOCITY_MIN = new HashMap<>();

    static {
        PARTICLE_VELOCITY_MIN.put("default", 2f);
        PARTICLE_VELOCITY_MIN.put("MUZZLE_PARTICLE_BURST", 100f);
        PARTICLE_VELOCITY_MIN.put("ELECTROPARTICLES_BURST_LEFT", 50f);
        PARTICLE_VELOCITY_MIN.put("ELECTROPARTICLES_BURST_RIGHT", 50f);
        PARTICLE_VELOCITY_MIN.put("SMOKE_BURST_LEFT", 10f);
        PARTICLE_VELOCITY_MIN.put("SMOKE_BURST_RIGHT", 10f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_LEFT_UPPER", 80f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_LEFT_LOWER", 50f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_RIGHT_UPPER", 80f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_RIGHT_LOWER", 50f);
    }

    //What's the highest velocity a particle can spawn with (can be negative)?
    private static final Map<String, Float> PARTICLE_VELOCITY_MAX = new HashMap<>();

    static {
        PARTICLE_VELOCITY_MAX.put("default", 20f);
        PARTICLE_VELOCITY_MAX.put("MUZZLE_PARTICLE_BURST", 750f);
        PARTICLE_VELOCITY_MAX.put("SMOKE_BURST_LEFT", 30f);
        PARTICLE_VELOCITY_MAX.put("SMOKE_BURST_RIGHT", 30f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_LEFT_UPPER", 120f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_LEFT_LOWER", 75f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_RIGHT_UPPER", 120f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_RIGHT_LOWER", 75f);
    }

    //The shortest duration a particle will last before completely fading away
    private static final Map<String, Float> PARTICLE_DURATION_MIN = new HashMap<>();

    static {
        PARTICLE_DURATION_MIN.put("default", 0.75f);
        PARTICLE_DURATION_MIN.put("MUZZLE_PARTICLE_BURST", 0.37f);
        PARTICLE_DURATION_MIN.put("ELECTROPARTICLES_BURST_LEFT", 0.15f);
        PARTICLE_DURATION_MIN.put("ELECTROPARTICLES_BURST_RIGHT", 0.15f);
        PARTICLE_DURATION_MIN.put("SMOKE_BURST_LEFT", 0.75f);
        PARTICLE_DURATION_MIN.put("SMOKE_BURST_RIGHT", 0.75f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_LEFT_UPPER", 0.15f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_LEFT_LOWER", 0.15f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_RIGHT_UPPER", 0.15f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_RIGHT_LOWER", 0.15f);

    }

    //The longest duration a particle will last before completely fading away
    private static final Map<String, Float> PARTICLE_DURATION_MAX = new HashMap<>();

    static {
        PARTICLE_DURATION_MAX.put("default", 1.5f);
        PARTICLE_DURATION_MAX.put("MUZZLE_PARTICLE_BURST", 0.75f);
        PARTICLE_DURATION_MAX.put("ELECTROPARTICLES_BURST_LEFT", 0.5f);
        PARTICLE_DURATION_MAX.put("ELECTROPARTICLES_BURST_RIGHT", 0.5f);
        PARTICLE_DURATION_MAX.put("SMOKE_BURST_LEFT", 1.5f);
        PARTICLE_DURATION_MAX.put("SMOKE_BURST_RIGHT", 1.5f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_LEFT_UPPER", 0.5f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_LEFT_LOWER", 0.5f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_RIGHT_UPPER", 0.5f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_RIGHT_LOWER", 0.5f);
    }

    //The shortest along their velocity vector any individual particle is allowed to spawn (can be negative to spawn behind their origin point)
    private static final Map<String, Float> PARTICLE_OFFSET_MIN = new HashMap<>();

    static {
        PARTICLE_OFFSET_MIN.put("default", 0f);
        PARTICLE_OFFSET_MIN.put("MUZZLE_PARTICLE_BURST", 5f);
        PARTICLE_OFFSET_MIN.put("ELECTROPARTICLES_BURST_LEFT", -15f);
        PARTICLE_OFFSET_MIN.put("ELECTROPARTICLES_BURST_RIGHT", -15f);
        PARTICLE_OFFSET_MIN.put("SMOKE_BURST_LEFT", -10f);
        PARTICLE_OFFSET_MIN.put("SMOKE_BURST_RIGHT", -10f);
    }

    //The furthest along their velocity vector any individual particle is allowed to spawn (can be negative to spawn behind their origin point)
    private static final Map<String, Float> PARTICLE_OFFSET_MAX = new HashMap<>();

    static {
        PARTICLE_OFFSET_MAX.put("default", 10f);
        PARTICLE_OFFSET_MAX.put("ELECTROPARTICLES_BURST_LEFT", 15f);
        PARTICLE_OFFSET_MAX.put("ELECTROPARTICLES_BURST_RIGHT", 15f);
        PARTICLE_OFFSET_MAX.put("SMOKE_BURST_LEFT", 10f);
        PARTICLE_OFFSET_MAX.put("SMOKE_BURST_RIGHT", 10f);
    }

    //The width of the "arc" the particles spawn in; affects both offset and velocity. 360f = full circle, 0f = straight line
    private static final Map<String, Float> PARTICLE_ARC = new HashMap<>();

    static {
        PARTICLE_ARC.put("default", 10f);
        PARTICLE_ARC.put("MUZZLE_PARTICLE_BURST", 8f);
        PARTICLE_ARC.put("ELECTROPARTICLES_BURST_LEFT", 120f);
        PARTICLE_ARC.put("ELECTROPARTICLES_BURST_RIGHT", 120f);
        PARTICLE_ARC.put("SMOKE_BURST_LEFT", 120f);
        PARTICLE_ARC.put("SMOKE_BURST_RIGHT", 120f);
        PARTICLE_ARC.put("BLOWBACK_LEFT_UPPER", 45f);
        PARTICLE_ARC.put("BLOWBACK_LEFT_LOWER", 30f);
        PARTICLE_ARC.put("BLOWBACK_RIGHT_UPPER", 45f);
        PARTICLE_ARC.put("BLOWBACK_RIGHT_LOWER", 30f);


    }

    //The offset of the "arc" the particles spawn in, compared to the weapon's forward facing.
    //  For example: 90f = the center of the arc is 90 degrees clockwise around the weapon, 0f = the same arc center as the weapon's facing.
    private static final Map<String, Float> PARTICLE_ARC_FACING = new HashMap<>();

    static {
        PARTICLE_ARC_FACING.put("default", 80f);
        PARTICLE_ARC_FACING.put("MUZZLE_PARTICLE_BURST", 0f);
        PARTICLE_ARC_FACING.put("ELECTROPARTICLES_BURST_LEFT", 90f);
        PARTICLE_ARC_FACING.put("ELECTROPARTICLES_BURST_RIGHT", -90f);
        PARTICLE_ARC_FACING.put("SMOKE_BURST_LEFT", 90f);
        PARTICLE_ARC_FACING.put("SMOKE_BURST_RIGHT", -90f);
        PARTICLE_ARC_FACING.put("BLOWBACK_LEFT_UPPER", 135f);
        PARTICLE_ARC_FACING.put("BLOWBACK_LEFT_LOWER", 135f);
        PARTICLE_ARC_FACING.put("BLOWBACK_RIGHT_UPPER", -135f);
        PARTICLE_ARC_FACING.put("BLOWBACK_RIGHT_LOWER", -135f);
    }

    //How far away from the screen's edge the particles are allowed to spawn. Lower values mean better performance, but
    //too low values will cause pop-in of particles. Generally, the longer the particle's lifetime, the higher this
    //value should be
    private static final Map<String, Float> PARTICLE_SCREENSPACE_CULL_DISTANCE = new HashMap<>();

    static {
        PARTICLE_SCREENSPACE_CULL_DISTANCE.put("default", 600f);
    }


    //-----------------------------------------------------------You don't need to touch stuff beyond this point!------------------------------------------------------------


    //These ones are used in-script, so don't touch them!
    private boolean hasFiredThisCharge = false;
    private boolean hasFiredThisChargeBlowback = false;
    private int currentBarrel = 0;
    private boolean shouldOffsetBarrelExtra = false;

    float animStartTime = 0f,
            animEndTime = 0f;
    float totalFrames = 0,
            totalFireTime = 0,
            startPercent = 0,
            endPercent = 0;

    Float trailID = null;
    Float trailID2 = null;
    Float trailID3 = null;
    Float trailID4 = null;

    //Instantiator

    //
    boolean doOnce = true;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {


        //Don't run while paused, or without a weapon
        if (weapon == null || amount <= 0f) {
            return;
        }

        trailTracker.advance(amount);

        if (doOnce) {
            if (!weapon.getShip().hasListenerOfClass(vic_weaponDamageListener.class)) {
                weapon.getShip().addListener(new vic_weaponDamageListener());
            }
            totalFireTime = ((ProjectileWeaponSpecAPI) weapon.getSpec()).getRefireDelay();
            totalFrames = weapon.getAnimation().getNumFrames();
            animEndTime = totalFrames / weapon.getAnimation().getFrameRate() + animStartTime;
            weapon.getAnimation().setFrameRate(0);
            startPercent = animStartTime / totalFireTime;
            endPercent = animEndTime / totalFireTime;
            doOnce = false;
        }

        //Saves handy variables used later
        boolean fired = weapon.getCooldownRemaining() > 0;
        float chargeLevel = weapon.getChargeLevel();

        String sequenceState = "READY";
        if (chargeLevel > 0 && (!weapon.isBeam() || weapon.isFiring())) {
            if (chargeLevel >= 1f) {
                sequenceState = "FIRING";
            } else if (!hasFiredThisCharge) {
                sequenceState = "CHARGEUP";
            } else {
                sequenceState = "CHARGEDOWN";
            }
        } else if (weapon.getCooldownRemaining() > 0) {
            sequenceState = "COOLDOWN";
        }

        float shotFaction = 0.0f;
        if (!fired) {
            shotFaction = ((((ProjectileWeaponSpecAPI) weapon.getSpec()).getChargeTime() * chargeLevel) / totalFireTime);
        } else {
            shotFaction = ((((ProjectileWeaponSpecAPI) weapon.getSpec()).getChargeTime() + ((totalFireTime - ((ProjectileWeaponSpecAPI) weapon.getSpec()).getChargeTime()) * (1 - chargeLevel))) / totalFireTime);
        }
        float animFraction = (shotFaction - startPercent) / endPercent;
        int frame = (int) MathUtils.clamp((animFraction * totalFrames), 0, totalFrames - 1);
        Global.getLogger(vic_xlLaidlawOnFire.class).info((frame + 1) + "/" + totalFrames);
        weapon.getAnimation().setFrame(frame);


        //Adjustment for burst beams, since they are a pain
        if (weapon.isBurstBeam() && sequenceState.contains("CHARGEDOWN")) {
            chargeLevel = Math.max(0f, Math.min(Math.abs(weapon.getCooldownRemaining() - weapon.getCooldown()) / weapon.getSpec().getDerivedStats().getBurstFireDuration(), 1f));
        }

        //The sequenceStates "CHARGEDOWN" and "COOLDOWN" counts its barrel as 1 earlier than usual, due to code limitations
        shouldOffsetBarrelExtra = sequenceState.contains("CHARGEDOWN") || sequenceState.contains("COOLDOWN");

        //We go through each of our particle systems and handle their particle spawning
        for (String ID : USED_IDS) {
            //Screenspace check: simplified but should do the trick 99% of the time
            float screenspaceCullingDistance = PARTICLE_SCREENSPACE_CULL_DISTANCE.get("default");
            if (PARTICLE_SCREENSPACE_CULL_DISTANCE.containsKey(ID)) {
                screenspaceCullingDistance = PARTICLE_SCREENSPACE_CULL_DISTANCE.get(ID);
            }
            if (!engine.getViewport().isNearViewport(weapon.getLocation(), screenspaceCullingDistance)) {
                continue;
            }

            //Store all the values used for this check, and use default values if we don't have specific values for our ID specified
            //Note that particle count, specifically, is not declared here and is only used in more local if-cases
            boolean affectedByChargeLevel = AFFECTED_BY_CHARGELEVEL.get("default");
            if (AFFECTED_BY_CHARGELEVEL.containsKey(ID)) {
                affectedByChargeLevel = AFFECTED_BY_CHARGELEVEL.get(ID);
            }

            String particleSpawnMoment = PARTICLE_SPAWN_MOMENT.get("default");
            if (PARTICLE_SPAWN_MOMENT.containsKey(ID)) {
                particleSpawnMoment = PARTICLE_SPAWN_MOMENT.get(ID);
            }

            boolean spawnPointAnchorAlternation = SPAWN_POINT_ANCHOR_ALTERNATION.get("default");
            if (SPAWN_POINT_ANCHOR_ALTERNATION.containsKey(ID)) {
                spawnPointAnchorAlternation = SPAWN_POINT_ANCHOR_ALTERNATION.get(ID);
            }

            //Here, we only store one value, depending on if we're a hardpoint or not
            Vector2f particleSpawnPoint = PARTICLE_SPAWN_POINT_TURRET.get("default");
            if (weapon.getSlot().isHardpoint()) {
                particleSpawnPoint = PARTICLE_SPAWN_POINT_HARDPOINT.get("default");
                if (PARTICLE_SPAWN_POINT_HARDPOINT.containsKey(ID)) {
                    particleSpawnPoint = PARTICLE_SPAWN_POINT_HARDPOINT.get(ID);
                }
            } else {
                if (PARTICLE_SPAWN_POINT_TURRET.containsKey(ID)) {
                    particleSpawnPoint = PARTICLE_SPAWN_POINT_TURRET.get(ID);
                }
            }

            String particleType = PARTICLE_TYPE.get("default");
            if (PARTICLE_TYPE.containsKey(ID)) {
                particleType = PARTICLE_TYPE.get(ID);
            }

            Color particleColor = PARTICLE_COLOR.get("default");
            if (PARTICLE_COLOR.containsKey(ID)) {
                particleColor = PARTICLE_COLOR.get(ID);
            }

            float particleSizeMin = PARTICLE_SIZE_MIN.get("default");
            if (PARTICLE_SIZE_MIN.containsKey(ID)) {
                particleSizeMin = PARTICLE_SIZE_MIN.get(ID);
            }
            float particleSizeMax = PARTICLE_SIZE_MAX.get("default");
            if (PARTICLE_SIZE_MAX.containsKey(ID)) {
                particleSizeMax = PARTICLE_SIZE_MAX.get(ID);
            }

            float particleVelocityMin = PARTICLE_VELOCITY_MIN.get("default");
            if (PARTICLE_VELOCITY_MIN.containsKey(ID)) {
                particleVelocityMin = PARTICLE_VELOCITY_MIN.get(ID);
            }
            float particleVelocityMax = PARTICLE_VELOCITY_MAX.get("default");
            if (PARTICLE_VELOCITY_MAX.containsKey(ID)) {
                particleVelocityMax = PARTICLE_VELOCITY_MAX.get(ID);
            }

            float particleDurationMin = PARTICLE_DURATION_MIN.get("default");
            if (PARTICLE_DURATION_MIN.containsKey(ID)) {
                particleDurationMin = PARTICLE_DURATION_MIN.get(ID);
            }
            float particleDurationMax = PARTICLE_DURATION_MAX.get("default");
            if (PARTICLE_DURATION_MAX.containsKey(ID)) {
                particleDurationMax = PARTICLE_DURATION_MAX.get(ID);
            }

            float particleOffsetMin = PARTICLE_OFFSET_MIN.get("default");
            if (PARTICLE_OFFSET_MIN.containsKey(ID)) {
                particleOffsetMin = PARTICLE_OFFSET_MIN.get(ID);
            }
            float particleOffsetMax = PARTICLE_OFFSET_MAX.get("default");
            if (PARTICLE_OFFSET_MAX.containsKey(ID)) {
                particleOffsetMax = PARTICLE_OFFSET_MAX.get(ID);
            }

            float particleArc = PARTICLE_ARC.get("default");
            if (PARTICLE_ARC.containsKey(ID)) {
                particleArc = PARTICLE_ARC.get(ID);
            }
            float particleArcFacing = PARTICLE_ARC_FACING.get("default");
            if (PARTICLE_ARC_FACING.containsKey(ID)) {
                particleArcFacing = PARTICLE_ARC_FACING.get(ID);
            }
            //---------------------------------------END OF DECLARATIONS-----------------------------------------

            //First, spawn "on full firing" particles, since those ignore sequence state
            if (chargeLevel >= 1f && !hasFiredThisCharge) {
                //Count spawned particles: only trigger if the spawned particles are more than 0
                float particleCount = ON_SHOT_PARTICLE_COUNT.get("default");
                if (ON_SHOT_PARTICLE_COUNT.containsKey(ID)) {
                    particleCount = ON_SHOT_PARTICLE_COUNT.get(ID);
                }

                if (particleCount > 0) {
                    spawnParticles(engine, weapon, particleCount, particleType, spawnPointAnchorAlternation, particleSpawnPoint, particleColor, particleSizeMin, particleSizeMax, particleVelocityMin, particleVelocityMax,
                            particleDurationMin, particleDurationMax, particleOffsetMin, particleOffsetMax, particleArc, particleArcFacing);
                }
            }

            //Then, we check if we should spawn particles over duration; only spawn if our spawn moment is in the declaration
            if (particleSpawnMoment.contains(sequenceState)) {
                //Get how many particles should be spawned this frame
                float particleCount = PARTICLES_PER_SECOND.get("default");
                if (PARTICLES_PER_SECOND.containsKey(ID)) {
                    particleCount = PARTICLES_PER_SECOND.get(ID);
                }
                particleCount *= amount;
                if (affectedByChargeLevel && (sequenceState.contains("CHARGEUP") || sequenceState.contains("CHARGEDOWN"))) {
                    particleCount *= chargeLevel;
                }
                if (affectedByChargeLevel && sequenceState.contains("COOLDOWN")) {
                    particleCount *= (weapon.getCooldownRemaining() / weapon.getCooldown());
                }

                //Then, if the particle count is greater than 0, we actually spawn the particles
                if (particleCount > 0f) {
                    spawnParticles(engine, weapon, particleCount, particleType, spawnPointAnchorAlternation, particleSpawnPoint, particleColor, particleSizeMin, particleSizeMax,
                            particleVelocityMin, particleVelocityMax, particleDurationMin, particleDurationMax, particleOffsetMin, particleOffsetMax,
                            particleArc, particleArcFacing);
                }
            }
        }

        //If this was our "reached full charge" frame, register that
        if (chargeLevel >= 1f && !hasFiredThisCharge) {
            hasFiredThisCharge = true;
        }

        //Increase our current barrel if we have <= 0 chargeLevel OR have ceased firing for now, if we alternate, and have fired at least once since we last increased it
        //Also make sure the barrels "loop around", and reset our hasFired variable
        if (hasFiredThisCharge && (chargeLevel <= 0f || !weapon.isFiring())) {
            hasFiredThisCharge = false;
            currentBarrel++;

            //We can *technically* have different barrel counts for hardpoints, hiddens and turrets, so take that into account
            int barrelCount = weapon.getSpec().getTurretAngleOffsets().size();
            if (weapon.getSlot().isHardpoint()) {
                barrelCount = weapon.getSpec().getHardpointAngleOffsets().size();
            } else if (weapon.getSlot().isHidden()) {
                barrelCount = weapon.getSpec().getHiddenAngleOffsets().size();
            }

            if (currentBarrel >= barrelCount) {
                currentBarrel = 0;
            }
        }


        //chargedown blowback visuals

        if (weapon.getChargeLevel() >= 1f && !hasFiredThisChargeBlowback) {
            hasFiredThisChargeBlowback = true;
        }
        if (hasFiredThisChargeBlowback && (weapon.getChargeLevel() <= 0f || !weapon.isFiring())) {
            hasFiredThisChargeBlowback = false;
        }

        if (weapon.getChargeLevel() > 0f && hasFiredThisChargeBlowback) {
            weaponChargedownBlowback = true;
            hasFiredThisChargeBlowback = false;
        }
        if (weaponChargedownBlowback) {
            emitTrailBlowback = true;
            weaponChargedownBlowback = false;
        }


        if (timeBlowback >= durationBlowback) {
            timeBlowback = 0f;
            emitTrailBlowback = false;
        }

        if (emitTrailBlowback) {
            timeBlowback += amount;

            float trailDirLeft = weapon.getCurrAngle() + 135f;
            float trailDirRight = weapon.getCurrAngle() - 135f;

            float angle = Misc.getAngleInDegrees(new Vector2f(weapon.getShip().getVelocity()));

            Vector2f weaponLocation = weapon.getLocation();
            float weaponFacing = weapon.getCurrAngle();

            Vector2f additionalTrailOffset = VectorUtils.rotate(new Vector2f(15, 0), weaponFacing - 90);
            Vector2f additionalTrailOffset2 = VectorUtils.rotate(new Vector2f(-15, 0), weaponFacing - 90);
            Vector2f additionalTrailOffset3 = VectorUtils.rotate(new Vector2f(15, 0), weaponFacing - 90);
            Vector2f additionalTrailOffset4 = VectorUtils.rotate(new Vector2f(-15, 0), weaponFacing - 90);


            if (trailTracker.intervalElapsed()) {

                if (trailID == null) {
                    trailID = MagicTrailPlugin.getUniqueID();
                }
                if (trailID2 == null) {
                    trailID2 = MagicTrailPlugin.getUniqueID();
                }
                if (trailID3 == null) {
                    trailID3 = MagicTrailPlugin.getUniqueID();
                }
                if (trailID4 == null) {
                    trailID4 = MagicTrailPlugin.getUniqueID();
                }

                Vector2f muzzleLocationTrailBlowbackRight = MathUtils.getPointOnCircumference(Vector2f.add(additionalTrailOffset, weaponLocation, null),
                        weapon.getSlot().isTurret() ? MUZZLE_OFFSET_TURRET_TRAIL : MUZZLE_OFFSET_HARDPOINT_TRAIL, weaponFacing);
                Vector2f muzzleLocationTrailBlowbackLeft = MathUtils.getPointOnCircumference(Vector2f.add(additionalTrailOffset2, weaponLocation, null),
                        weapon.getSlot().isTurret() ? MUZZLE_OFFSET_TURRET_TRAIL : MUZZLE_OFFSET_HARDPOINT_TRAIL, weaponFacing);
                Vector2f muzzleLocationTrailBlowbackRight2 = MathUtils.getPointOnCircumference(Vector2f.add(additionalTrailOffset3, weaponLocation, null),
                        weapon.getSlot().isTurret() ? MUZZLE_OFFSET_TURRET_TRAIL2 : MUZZLE_OFFSET_HARDPOINT_TRAIL2, weaponFacing);
                Vector2f muzzleLocationTrailBlowbackLeft2 = MathUtils.getPointOnCircumference(Vector2f.add(additionalTrailOffset4, weaponLocation, null),
                        weapon.getSlot().isTurret() ? MUZZLE_OFFSET_TURRET_TRAIL2 : MUZZLE_OFFSET_HARDPOINT_TRAIL2, weaponFacing);

                MagicTrailPlugin.addTrailMemberSimple(
                        weapon.getShip(),
                        trailID,
                        Global.getSettings().getSprite("fx", "trails_trail_smooth"),
                        muzzleLocationTrailBlowbackLeft,
                        250f,
                        trailDirLeft,
                        40f,
                        150f,
                        new Color(162, 255, 235, 255),
                        1f-(timeBlowback * 0.1f),
                        0f,
                        0f,
                        0.33f,
                        true
                );
                MagicTrailPlugin.addTrailMemberSimple(
                        weapon.getShip(),
                        trailID2,
                        Global.getSettings().getSprite("fx", "trails_trail_smooth"),
                        muzzleLocationTrailBlowbackRight,
                        250f,
                        trailDirRight,
                        40f,
                        150f,
                        new Color(166, 255, 233, 255),
                        1f-(timeBlowback * 0.1f),
                        0f,
                        0f,
                        0.33f,
                        true
                );

/*                MagicTrailPlugin.addTrailMemberSimple(
                        weapon.getShip(),
                        trailID3,
                        Global.getSettings().getSprite("fx", "trails_trail_smooth"),
                        muzzleLocationTrailBlowbackLeft2,
                        250f,
                        trailDirLeft,
                        25f,
                        100f,
                        new Color(194, 255, 240, 255),
                        1f,
                        0f,
                        0f,
                        0.25f,
                        true
                );

                MagicTrailPlugin.addTrailMemberSimple(
                        weapon.getShip(),
                        trailID4,
                        Global.getSettings().getSprite("fx", "trails_trail_smooth"),
                        muzzleLocationTrailBlowbackRight2,
                        250f,
                        trailDirRight,
                        25f,
                        100f,
                        new Color(194, 255, 240, 255),
                        1f,
                        0f,
                        0f,
                        0.25f,
                        true
                );
                */
            }


        } else {
            trailID = null;
            trailID2 = null;
        }


    }


    //Shorthand function for actually spawning the particles
    private void spawnParticles(CombatEngineAPI engine, WeaponAPI weapon, float count, String type, boolean anchorAlternation, Vector2f spawnPoint, Color color, float sizeMin, float sizeMax,
                                float velocityMin, float velocityMax, float durationMin, float durationMax,
                                float offsetMin, float offsetMax, float arc, float arcFacing) {
        //First, ensure we take barrel position into account if we use Anchor Alternation (note that the spawn location is actually rotated 90 degrees wrong, so we invert their x and y values)
        Vector2f trueCenterLocation = new Vector2f(spawnPoint.y, spawnPoint.x);
        float trueArcFacing = arcFacing;
        if (anchorAlternation) {
            if (weapon.getSlot().isHardpoint()) {
                trueCenterLocation.x += weapon.getSpec().getHardpointFireOffsets().get(currentBarrel).x;
                trueCenterLocation.y += weapon.getSpec().getHardpointFireOffsets().get(currentBarrel).y;
                trueArcFacing += weapon.getSpec().getHardpointAngleOffsets().get(currentBarrel);
            } else if (weapon.getSlot().isTurret()) {
                trueCenterLocation.x += weapon.getSpec().getTurretFireOffsets().get(currentBarrel).x;
                trueCenterLocation.y += weapon.getSpec().getTurretFireOffsets().get(currentBarrel).y;
                trueArcFacing += weapon.getSpec().getTurretAngleOffsets().get(currentBarrel);
            } else {
                trueCenterLocation.x += weapon.getSpec().getHiddenFireOffsets().get(currentBarrel).x;
                trueCenterLocation.y += weapon.getSpec().getHiddenFireOffsets().get(currentBarrel).y;
                trueArcFacing += weapon.getSpec().getHiddenAngleOffsets().get(currentBarrel);
            }
        }

        //Then, we offset the true position and facing with our weapon's position and facing, while also rotating the position depending on facing
        trueArcFacing += weapon.getCurrAngle();
        trueCenterLocation = VectorUtils.rotate(trueCenterLocation, weapon.getCurrAngle(), new Vector2f(0f, 0f));
        trueCenterLocation.x += weapon.getLocation().x;
        trueCenterLocation.y += weapon.getLocation().y;

        //Then, we can finally start spawning particles
        float counter = count;
        while (Math.random() < counter) {
            //Ticks down the counter
            counter--;

            //Gets a velocity for the particle
            float arcPoint = MathUtils.getRandomNumberInRange(trueArcFacing - (arc / 2f), trueArcFacing + (arc / 2f));
            Vector2f velocity = MathUtils.getPointOnCircumference(weapon.getShip().getVelocity(), MathUtils.getRandomNumberInRange(velocityMin, velocityMax),
                    arcPoint);

            //Gets a spawn location in the cone, depending on our offsetMin/Max
            Vector2f spawnLocation = MathUtils.getPointOnCircumference(trueCenterLocation, MathUtils.getRandomNumberInRange(offsetMin, offsetMax),
                    arcPoint);

            //Gets our duration
            float duration = MathUtils.getRandomNumberInRange(durationMin, durationMax);

            //Gets our size
            float size = MathUtils.getRandomNumberInRange(sizeMin, sizeMax);

            //Finally, determine type of particle to actually spawn and spawns it
            switch (type) {
                case "SMOOTH":
                    engine.addSmoothParticle(spawnLocation, velocity, size, 1f, duration, color);
                    break;
                case "SMOKE":
                    engine.addSmokeParticle(spawnLocation, velocity, size, 1f, duration, color);
                    break;
                case "NEBULA":
                    engine.addNebulaParticle(spawnLocation, velocity, size, 1.5f, 0.1f, 0.2f, duration, color);
                    break;
                case "NEBULA_SMOKE":
                    engine.addNebulaSmokeParticle(spawnLocation, velocity, size, 1.5f, 0.1f, 0.2f, duration, color);
                    break;
                case "NEBULA_SWIRLY":
                    engine.addSwirlyNebulaParticle(spawnLocation, velocity, size, 1.5f, 0.1f, 0.2f, duration, color, true);
                    break;
                default:
                    engine.addHitParticle(spawnLocation, velocity, size, 10f, duration, color);
                    break;
            }
        }
    }

    public void onFire(final DamagingProjectileAPI projectile, WeaponAPI weapon, final CombatEngineAPI engine) {
        AddXLLaidlawProj(projectile);
        projectile.setCollisionClass(CollisionClass.NONE);

        Vector2f weaponLocation = weapon.getLocation();
        float weaponFacing = weapon.getCurrAngle();
        Vector2f additionalOffset = VectorUtils.rotate(new Vector2f(0, 50), weaponFacing - 90);
        Vector2f.add(additionalOffset, weaponLocation, null);
        Vector2f muzzleLocationShockwave = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isTurret() ? MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE : MUZZLE_OFFSET_TURRET_SHOCKWAVE, weaponFacing);
        Vector2f muzzleLocationShockwaveSprite1 = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isTurret() ? MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE_SPRITE1 : MUZZLE_OFFSET_TURRET_SHOCKWAVE_SPRITE1, weaponFacing);
        Vector2f muzzleLocationShockwaveSprite2 = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isTurret() ? MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE_SPRITE2 : MUZZLE_OFFSET_TURRET_SHOCKWAVE_SPRITE2, weaponFacing);
        Vector2f muzzleLocationShockwaveSprite3 = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isTurret() ? MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE_SPRITE3 : MUZZLE_OFFSET_TURRET_SHOCKWAVE_SPRITE3, weaponFacing);

        float trueArcFacing = weapon.getCurrAngle();
        trueArcFacing += weapon.getSpec().getTurretAngleOffsets().get(currentBarrel);


        Vector2f speed = weapon.getShip().getVelocity();
        for (int I = 0; I < 3; I++) {
            float shrapnelDir1 = weapon.getCurrAngle() + 5f + MathUtils.getRandomNumberInRange(-2.5f, 2.5f);
            DamagingProjectileAPI xlLaidlawShrapnel1 = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(weapon.getShip(), weapon, "vic_raum_weapon_xl_laidlaw_shrapnel", muzzleLocationShockwave,
                    shrapnelDir1, speed);
            xlLaidlawShrapnel1.getVelocity().scale(MathUtils.getRandomNumberInRange(0.20f, 0.60f));
            xlLaidlawShrapnel1.getProjectileSpec().setFadeTime(MathUtils.getRandomNumberInRange(0.25f, 0.4f));
        }
        for (int I = 0; I < 3; I++) {
            float shrapnelDir2 = weapon.getCurrAngle() - 5f + MathUtils.getRandomNumberInRange(-2.5f, 2.5f);
            DamagingProjectileAPI xlLaidlawShrapnel2 = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(weapon.getShip(), weapon, "vic_raum_weapon_xl_laidlaw_shrapnel", muzzleLocationShockwave,
                    shrapnelDir2, speed);
            xlLaidlawShrapnel2.getVelocity().scale(MathUtils.getRandomNumberInRange(0.20f, 0.60f));
            xlLaidlawShrapnel2.getProjectileSpec().setFadeTime(MathUtils.getRandomNumberInRange(0.25f, 0.4f));
        }

        engine.addHitParticle(weaponLocation, ZERO, 500, 1f, 0.5f, new Color(215, 241, 234, 255));

        WaveDistortion wave = new WaveDistortion(muzzleLocationShockwave, ZERO);
        wave.setIntensity(5f);
        wave.setSize(150f);
        wave.flip(true);
        wave.setLifetime(0f);
        wave.fadeOutIntensity(0.2f);
        wave.setLocation(muzzleLocationShockwave);
        DistortionShader.addDistortion(wave);

        if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
            light = true;
        }

        if (light) {
            vic_graphicLibEffects.CustomRippleDistortion(
                    muzzleLocationShockwave,
                    ZERO,
                    150,
                    4,
                    false,
                    trueArcFacing + 180f,
                    160,
                    1f,
                    0.1f,
                    0.15f,
                    0.5f,
                    0.35f,
                    0f
            );

            vic_graphicLibEffects.CustomRippleDistortion(
                    muzzleLocationShockwave,
                    ZERO,
                    200,
                    2.5f,
                    false,
                    trueArcFacing + 180f,
                    160,
                    1f,
                    0.1f,
                    0.25f,
                    0.5f,
                    0.6f,
                    0f
            );
            vic_graphicLibEffects.CustomRippleDistortion(
                    muzzleLocationShockwave,
                    ZERO,
                    75,
                    2,
                    false,
                    trueArcFacing,
                    200,
                    1f,
                    0.1f,
                    0.15f,
                    0.5f,
                    0.35f,
                    0f
            );

            vic_graphicLibEffects.CustomRippleDistortion(
                    muzzleLocationShockwave,
                    ZERO,
                    150,
                    1.5f,
                    false,
                    trueArcFacing,
                    200,
                    1f,
                    0.1f,
                    0.25f,
                    0.5f,
                    0.6f,
                    0f
            );
        }

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_xl_laidlaw_shockwave"),
                muzzleLocationShockwaveSprite1,
                ZERO,
                new Vector2f(25f, 100),
                new Vector2f(25, 100),
                weaponFacing,
                0f,
                new Color(255, 255, 255, 255),
                true,
                0.0f,
                0f,
                0.5f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_xl_laidlaw_shockwave"),
                muzzleLocationShockwaveSprite2,
                ZERO,
                new Vector2f(17.5f, 70),
                new Vector2f(17.5f, 70),
                weaponFacing,
                0f,
                new Color(255, 255, 255, 255),
                true,
                0.0f,
                0f,
                0.66f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_xl_laidlaw_shockwave"),
                muzzleLocationShockwaveSprite3,
                ZERO,
                new Vector2f(10f, 40),
                new Vector2f(10, 40),
                weaponFacing,
                0f,
                new Color(255, 255, 255, 255),
                true,
                0.0f,
                0f,
                0.8f
        );

    }
}