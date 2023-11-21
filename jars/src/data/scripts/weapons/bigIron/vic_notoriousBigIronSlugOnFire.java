package data.scripts.weapons.bigIron;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.vic_weaponDamageListener;
import org.magiclib.util.MagicRender;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class vic_notoriousBigIronSlugOnFire implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    boolean firstBarrel = true;

    final float MUZZLE_OFFSET_HARDPOINT = 100f;
    final float MUZZLE_OFFSET_TURRET = 100f;
    final float SHELL_EJECTION_OFFSET_HARDPOINT = -33f;
    final float SHELL_EJECTION_OFFSET_TURRET = -33f;
    final float MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE = 90f + MathUtils.getRandomNumberInRange(-5f, 5f);
    final float MUZZLE_OFFSET_TURRET_SHOCKWAVE = 90f + MathUtils.getRandomNumberInRange(-5f, 5f);

    /*

          HOW TO USE:
          USED_IDS specifies which IDs to use for the rest of the script; any ID is valid EXCEPT the unique ID "default". Each ID should only be used once on the same weapon
          The script will spawn one particle "system" for each ID in this list, with the specific attributes of that ID.

          All the different Maps<> specify the attributes of each of the particle "systems"; they MUST have something defined as "default", and can have specific fields for specific IDs
          in the USED_IDS list; any field not filled in for a specific ID will revert to "default" instead.

      */
    private static final List<String> USED_IDS = new ArrayList<>();

    static {
        USED_IDS.add("FLASH_CORE_1");
        USED_IDS.add("FLASH_FRINGE_1");
        USED_IDS.add("BLOWBACK_ID_1");
        USED_IDS.add("BLOWBACK_ID_2");
        USED_IDS.add("BLOWBACK_ID_3");
        USED_IDS.add("CHARGEDOWN_STEAM");
    }

    //The amount of particles spawned immediately when the weapon reaches full charge level
    //  -For projectile weapons, this is when the projectile is actually fired
    //  -For beam weapons, this is when the beam has reached maximum brightness
    private static final Map<String, Integer> ON_SHOT_PARTICLE_COUNT = new HashMap<>();

    static {
        ON_SHOT_PARTICLE_COUNT.put("default", 15);
        ON_SHOT_PARTICLE_COUNT.put("FLASH_FRINGE_1", 1);
        ON_SHOT_PARTICLE_COUNT.put("FLASH_CORE_1", 1);
        ON_SHOT_PARTICLE_COUNT.put("BLOWBACK_ID_1", 15);
        ON_SHOT_PARTICLE_COUNT.put("BLOWBACK_ID_2", 1);
        ON_SHOT_PARTICLE_COUNT.put("BLOWBACK_ID_3", 1);
        ON_SHOT_PARTICLE_COUNT.put("CHARGEDOWN_STEAM", 0);
    }

    //How many particles are spawned each second the weapon is firing, on average
    private static final Map<String, Float> PARTICLES_PER_SECOND = new HashMap<>();

    static {
        PARTICLES_PER_SECOND.put("default", 0f);
        PARTICLES_PER_SECOND.put("CHARGEDOWN_STEAM", 30f);
    }

    //Does the PARTICLES_PER_SECOND field get multiplied by the weapon's current chargeLevel?
    private static final Map<String, Boolean> AFFECTED_BY_CHARGELEVEL = new HashMap<>();

    static {
        AFFECTED_BY_CHARGELEVEL.put("default", false);
    }

    //When are the particles spawned (only used for PARTICLES_PER_SECOND)? Valid values are "CHARGEUP", "FIRING", "CHARGEDOWN", "READY" (not on cooldown or firing) and "COOLDOWN".
    //  Multiple of these values can be combined via "-" inbetween; "CHARGEUP-CHARGEDOWN" is for example valid
    private static final Map<String, String> PARTICLE_SPAWN_MOMENT = new HashMap<>();

    static {
        PARTICLE_SPAWN_MOMENT.put("default", "FIRING");
        PARTICLE_SPAWN_MOMENT.put("CHARGEDOWN_STEAM", "CHARGEDOWN");

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
        PARTICLE_SPAWN_POINT_TURRET.put("default", new Vector2f(0f, 100f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_ID_1", new Vector2f(0f, -33f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_ID_2", new Vector2f(0f, -33f));
        PARTICLE_SPAWN_POINT_TURRET.put("BLOWBACK_ID_3", new Vector2f(0f, -33f));
        PARTICLE_SPAWN_POINT_TURRET.put("CHARGEDOWN_STEAM", new Vector2f(-30f, -5f));
    }

    //The position the particles are spawned (or at least where their arc originates when using offsets) compared to their weapon's center [or shot offset, see
    //SPAWN_POINT_ANCHOR_ALTERNATION above], if the weapon is a hardpoint
    private static final Map<String, Vector2f> PARTICLE_SPAWN_POINT_HARDPOINT = new HashMap<>();

    static {
        PARTICLE_SPAWN_POINT_HARDPOINT.put("default", new Vector2f(0f, 100f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put("BLOWBACK_ID_1", new Vector2f(-3f, -5f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put("BLOWBACK_ID_2", new Vector2f(-3f, -5f));
        PARTICLE_SPAWN_POINT_HARDPOINT.put("BLOWBACK_ID_3", new Vector2f(-3f, -5f));

    }

    //Which kind of particle is spawned (valid values are "SMOOTH", "BRIGHT" and "SMOKE")
    private static final Map<String, String> PARTICLE_TYPE = new HashMap<>();

    static {
        PARTICLE_TYPE.put("default", "SMOKE");
        PARTICLE_TYPE.put("FLASH_FRINGE_1", "BRIGHT");
        PARTICLE_TYPE.put("FLASH_CORE_1", "BRIGHT");
        PARTICLE_TYPE.put("BLOWBACK_ID_1", "SMOKE");
        PARTICLE_TYPE.put("BLOWBACK_ID_2", "BRIGHT");
        PARTICLE_TYPE.put("BLOWBACK_ID_3", "BRIGHT");
        PARTICLE_TYPE.put("CHARGEDOWN_STEAM", "NEBULA");
    }

    //What color does the particles have?
    private static final Map<String, Color> PARTICLE_COLOR = new HashMap<>();

    static {
        PARTICLE_COLOR.put("default", new Color(200, 190, 190, 50));
        PARTICLE_COLOR.put("FLASH_FRINGE_1", new Color(255, 100, 50,200));
        PARTICLE_COLOR.put("FLASH_CORE_1", new Color(255, 100, 100,255));
        PARTICLE_COLOR.put("BLOWBACK_ID_1", new Color(100, 100, 100, 150));
        PARTICLE_COLOR.put("BLOWBACK_ID_2", new Color(255, 125, 65, 150));
        PARTICLE_COLOR.put("BLOWBACK_ID_3", new Color(255, 234, 212,150));
        PARTICLE_COLOR.put("CHARGEDOWN_STEAM", new Color(75, 75, 75, 125));
    }

    //What's the smallest size the particles can have?
    private static final Map<String, Float> PARTICLE_SIZE_MIN = new HashMap<>();

    static {
        PARTICLE_SIZE_MIN.put("default", 5f);
        PARTICLE_SIZE_MIN.put("FLASH_FRINGE_1", 350f);
        PARTICLE_SIZE_MIN.put("FLASH_CORE_1", 200f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_ID_1", 15f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_ID_2", 75f);
        PARTICLE_SIZE_MIN.put("BLOWBACK_ID_3", 50f);
        PARTICLE_SIZE_MIN.put("CHARGEDOWN_STEAM", 15f);

    }

    //What's the largest size the particles can have?
    private static final Map<String, Float> PARTICLE_SIZE_MAX = new HashMap<>();

    static {
        PARTICLE_SIZE_MAX.put("default", 20f);
        PARTICLE_SIZE_MAX.put("FLASH_FRINGE_1", 350f);
        PARTICLE_SIZE_MAX.put("FLASH_CORE_1", 200f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_ID_1", 30f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_ID_2", 75f);
        PARTICLE_SIZE_MAX.put("BLOWBACK_ID_3", 50f);
        PARTICLE_SIZE_MAX.put("CHARGEDOWN_STEAM", 30f);

    }

    //What's the lowest velocity a particle can spawn with (can be negative)?
    private static final Map<String, Float> PARTICLE_VELOCITY_MIN = new HashMap<>();

    static {
        PARTICLE_VELOCITY_MIN.put("default", 0f);
        PARTICLE_VELOCITY_MIN.put("FLASH_FRINGE_1", 0f);
        PARTICLE_VELOCITY_MIN.put("FLASH_CORE_1", 0f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_ID_1", 1f);
        PARTICLE_VELOCITY_MIN.put("CHARGEDOWN_STEAM", 15f);

    }

    //What's the highest velocity a particle can spawn with (can be negative)?
    private static final Map<String, Float> PARTICLE_VELOCITY_MAX = new HashMap<>();

    static {
        PARTICLE_VELOCITY_MAX.put("default", 40f);
        PARTICLE_VELOCITY_MAX.put("FLASH_FRINGE_1", 0f);
        PARTICLE_VELOCITY_MAX.put("FLASH_CORE_1", 0f);
        PARTICLE_VELOCITY_MAX.put("BLOWBACK_ID_1", 50f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_ID_2", 0f);
        PARTICLE_VELOCITY_MIN.put("BLOWBACK_ID_3", 0f);
        PARTICLE_VELOCITY_MIN.put("CHARGEDOWN_STEAM", 40f);

    }

    //The shortest duration a particle will last before completely fading away
    private static final Map<String, Float> PARTICLE_DURATION_MIN = new HashMap<>();

    static {
        PARTICLE_DURATION_MIN.put("default", 1f);
        PARTICLE_DURATION_MIN.put("FLASH_FRINGE_1", 0.15f);
        PARTICLE_DURATION_MIN.put("FLASH_CORE_1", 0.25f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_ID_1", 0.75f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_ID_2", 0.3f);
        PARTICLE_DURATION_MIN.put("BLOWBACK_ID_3", 0.2f);
        PARTICLE_DURATION_MIN.put("CHARGEDOWN_STEAM", 0.33f);

    }

    //The longest duration a particle will last before completely fading away
    private static final Map<String, Float> PARTICLE_DURATION_MAX = new HashMap<>();

    static {
        PARTICLE_DURATION_MAX.put("default", 1.5f);
        PARTICLE_DURATION_MAX.put("FLASH_FRINGE_1", 0.15f);
        PARTICLE_DURATION_MAX.put("FLASH_CORE_1", 0.25f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_ID_1", 1.5f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_ID_2", 0.3f);
        PARTICLE_DURATION_MAX.put("BLOWBACK_ID_3", 0.2f);
        PARTICLE_DURATION_MAX.put("CHARGEDOWN_STEAM", 0.66f);

    }

    //The shortest along their velocity vector any individual particle is allowed to spawn (can be negative to spawn behind their origin point)
    private static final Map<String, Float> PARTICLE_OFFSET_MIN = new HashMap<>();

    static {
        PARTICLE_OFFSET_MIN.put("default", 0f);
        PARTICLE_OFFSET_MIN.put("FLASH_FRINGE_1", 0f);
        PARTICLE_OFFSET_MIN.put("FLASH_CORE_1", 0f);
    }

    //The furthest along their velocity vector any individual particle is allowed to spawn (can be negative to spawn behind their origin point)
    private static final Map<String, Float> PARTICLE_OFFSET_MAX = new HashMap<>();

    static {
        PARTICLE_OFFSET_MAX.put("default", 0f);
        PARTICLE_OFFSET_MAX.put("FLASH_FRINGE_1", 0f);
        PARTICLE_OFFSET_MAX.put("FLASH_CORE_1", 0f);
        PARTICLE_OFFSET_MAX.put("BLOWBACK_ID_1", 0f);
        PARTICLE_OFFSET_MAX.put("BLOWBACK_ID_2", 0f);
        PARTICLE_OFFSET_MAX.put("BLOWBACK_ID_3", 0f);

    }

    //The width of the "arc" the particles spawn in; affects both offset and velocity. 360f = full circle, 0f = straight line
    private static final Map<String, Float> PARTICLE_ARC = new HashMap<>();

    static {
        PARTICLE_ARC.put("default", 10f);
        PARTICLE_ARC.put("FLASH_FRINGE_1", 0f);
        PARTICLE_ARC.put("FLASH_CORE_1", 0f);
        PARTICLE_ARC.put("BLOWBACK_ID_1", 120f);
        PARTICLE_ARC.put("CHARGEDOWN_STEAM", 45f);

    }

    //The offset of the "arc" the particles spawn in, compared to the weapon's forward facing.
    //  For example: 90f = the center of the arc is 90 degrees clockwise around the weapon, 0f = the same arc center as the weapon's facing.
    private static final Map<String, Float> PARTICLE_ARC_FACING = new HashMap<>();

    static {
        PARTICLE_ARC_FACING.put("default", 0f);
        PARTICLE_ARC_FACING.put("BLOWBACK_ID_1", -180f);
        PARTICLE_ARC_FACING.put("CHARGEDOWN_STEAM", -90f);

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
    private int currentBarrel = 0;
    private boolean shouldOffsetBarrelExtra = false;

    float animStartTime = 0f,
            animEndTime = 0f;
    float totalFrames = 0,
            totalFireTime = 0,
            startPercent = 0,
            endPercent = 0;

    //Instantiator
    public vic_notoriousBigIronSlugOnFire() {
    }

    boolean doOnce = true;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run while paused, or without a weapon
        if (weapon == null || amount <= 0f) {
            return;
        }


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

        float shotFaction = 0;
        if (!fired) {
            shotFaction = ((((ProjectileWeaponSpecAPI) weapon.getSpec()).getChargeTime() * chargeLevel) / totalFireTime);
        } else {
            shotFaction = ((((ProjectileWeaponSpecAPI) weapon.getSpec()).getChargeTime() + ((totalFireTime - ((ProjectileWeaponSpecAPI) weapon.getSpec()).getChargeTime()) * (1 - chargeLevel))) / totalFireTime);
        }
        float animFraction = MathUtils.clamp((shotFaction - startPercent) / endPercent, 0, 1);
        weapon.getAnimation().setFrame(Math.round(totalFrames * animFraction));

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

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        Vector2f weaponLocation = weapon.getLocation();
        float shipFacing = weapon.getCurrAngle();
        float shellDir = weapon.getCurrAngle();
        float shellDirAngle = shellDir - MathUtils.getRandomNumberInRange(115, 155);
        float nebulaSideDir1 = weapon.getCurrAngle() + MathUtils.getRandomNumberInRange(-360f, 360f);
        float nebulaSideDir2 = weapon.getCurrAngle() + MathUtils.getRandomNumberInRange(-360f, 360f);
        float nebulaSideDir3 = weapon.getCurrAngle() + MathUtils.getRandomNumberInRange(-360f, 360f);
        float nebulaSideDir4 = weapon.getCurrAngle() + MathUtils.getRandomNumberInRange(-360f, 360f);
        float nebulaSideDir5 = weapon.getCurrAngle() + MathUtils.getRandomNumberInRange(-360f, 360f);

        Vector2f muzzleLocation = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isHardpoint() ? MUZZLE_OFFSET_HARDPOINT : MUZZLE_OFFSET_TURRET, shipFacing);
        Vector2f muzzleLocationShockwave = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isHardpoint() ? MUZZLE_OFFSET_HARDPOINT_SHOCKWAVE : MUZZLE_OFFSET_TURRET_SHOCKWAVE, shipFacing);
        Vector2f shellSpawnLocation = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isHardpoint() ? SHELL_EJECTION_OFFSET_HARDPOINT : SHELL_EJECTION_OFFSET_TURRET, shipFacing);

        Vector2f nebulaSpeed = (Vector2f) Misc.getUnitVectorAtDegreeAngle(shipFacing).scale(MathUtils.getRandomNumberInRange(15f, 22.5f));
        Vector2f nebulaSpeed2 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(shipFacing).scale(MathUtils.getRandomNumberInRange(5f, 15f));
        Vector2f nebulaSpeed3 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(shipFacing).scale(MathUtils.getRandomNumberInRange(0f, 5f));
        Vector2f nebulaSideSpeed1 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(nebulaSideDir1).scale(MathUtils.getRandomNumberInRange(0f, 15f));
        Vector2f nebulaSideSpeed2 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(nebulaSideDir2).scale(MathUtils.getRandomNumberInRange(0f, 15f));
        Vector2f nebulaSideSpeed3 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(nebulaSideDir3).scale(MathUtils.getRandomNumberInRange(0f, 15f));
        Vector2f nebulaSideSpeed4 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(nebulaSideDir4).scale(MathUtils.getRandomNumberInRange(0f, 15f));
        Vector2f nebulaSideSpeed5 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(nebulaSideDir5).scale(MathUtils.getRandomNumberInRange(0f, 15f));
        Vector2f shellSpeed = (Vector2f) Misc.getUnitVectorAtDegreeAngle(shellDirAngle).scale(30f + (MathUtils.getRandomNumberInRange(-10f, 15f)));


            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_notorious_big_iron_slug_shell"),
                    shellSpawnLocation,
                    shellSpeed,
                    new Vector2f(12, 18),
                    new Vector2f(0, 0),
                    360 * (float) Math.random(),
                    MathUtils.getRandomNumberInRange(-270f, 270f),
                    new Color(255, 255, 255, 255),
                    false,
                    0.0f,
                    1f,
                    MathUtils.getRandomNumberInRange(1.5f, 2.5f)
            );

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_gatebreaker_shotgun_shockwave"),
                    muzzleLocationShockwave,
                    weapon.getShip().getVelocity(),
                    new Vector2f(25, 50),
                    new Vector2f(60, 250),
                    shipFacing,
                    0f,
                    new Color(255, 255, 255, 255),
                    true,
                    0.0f,
                    0f,
                    0.5f
            );

            WaveDistortion wave = new WaveDistortion(muzzleLocation, ZERO);
            wave.setIntensity(3f);
            wave.setSize(100f);
            wave.flip(true);
            wave.setLifetime(0f);
            wave.fadeOutIntensity(0.25f);
            wave.setLocation(projectile.getLocation());
            DistortionShader.addDistortion(wave);


        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSpeed3, 10f, 5f, 0.2f, 0.2f, 2.5f, new Color(47, 42, 42, 225));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSpeed2, 15f, 3f, 0.2f, 0.2f, 2.25f, new Color(45, 37, 37, 225));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSpeed, 30f, 3f, 0.2f, 0.2f, 2f, new Color(36, 33, 33, 225));

        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed1, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(62, 46, 29, 100));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed2, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(50, 50, 50, 150));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed3, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(144, 102, 58, 100));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed4, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(50, 50, 50, 150));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed5, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(62, 46, 29, 100));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed1, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(50, 50, 50, 150));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed2, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(38, 22, 14, 100));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed3, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(50, 50, 50, 150));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed4, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(83, 47, 13, 100));
        Global.getCombatEngine().addNebulaSmokeParticle(muzzleLocation, nebulaSideSpeed5, MathUtils.getRandomNumberInRange(10f, 25f), MathUtils.getRandomNumberInRange(3f, 5f), 0.2f, 0.2f, MathUtils.getRandomNumberInRange(1.5f, 3f), new Color(50, 50, 50, 150));


    }

}
