package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class vic_thermalLance_beam implements BeamEffectPlugin {

    private final IntervalUtil flashInterval = new IntervalUtil(0.1f, 0.2f);

    //dont touch
    private boolean runOnce = true;
    private boolean runOnce2 = true;
    private float
            baseDMG = 0,
            chargeUpDMGMult = 0.1f,
            burstDMGMult = 1,
            lastFrameCharge = 0;


    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

        //Global.getLogger(vic_thermalLance_beam.class).debug(beam.getWeapon().getSpec().getBurstDuration());

        if (runOnce) {
            baseDMG = beam.getWeapon().getDamage().getBaseDamage();
            WeaponSpecAPI weaponSpec = beam.getWeapon().getSpec();
            float charge = (weaponSpec.getBeamChargedownTime() + weaponSpec.getBeamChargeupTime()) * 1 / 3;
            burstDMGMult = ((charge + weaponSpec.getBurstDuration()) - (charge * chargeUpDMGMult)) / weaponSpec.getBurstDuration();
            runOnce = false;
        }

        float charge = beam.getWeapon().getChargeLevel();


        beam.getDamage().setDamage(baseDMG * chargeUpDMGMult);

        if (charge < 0.90f) {
            beam.setWidth(15);
        } else {
            beam.setWidth(45 * (float) Math.pow(beam.getWeapon().getChargeLevel(), 4f));
        }

        if (charge == 1)
            beam.getDamage().setDamage(baseDMG * burstDMGMult);

        if (lastFrameCharge > charge && runOnce2) {
            engine.spawnProjectile(beam.getWeapon().getShip(),
                    beam.getWeapon(),
                    "vic_thermalLance_trail",
                    beam.getFrom(),
                    beam.getWeapon().getCurrAngle(),
                    null);
            //Global.getLogger(vic_thermalLance_beam.class).info("kek");
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

