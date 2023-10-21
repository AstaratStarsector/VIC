package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.util.Misc.ZERO;
import static data.scripts.plugins.vic_combatPlugin.AddAlkonostProj;

public class vic_alkonostOnFire implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {


    private static final Color PARTICLE_COLOR_EXTRA = new Color(45, 255, 97);
    private static final Color GLOW_COLOR = new Color(0, 229, 4, 50);
    private static final Color FLASH_COLOR = new Color(182, 255, 188);
    private static final int NUM_PARTICLES = 30;

    private boolean light = false;


    private final IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.1f);

    boolean weaponChargedownBlowback = false;
    boolean emitSmokeBlowback = false;

    float
            durationBlowback = 0.75f,
            timeBlowback = 0f;


    final float MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE = 15f;
    final float MUZZLE_OFFSET_TURRET_SHOCKWAVE = 15f;
    final float MUZZLE_OFFSET_TURRET_BLOWBACK = -60f;
    final float MUZZLE_OFFSET_HARDPOINT_BLOWBACK = -60f;


    /*

          HOW TO USE:
          USED_IDS specifies which IDs to use for the rest of the script; any ID is valid EXCEPT the unique ID "default". Each ID should only be used once on the same weapon
          The script will spawn one particle "system" for each ID in this list, with the specific attributes of that ID.

          All the different Maps<> specify the attributes of each of the particle "systems"; they MUST have something defined as "default", and can have specific fields for specific IDs
          in the USED_IDS list; any field not filled in for a specific ID will revert to "default" instead.

      */
    private static final List<String> USED_IDS = new ArrayList<>();

    static {
        USED_IDS.add("CHARGEUP_PARTICLES1");
        USED_IDS.add("CHARGEUP_PARTICLES2");
        USED_IDS.add("CHARGEUP_PARTICLES3");
        USED_IDS.add("CHARGEUP_PARTICLES4");
        USED_IDS.add("CHARGEUP_PARTICLES5");
        USED_IDS.add("CHARGEUP_PARTICLES6");
        USED_IDS.add("BLOWBACK_ID_1");
        USED_IDS.add("BLOWBACK_ID_2");
        USED_IDS.add("BLOWBACK_ID_3");
        USED_IDS.add("BLOWBACK_ID_4");

    }

    //The amount of particles spawned immediately when the weapon reaches full charge level
    //  -For projectile weapons, this is when the projectile is actually fired
    //  -For beam weapons, this is when the beam has reached maximum brightness
    private static final Map<String, Integer> ON_SHOT_PARTICLE_COUNT = new HashMap<>();

    static {
        ON_SHOT_PARTICLE_COUNT.put("default", 15);
        ON_SHOT_PARTICLE_COUNT.put("CHARGEUP_PARTICLES1", 0);
        ON_SHOT_PARTICLE_COUNT.put("CHARGEUP_PARTICLES2", 0);
        ON_SHOT_PARTICLE_COUNT.put("CHARGEUP_PARTICLES3", 0);
        ON_SHOT_PARTICLE_COUNT.put("CHARGEUP_PARTICLES4", 0);
        ON_SHOT_PARTICLE_COUNT.put("CHARGEUP_PARTICLES5", 0);
        ON_SHOT_PARTICLE_COUNT.put("CHARGEUP_PARTICLES6", 0);


    }

    //How many particles are spawned each second the weapon is firing, on average
    private static final Map<String, Float> PARTICLES_PER_SECOND = new HashMap<>();

    static {
        PARTICLES_PER_SECOND.put("default", 0f);
        PARTICLES_PER_SECOND.put("CHARGEUP_PARTICLES1", 25f);
        PARTICLES_PER_SECOND.put("CHARGEUP_PARTICLES2", 25f);
        PARTICLES_PER_SECOND.put("CHARGEUP_PARTICLES3", 25f);
        PARTICLES_PER_SECOND.put("CHARGEUP_PARTICLES4", 30f);
        PARTICLES_PER_SECOND.put("CHARGEUP_PARTICLES5", 30f);
        PARTICLES_PER_SECOND.put("CHARGEUP_PARTICLES6", 30f);
        PARTICLES_PER_SECOND.put("BLOWBACK_ID_1", 15f);
        PARTICLES_PER_SECOND.put("BLOWBACK_ID_2", 15f);
        PARTICLES_PER_SECOND.put("BLOWBACK_ID_3", 15f);
        PARTICLES_PER_SECOND.put("BLOWBACK_ID_4", 15f);

    }

    //Does the PARTICLES_PER_SECOND field get multiplied by the weapon's current chargeLevel?
    private static final Map<String, Boolean> AFFECTED_BY_CHARGELEVEL = new HashMap<>();

    static {
        AFFECTED_BY_CHARGELEVEL.put("default", false);
        AFFECTED_BY_CHARGELEVEL.put("CHARGEUP_PARTICLES1", true);
        AFFECTED_BY_CHARGELEVEL.put("CHARGEUP_PARTICLES2", true);
        AFFECTED_BY_CHARGELEVEL.put("CHARGEUP_PARTICLES3", true);
        AFFECTED_BY_CHARGELEVEL.put("CHARGEUP_PARTICLES4", true);
        AFFECTED_BY_CHARGELEVEL.put("CHARGEUP_PARTICLES5", true);
        AFFECTED_BY_CHARGELEVEL.put("CHARGEUP_PARTICLES6", true);

    }

    //When are the particles spawned (only used for PARTICLES_PER_SECOND)? Valid values are "CHARGEUP", "FIRING", "CHARGEDOWN", "READY" (not on cooldown or firing) and "COOLDOWN".
    //  Multiple of these values can be combined via "-" inbetween; "CHARGEUP-CHARGEDOWN" is for example valid
    private static final Map<String, String> PARTICLE_SPAWN_MOMENT = new HashMap<>();

    static {
        PARTICLE_SPAWN_MOMENT.put("default", "FIRING");
        PARTICLE_SPAWN_MOMENT.put("CHARGEUP_PARTICLES1", "CHARGEUP");
        PARTICLE_SPAWN_MOMENT.put("CHARGEUP_PARTICLES2", "CHARGEUP");
        PARTICLE_SPAWN_MOMENT.put("CHARGEUP_PARTICLES3", "CHARGEUP");
        PARTICLE_SPAWN_MOMENT.put("CHARGEUP_PARTICLES4", "CHARGEUP");
        PARTICLE_SPAWN_MOMENT.put("CHARGEUP_PARTICLES5", "CHARGEUP");
        PARTICLE_SPAWN_MOMENT.put("CHARGEUP_PARTICLES6", "CHARGEUP");
        PARTICLE_SPAWN_MOMENT.put("BLOWBACK_ID_1", "CHARGEDOWN");
        PARTICLE_SPAWN_MOMENT.put("BLOWBACK_ID_2", "CHARGEDOWN");
        PARTICLE_SPAWN_MOMENT.put("BLOWBACK_ID_3", "CHARGEDOWN");
        PARTICLE_SPAWN_MOMENT.put("BLOWBACK_ID_4", "CHARGEDOWN");

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
    private static final Map<String, Vector2f> PARTICLE_SPAWN_POINT_TURRET_RIGHT = new HashMap<>();

    static {
        PARTICLE_SPAWN_POINT_TURRET.put("default", new Vector2f(0f, 0f));
        PARTICLE_SPAWN_POINT_TURRET.put("CHARGEUP_PARTICLES1", new Vector2f(0f, 18f));
        PARTICLE_SPAWN_POINT_TURRET.put("CHARGEUP_PARTICLES2", new Vector2f(0f, 18f));
        PARTICLE_SPAWN_POINT_TURRET.put("CHARGEUP_PARTICLES3", new Vector2f(0f, 18f));
        PARTICLE_SPAWN_POINT_TURRET.put("CHARGEUP_PARTICLES4", new Vector2f(0f, 18f));
        PARTICLE_SPAWN_POINT_TURRET.put("CHARGEUP_PARTICLES5", new Vector2f(0f, 18f));
        PARTICLE_SPAWN_POINT_TURRET.put("CHARGEUP_PARTICLES6", new Vector2f(0f, 18f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_ID_1", new Vector2f(20f, 10f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_ID_2", new Vector2f(-20f, 10f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_ID_3", new Vector2f(20f, -30f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_ID_4", new Vector2f(-20f, -30f));


        PARTICLE_SPAWN_POINT_TURRET_RIGHT.put("default", new Vector2f(0f, 0f));


    }

    //The position the particles are spawned (or at least where their arc originates when using offsets) compared to their weapon's center [or shot offset, see
    //SPAWN_POINT_ANCHOR_ALTERNATION above], if the weapon is a hardpoint
    private static final Map<String, Vector2f> PARTICLE_SPAWN_POINT_HARDPOINT = new HashMap<>();
    private static final Map<String, Vector2f> PARTICLE_SPAWN_POINT_HARDPOINT_RIGHT = new HashMap<>();

    static {
        PARTICLE_SPAWN_POINT_HARDPOINT.put("default", new Vector2f(0f, 0f));

        PARTICLE_SPAWN_POINT_HARDPOINT_RIGHT.put("default", new Vector2f(0f, 0f));

    }

    //Which kind of particle is spawned (valid values are "SMOOTH", "BRIGHT" and "SMOKE")
    private static final Map<String, String> PARTICLE_TYPE = new HashMap<>();

    static {
        PARTICLE_TYPE.put("default", "BRIGHT");
        PARTICLE_TYPE.put("CHARGEUP_PARTICLES1", "BRIGHT");
        PARTICLE_TYPE.put("CHARGEUP_PARTICLES2", "BRIGHT");
        PARTICLE_TYPE.put("CHARGEUP_PARTICLES3", "BRIGHT");
        PARTICLE_TYPE.put("CHARGEUP_PARTICLES4", "BRIGHT");
        PARTICLE_TYPE.put("CHARGEUP_PARTICLES5", "BRIGHT");
        PARTICLE_TYPE.put("CHARGEUP_PARTICLES6", "BRIGHT");
        PARTICLE_TYPE.put("BLOWBACK_ID_1", "SMOKE");
        PARTICLE_TYPE.put("BLOWBACK_ID_2", "SMOKE");
        PARTICLE_TYPE.put("BLOWBACK_ID_3", "SMOKE");
        PARTICLE_TYPE.put("BLOWBACK_ID_4", "SMOKE");


    }

    //What color does the particles have?
    private static final Map<String, Color> PARTICLE_COLOR = new HashMap<>();

    static {
        PARTICLE_COLOR.put("default", new Color(200, 190, 190, 50));
        PARTICLE_COLOR.put("CHARGEUP_PARTICLES1", new Color(95, 255, 95, 255));
        PARTICLE_COLOR.put("CHARGEUP_PARTICLES2", new Color(95, 255, 135, 255));
        PARTICLE_COLOR.put("CHARGEUP_PARTICLES3", new Color(95, 255, 170, 255));
        PARTICLE_COLOR.put("CHARGEUP_PARTICLES4", new Color(95, 255, 95, 255));
        PARTICLE_COLOR.put("CHARGEUP_PARTICLES5", new Color(95, 255, 135, 255));
        PARTICLE_COLOR.put("CHARGEUP_PARTICLES6", new Color(95, 255, 170, 255));
        PARTICLE_COLOR.put("BLOWBACK_ID_1", new Color(255, 255, 255, 200));
        PARTICLE_COLOR.put("BLOWBACK_ID_2", new Color(255, 255, 255, 200));
        PARTICLE_COLOR.put("BLOWBACK_ID_3", new Color(255, 255, 255, 200));
        PARTICLE_COLOR.put("BLOWBACK_ID_4", new Color(255, 255, 255, 200));


    }

    //What's the smallest size the particles can have?
    private static final Map<String, Float> PARTICLE_SIZE_MIN = new HashMap<>();

    static {
        PARTICLE_SIZE_MIN.put("default", 5f);
        PARTICLE_SIZE_MIN.put("CHARGEUP_PARTICLES1", 3f);
        PARTICLE_SIZE_MIN.put("CHARGEUP_PARTICLES2", 3f);
        PARTICLE_SIZE_MIN.put("CHARGEUP_PARTICLES3", 3f);
        PARTICLE_SIZE_MIN.put("CHARGEUP_PARTICLES4", 2f);
        PARTICLE_SIZE_MIN.put("CHARGEUP_PARTICLES5", 2f);
        PARTICLE_SIZE_MIN.put("CHARGEUP_PARTICLES6", 2f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_ID_1", 7.5f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_ID_2", 7.5f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_ID_3", 7.5f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_ID_4", 7.5f);


    }

    //What's the largest size the particles can have?
    private static final Map<String, Float> PARTICLE_SIZE_MAX = new HashMap<>();

    static {
        PARTICLE_SIZE_MAX.put("default", 20f);
        PARTICLE_SIZE_MAX.put("CHARGEUP_PARTICLES1", 10f);
        PARTICLE_SIZE_MAX.put("CHARGEUP_PARTICLES2", 10f);
        PARTICLE_SIZE_MAX.put("CHARGEUP_PARTICLES3", 10f);
        PARTICLE_SIZE_MAX.put("CHARGEUP_PARTICLES4", 7.5f);
        PARTICLE_SIZE_MAX.put("CHARGEUP_PARTICLES5", 7.5f);
        PARTICLE_SIZE_MAX.put("CHARGEUP_PARTICLES6", 7.5f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_ID_1", 15f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_ID_2", 15f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_ID_3", 15f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_ID_4", 15f);


    }

    //What's the lowest velocity a particle can spawn with (can be negative)?
    private static final Map<String, Float> PARTICLE_VELOCITY_MIN = new HashMap<>();

    static {
        PARTICLE_VELOCITY_MIN.put("default", 0f);
        PARTICLE_VELOCITY_MIN.put("CHARGEUP_PARTICLES1", 100f);
        PARTICLE_VELOCITY_MIN.put("CHARGEUP_PARTICLES2", 100f);
        PARTICLE_VELOCITY_MIN.put("CHARGEUP_PARTICLES3", 100f);
        PARTICLE_VELOCITY_MIN.put("CHARGEUP_PARTICLES4", 200f);
        PARTICLE_VELOCITY_MIN.put("CHARGEUP_PARTICLES5", 200f);
        PARTICLE_VELOCITY_MIN.put("CHARGEUP_PARTICLES6", 200f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_ID_1", 35f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_ID_2", 35f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_ID_3", 35f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_ID_4", 35f);


    }

    //What's the highest velocity a particle can spawn with (can be negative)?
    private static final Map<String, Float> PARTICLE_VELOCITY_MAX = new HashMap<>();

    static {
        PARTICLE_VELOCITY_MAX.put("default", 40f);
        PARTICLE_VELOCITY_MAX.put("CHARGEUP_PARTICLES1", 450f);
        PARTICLE_VELOCITY_MAX.put("CHARGEUP_PARTICLES2", 450f);
        PARTICLE_VELOCITY_MAX.put("CHARGEUP_PARTICLES3", 450f);
        PARTICLE_VELOCITY_MAX.put("CHARGEUP_PARTICLES4", 600f);
        PARTICLE_VELOCITY_MAX.put("CHARGEUP_PARTICLES5", 600f);
        PARTICLE_VELOCITY_MAX.put("CHARGEUP_PARTICLES6", 600f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_ID_1", 70f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_ID_2", 70f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_ID_3", 70f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_ID_4", 70f);


    }

    //The shortest duration a particle will last before completely fading away
    private static final Map<String, Float> PARTICLE_DURATION_MIN = new HashMap<>();

    static {
        PARTICLE_DURATION_MIN.put("default", 1f);
        PARTICLE_DURATION_MIN.put("CHARGEUP_PARTICLES1", 0.15f);
        PARTICLE_DURATION_MIN.put("CHARGEUP_PARTICLES2", 0.15f);
        PARTICLE_DURATION_MIN.put("CHARGEUP_PARTICLES3", 0.15f);
        PARTICLE_DURATION_MIN.put("CHARGEUP_PARTICLES4", 0.10f);
        PARTICLE_DURATION_MIN.put("CHARGEUP_PARTICLES5", 0.10f);
        PARTICLE_DURATION_MIN.put("CHARGEUP_PARTICLES6", 0.10f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_ID_1", 0.33f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_ID_2", 0.33f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_ID_3", 0.33f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_ID_4", 0.33f);


    }

    //The longest duration a particle will last before completely fading away
    private static final Map<String, Float> PARTICLE_DURATION_MAX = new HashMap<>();

    static {
        PARTICLE_DURATION_MAX.put("default", 1.5f);
        PARTICLE_DURATION_MAX.put("CHARGEUP_PARTICLES1", 0.30f);
        PARTICLE_DURATION_MAX.put("CHARGEUP_PARTICLES2", 0.30f);
        PARTICLE_DURATION_MAX.put("CHARGEUP_PARTICLES3", 0.30f);
        PARTICLE_DURATION_MAX.put("CHARGEUP_PARTICLES4", 0.2f);
        PARTICLE_DURATION_MAX.put("CHARGEUP_PARTICLES5", 0.2f);
        PARTICLE_DURATION_MAX.put("CHARGEUP_PARTICLES6", 0.2f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_ID_1", 0.66f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_ID_2", 0.66f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_ID_3", 0.66f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_ID_4", 0.66f);

    }

    //The shortest along their velocity vector any individual particle is allowed to spawn (can be negative to spawn behind their origin point)
    private static final Map<String, Float> PARTICLE_OFFSET_MIN = new HashMap<>();

    static {
        PARTICLE_OFFSET_MIN.put("default", 0f);
        PARTICLE_OFFSET_MIN.put("CHARGEUP_PARTICLES1", -125f);
        PARTICLE_OFFSET_MIN.put("CHARGEUP_PARTICLES2", -125f);
        PARTICLE_OFFSET_MIN.put("CHARGEUP_PARTICLES3", -125f);
        PARTICLE_OFFSET_MIN.put("CHARGEUP_PARTICLES4", -200f);
        PARTICLE_OFFSET_MIN.put("CHARGEUP_PARTICLES5", -200f);
        PARTICLE_OFFSET_MIN.put("CHARGEUP_PARTICLES6", -200f);


    }

    //The furthest along their velocity vector any individual particle is allowed to spawn (can be negative to spawn behind their origin point)
    private static final Map<String, Float> PARTICLE_OFFSET_MAX = new HashMap<>();

    static {
        PARTICLE_OFFSET_MAX.put("default", 0f);
        PARTICLE_OFFSET_MAX.put("CHARGEUP_PARTICLES1", -50f);
        PARTICLE_OFFSET_MAX.put("CHARGEUP_PARTICLES2", -50f);
        PARTICLE_OFFSET_MAX.put("CHARGEUP_PARTICLES3", -50f);
        PARTICLE_OFFSET_MAX.put("CHARGEUP_PARTICLES4", -100f);
        PARTICLE_OFFSET_MAX.put("CHARGEUP_PARTICLES5", -100f);
        PARTICLE_OFFSET_MAX.put("CHARGEUP_PARTICLES6", -100f);


    }

    //The width of the "arc" the particles spawn in; affects both offset and velocity. 360f = full circle, 0f = straight line
    private static final Map<String, Float> PARTICLE_ARC = new HashMap<>();

    static {
        PARTICLE_ARC.put("default", 10f);
        PARTICLE_ARC.put("CHARGEUP_PARTICLES1", 300f);
        PARTICLE_ARC.put("CHARGEUP_PARTICLES2", 300f);
        PARTICLE_ARC.put("CHARGEUP_PARTICLES3", 300f);
        PARTICLE_ARC.put("CHARGEUP_PARTICLES4", 25f);
        PARTICLE_ARC.put("CHARGEUP_PARTICLES5", 25f);
        PARTICLE_ARC.put("CHARGEUP_PARTICLES6", 25f);
        PARTICLE_ARC.put("BLOWBACK_ID_1", 45f);
        PARTICLE_ARC.put("BLOWBACK_ID_2", 45f);
        PARTICLE_ARC.put("BLOWBACK_ID_3", 45f);
        PARTICLE_ARC.put("BLOWBACK_ID_4", 45f);


    }

    //The offset of the "arc" the particles spawn in, compared to the weapon's forward facing.
    //  For example: 90f = the center of the arc is 90 degrees clockwise around the weapon, 0f = the same arc center as the weapon's facing.
    private static final Map<String, Float> PARTICLE_ARC_FACING = new HashMap<>();
    private static final Map<String, Float> PARTICLE_ARC_FACING_RIGHT = new HashMap<>();

    static {
        PARTICLE_ARC_FACING.put("default", 0f);
        PARTICLE_ARC_FACING.put("CHARGEUP_PARTICLES4", 180f);
        PARTICLE_ARC_FACING.put("CHARGEUP_PARTICLES5", 180f);
        PARTICLE_ARC_FACING.put("CHARGEUP_PARTICLES6", 180f);
        PARTICLE_ARC_FACING.put("BLOWBACK_ID_1", 75f);
        PARTICLE_ARC_FACING.put("BLOWBACK_ID_2", -75f);
        PARTICLE_ARC_FACING.put("BLOWBACK_ID_3", 135f);
        PARTICLE_ARC_FACING.put("BLOWBACK_ID_4", -135f);


        PARTICLE_ARC_FACING_RIGHT.put("default", 0f);

    }

    //How far away from the screen's edge the particles are allowed to spawn. Lower values mean better performance, but
    //too low values will cause pop-in of particles. Generally, the longer the particle's lifetime, the higher this
    //value should be
    private static final Map<String, Float> PARTICLE_SCREENSPACE_CULL_DISTANCE = new HashMap<>();

    static {
        PARTICLE_SCREENSPACE_CULL_DISTANCE.put("default", 600f);
    }


    //-----------------------------------------------------------You don't need to touch stuff beyond this point!------------------------------------------------------------


    //These are used in-script, so don't touch them!
    private boolean hasFiredThisCharge = false;
    private boolean hasFiredThisChargeBlowback = false;
    private int currentBarrel = 0;
    private boolean shouldOffsetBarrelExtra = false;
    private boolean RIGHT = false;

    //Instantiator

    boolean doOnce = true;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (doOnce) {
            RIGHT = weapon.getSlot().getLocation().y > 0;
            doOnce = false;
        }
        //Don't run while paused, or without a weapon
        if (weapon == null || amount <= 0f) {
            return;
        }

        if (doOnce) {
            if (!weapon.getShip().hasListenerOfClass(vic_gaganaUltraOnFire.vic_gaganaUltraListner.class)) {
                weapon.getShip().addListener(new vic_gaganaUltraOnFire.vic_gaganaUltraListner());
            }
            doOnce = false;
        }


        //Saves handy variables used later
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

        //Muzzle location calculation
        Vector2f pointExtra = new Vector2f();

        if (weapon.getSlot().isHardpoint()) {
            pointExtra.x = weapon.getSpec().getHardpointFireOffsets().get(0).x;
            pointExtra.y = weapon.getSpec().getHardpointFireOffsets().get(0).y;
        } else if (weapon.getSlot().isTurret()) {
            pointExtra.x = weapon.getSpec().getTurretFireOffsets().get(0).x;
            pointExtra.y = weapon.getSpec().getTurretFireOffsets().get(0).y;
        } else {
            pointExtra.x = weapon.getSpec().getHiddenFireOffsets().get(0).x;
            pointExtra.y = weapon.getSpec().getHiddenFireOffsets().get(0).y;
        }

        pointExtra = VectorUtils.rotate(pointExtra, weapon.getCurrAngle(), new Vector2f(0f, 0f));
        pointExtra.x += weapon.getLocation().x;
        pointExtra.y += weapon.getLocation().y;


        //Chargeup visuals

        if (chargeLevel > 0f && !hasFiredThisCharge) {

            effectInterval.advance(engine.getElapsedInLastFrame());
            if (effectInterval.intervalElapsed()) {
                Vector2f arcPoint = MathUtils.getRandomPointInCircle(pointExtra, 150f * chargeLevel);
                EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(weapon.getShip(), pointExtra, weapon.getShip(),
                        new SimpleEntity(arcPoint),
                        DamageType.FRAGMENTATION,
                        0f,
                        0f,
                        150f,
                        null,
                        5f + 10f * chargeLevel,
                        PARTICLE_COLOR_EXTRA,
                        FLASH_COLOR
                );
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
            emitSmokeBlowback = true;
            weaponChargedownBlowback = false;
        }


        if (timeBlowback >= durationBlowback) {
            timeBlowback = 0f;
            emitSmokeBlowback = false;
        }
        if (emitSmokeBlowback) {
            timeBlowback += amount;

            float smokeDir = weapon.getCurrAngle() + 180f;
            float smokeDirAngle = smokeDir + MathUtils.getRandomNumberInRange(-20, 20);

            Vector2f weaponLocation = weapon.getLocation();
            float shipFacing = weapon.getCurrAngle();
            Vector2f muzzleLocationBlowback = MathUtils.getPointOnCircumference(weaponLocation,
                    weapon.getSlot().isTurret() ? MUZZLE_OFFSET_TURRET_BLOWBACK : MUZZLE_OFFSET_HARDPOINT_BLOWBACK, shipFacing);

            Vector2f vel = (Vector2f) Misc.getUnitVectorAtDegreeAngle(smokeDirAngle).scale(100f);

            engine.addNebulaParticle(muzzleLocationBlowback, vel, MathUtils.getRandomNumberInRange(15f, 25f), 2.5f, 0.3f, 0.3f, 0.5f, new Color(95, 255, 130, 150));
            engine.addNebulaParticle(muzzleLocationBlowback, vel, MathUtils.getRandomNumberInRange(5f, 15f), 2.5f, 0.3f, 0.3f, 0.15f, new Color(141, 255, 127, 255));
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
        AddAlkonostProj(projectile);

        Vector2f weaponLocation = weapon.getLocation();
        float shipFacing = weapon.getCurrAngle();
        Vector2f muzzleLocationShockwave = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isTurret() ? MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE : MUZZLE_OFFSET_TURRET_SHOCKWAVE, shipFacing);


        WaveDistortion wave = new WaveDistortion(muzzleLocationShockwave, ZERO);
        wave.setIntensity(10f);
        wave.setSize(200f);
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
                    0,
                    360,
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
                    0,
                    360,
                    1f,
                    0.1f,
                    0.25f,
                    0.5f,
                    0.6f,
                    0f
            );
        }

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_alkonost_shockwave"),
                muzzleLocationShockwave,
                ZERO,
                new Vector2f(75, 200),
                new Vector2f(100, 200),
                shipFacing,
                0f,
                new Color(255, 255, 255, 255),
                true,
                0.0f,
                0f,
                0.75f
        );

        Global.getCombatEngine().spawnExplosion(muzzleLocationShockwave, new Vector2f(0f, 0f), PARTICLE_COLOR_EXTRA, 250f, 0.33f);
        Global.getCombatEngine().spawnExplosion(muzzleLocationShockwave, new Vector2f(0f, 0f), FLASH_COLOR, 180f, 0.33f);
        engine.addSmoothParticle(muzzleLocationShockwave, ZERO, 350f, 0.7f, 0.2f, PARTICLE_COLOR_EXTRA);
        engine.addSmoothParticle(muzzleLocationShockwave, ZERO, 450f, 0.7f, 1f, GLOW_COLOR);
        engine.addHitParticle(muzzleLocationShockwave, ZERO, 750f, 1f, 0.15f, FLASH_COLOR);
        for (int x = 0; x < NUM_PARTICLES; x++) {
            engine.addHitParticle(muzzleLocationShockwave,
                    MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 150f), (float) Math.random() * 360f),
                    5f, 1f, MathUtils.getRandomNumberInRange(0.3f, 0.6f), PARTICLE_COLOR_EXTRA);
        }


        /*
        engine.addPlugin(new EveryFrameCombatPlugin() {
            @Override
            public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

            }

            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                Global.getSoundPlayer().playLoop("vic_alkonost_projectile_loop", projectile, 1,1,projectile.getLocation(),projectile.getVelocity(),0.2f,0.2f);
                if (!engine.isEntityInPlay(projectile)) engine.removePlugin(this);
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

         */

    }


}

