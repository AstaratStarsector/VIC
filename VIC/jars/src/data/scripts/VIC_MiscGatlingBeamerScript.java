package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import data.scripts.util.MagicFakeBeam;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tries to simulate a gatling-beam effect, by spawning fake trails on the invisible beam
 * <p>
 * Supports multi-barrel weapons, runs once per beam. They will be fired in sync, though
 * REQUIRES that the hardpoint and turret version of the gun has an equal number of barrels
 *
 * @author Nicke535
 */
public class VIC_MiscGatlingBeamerScript implements EveryFrameWeaponEffectPlugin {
    //The maximum dispersal the beam can have, in either direction (so 15f makes a 30-degree cone)
    private static final float MAX_DISPERSAL_ANGLE = 3f;

    //A mathematical constant for how "grouped in the middle" dispersal is.
    //   At 2f, 50% of the shots will hit within the centermost 25% of the possible spread, and it only gets more
    //   grouped with higher values. 1f means equal spread in the entire cone
    //   Values below 1f means it is grouped at the edges instead of the center
    private static final float DISPERSAL_GROUPING_FACTOR = 2f;

    //The center and fringe color of the "fake beam" that is spawned
    private static final Color CENTER_COLOR = new Color(1f, 0.5f, 0f, 1f);
    private static final Color FRINGE_COLOR = new Color(1f, 0.5f, 0f, 1f);

    //Size of the impact flash spawned by the fake beam
    //      Supports randomization: will pick a random number between the MAX and the MIN
    private static final float IMPACT_SIZE_MIN = 18f;
    private static final float IMPACT_SIZE_MAX = 22f;

    //How long the beam stays at full brightness and how long it takes to fade out, respectively
    //      Supports randomization: will pick a random number between the MAX and the MIN
    //      Not really necessary, but hey, might add a slightly more "organic" or "haphazard" feel, what do i know
    private static final float FULL_BRIGHTNESS_TIME_MIN = 0.7f;
    private static final float FULL_BRIGHTNESS_TIME_MAX = 0.7f;
    private static final float FADE_BRIGHTNESS_TIME_MIN = 1.3f;
    private static final float FADE_BRIGHTNESS_TIME_MAX = 1.3f;


    //In-script variables: don't touch!
    private boolean runOnceOn = false;
    private boolean runOnceOff = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Ensure the spec is cloned: important for our stuff to work
        weapon.ensureClonedSpec();

        //Check if we're firing the weapon, and reset variables if not
        if (!weapon.isFiring()) {
            runOnceOn = false;

            //First frame we're not firing, make sure we randomize angles for the beams
            if (!runOnceOff) {
                runOnceOff = true;

                for (int i = 0; i < weapon.getSpec().getTurretAngleOffsets().size(); i++) {
                    //Calculates one dispersal angle
                    float dispersal = (float) Math.pow(MathUtils.getRandomNumberInRange(0f, 1f), DISPERSAL_GROUPING_FACTOR) * MAX_DISPERSAL_ANGLE;
                    if (Math.random() < 0.5f) {
                        dispersal *= -1f;
                    }

                    //And applies it
                    weapon.getSpec().getTurretAngleOffsets().set(0, dispersal);
                    weapon.getSpec().getHardpointAngleOffsets().set(0, dispersal);
                    weapon.getSpec().getHiddenAngleOffsets().set(0, dispersal);
                }
            }

            return;
        }
        runOnceOff = false;

        //Get all the beams that belong to this gun and put them in a list
        List<BeamAPI> beams = new ArrayList<>();
        for (BeamAPI beam : engine.getBeams()) {
            if (beam.getWeapon() == weapon) {
                beams.add(beam);
            }
        }

        //If we didn't find any beams, return
        if (beams.isEmpty()) {
            return;
        }

        //And now for the visuals: if this is the first time we've fired since we last stopped firing, get all the
        //beams and spawn a fake beam on each of them
        if (!runOnceOn) {
            runOnceOn = true;
            for (BeamAPI beam : beams) {
                applyFakeBeam(beam);
            }
        }
    }

    //Applies the "fake beam" to the real beam
    //Attempt 1: using MagicFakeBeams. Might actually do the trick...
    private void applyFakeBeam(BeamAPI beam) {
        //Calculates angles and distance
        float angle = VectorUtils.getAngle(beam.getFrom(), beam.getTo());
        float distance = MathUtils.getDistance(beam.getFrom(), beam.getTo());

        //And spawns a fake beam!
        MagicFakeBeam.spawnFakeBeam(Global.getCombatEngine(), new Vector2f(beam.getFrom()), distance, angle,
                beam.getWidth(), MathUtils.getRandomNumberInRange(FULL_BRIGHTNESS_TIME_MIN, FULL_BRIGHTNESS_TIME_MAX),
                MathUtils.getRandomNumberInRange(FADE_BRIGHTNESS_TIME_MIN, FADE_BRIGHTNESS_TIME_MAX),
                MathUtils.getRandomNumberInRange(IMPACT_SIZE_MIN, IMPACT_SIZE_MAX), CENTER_COLOR, FRINGE_COLOR,
                0f, DamageType.ENERGY, 0f, beam.getSource());
    }
}
