package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.plugins.vic_combatPlugin;
import org.lwjgl.util.vector.Vector2f;

public class vic_zlydzen_beam implements BeamEffectPlugin {

    private final IntervalUtil flashInterval = new IntervalUtil(0.1f, 0.2f);

    //don't touch
    private boolean runOnce = true;

    private IntervalUtil fireInterval = new IntervalUtil(0.25f, 1.75f);
    private boolean wasZero = true;


    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

        //Global.getLogger(vic_thermalLance_beam.class).debug(beam.getWeapon().getSpec().getBurstDuration());

        if (runOnce) {
            runOnce = false;
        }

        float charge = beam.getWeapon().getChargeLevel();

        CombatEntityAPI target = beam.getDamageTarget();
        if (target instanceof ShipAPI) {
            boolean shieldHit = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
            vic_combatPlugin.markTargetDamagedByZlydzen((ShipAPI) target, amount * charge, shieldHit);
        }


        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            float dur = beam.getDamage().getDpsDuration();
            // needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
            if (!wasZero) dur = 0;
            wasZero = beam.getDamage().getDpsDuration() <= 0;
            fireInterval.advance(dur);

            if (fireInterval.intervalElapsed()) {
                ShipAPI ship = (ShipAPI) target;
                boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.1f;
                pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                boolean piercedShield = hitShield && (float) Math.random() < pierceChance;
                //piercedShield = true;

                if (!hitShield || piercedShield) {
                    Vector2f point = beam.getRayEndPrevFrame();
                    float emp = beam.getDamage().getFluxComponent();
                    float dam = beam.getDamage().getDamage();
                    engine.spawnEmpArcPierceShields(
                            beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
                            DamageType.ENERGY,
                            dam, // damage
                            emp, // emp
                            100000f, // max range
                            "tachyon_lance_emp_impact",
                            10,
                            beam.getFringeColor(),
                            beam.getCoreColor()
                    );
                }
            }
        }

    }

}

