package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.utilities.vic_trailSpawner;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;

import static data.scripts.utilities.vic_trailSpawner.VIC_TRAILS_LIST;
import static data.scripts.utilities.vic_trailSpawner.createTrailSegment;

public class vic_thermalLance_beam implements BeamEffectPlugin {

    final IntervalUtil flashInterval = new IntervalUtil(0.1f, 0.2f);

    //dont touch
    boolean runOnce = true;
    boolean runOnce2 = true;
    float
            baseDMG = 0,
            burstDMGMult = 1,
            lastFrameCharge = 0;

    final float
            chargeUpDMGMult = 0.1f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {


        if (runOnce) {
            baseDMG = beam.getWeapon().getDamage().getBaseDamage();
            WeaponSpecAPI weaponSpec = beam.getWeapon().getSpec();
            float charge = (weaponSpec.getBeamChargedownTime() + weaponSpec.getBeamChargeupTime()) * 1 / 3;
            burstDMGMult = ((charge + weaponSpec.getBurstDuration()) - (charge * chargeUpDMGMult)) / weaponSpec.getBurstDuration();
            //Global.getLogger(vic_thermalLance_beam.class).info(baseDMG * burstDMGMult);
            runOnce = false;
        }

        float charge = beam.getWeapon().getChargeLevel();

        beam.getDamage().setDamage(baseDMG * chargeUpDMGMult);

        if (charge < 0.90f) {
            beam.setWidth(15);
        } else {
            beam.setWidth(45 * (float) Math.pow(beam.getWeapon().getChargeLevel(), 4f));
        }

        if (charge == 1) {
            beam.getDamage().setDamage(baseDMG * burstDMGMult);
        }


        if (lastFrameCharge > charge && runOnce2) {
            /*
            engine.spawnProjectile(beam.getWeapon().getShip(),
                    beam.getWeapon(),
                    "vic_thermalLance_trail",
                    beam.getFrom(),
                    beam.getWeapon().getCurrAngle(),
                    null);

             */
            //Global.getLogger(vic_thermalLance_beam.class).info("kek");
            //MagicTrailPlugin.AddTrailMemberAdvanced();
            Vector2f startLoc = beam.getFrom();
            Vector2f endLoc = beam.getTo();
            Vector2f beamDirection = VectorUtils.getDirectionalVector(startLoc, endLoc);
            float angle = Misc.getAngleInDegrees(beamDirection);
            float beamLength = MathUtils.getDistance(startLoc, endLoc);
            Global.getLogger(vic_thermalLance_beam.class).info(angle + "/" + beamDirection);
            for (vic_trailSpawner.vic_trailData trailData : VIC_TRAILS_LIST.get("vic_thermalLance_trail_proj")) {
                float trailID = MagicTrailPlugin.getUniqueID();
                for (float i = 0; beamLength >= i; i += 150) {
                    Vector2f addVector = (Vector2f) new Vector2f(beamDirection).scale(i);
                    Vector2f segmentLoc = Vector2f.add(startLoc, addVector, null);
                    createTrailSegment(trailData, segmentLoc, trailID, new Vector2f(), new Vector2f(), null, angle);
                }
                /*
                MagicTrailPlugin.addTrailMemberSimple(
                        beam.getSource(),
                        trailID,
                        Global.getSettings().getSprite("fx", "trails_trail_smooth"),
                        segmentLoc,
                        0f,
                        angle,
                        50f,
                        5f,
                        new Color(255, 50, 50, 255),
                        1f,
                        0f,
                        4f,
                        4f,
                        true
                );
                MagicTrailPlugin.addTrailMemberAdvanced(beam.getSource(),trailID,Global.getSettings().getSprite("fx", "trails_trail_smooth"),
                        segmentLoc,0,0,angle,0,50,5,100,new Color(235,70,5,204),new Color(235,135,5,204),0.8f,0,2.5f,5,);


                 */

            }
            runOnce2 = false;
        }
        lastFrameCharge = charge;


        flashInterval.advance(engine.getElapsedInLastFrame());
        if (flashInterval.intervalElapsed() && beam.didDamageThisFrame()) {
            float size = beam.getWidth() * MathUtils.getRandomNumberInRange(2f, 2.2f);
            float dur = MathUtils.getRandomNumberInRange(0.2f, 0.25f);
            engine.addHitParticle(beam.getTo(), beam.getSource().getVelocity(), beam.getWidth(), 0.8f, dur, Color.red);
            engine.addHitParticle(beam.getTo(), beam.getSource().getVelocity(), size, 0.8f, dur, Color.orange);
            engine.addHitParticle(beam.getTo(), beam.getSource().getVelocity(), beam.getWidth(), 0.8f, dur, Color.white);
        }
    }

}

