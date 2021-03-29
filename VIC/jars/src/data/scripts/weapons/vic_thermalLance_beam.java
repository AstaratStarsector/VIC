package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.util.MagicLensFlare;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.awt.*;

public class vic_thermalLance_beam implements BeamEffectPlugin {

    private IntervalUtil flashInterval = new IntervalUtil(0.1f,0.2f);

    private float timer = 0;


    //dont touch
    private boolean runOnce = false;
    private boolean hidden = false;
    private AnimationAPI theAnim;
    private int maxFrame;
    private int frame;
    private float
            firingTime = 0f,
            heat = 0f,
            currentScore = 0f;


    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (beam.getWeapon().getChargeLevel() < 0.9f){
            beam.setWidth(15);
        } else {
            beam.setWidth(45 * (float) Math.pow( beam.getWeapon().getChargeLevel(), 4f));
        }

        flashInterval.advance(engine.getElapsedInLastFrame());
        if (flashInterval.intervalElapsed()) {
            float size = beam.getWidth() * MathUtils.getRandomNumberInRange(2f, 2.2f);
            float dur = MathUtils.getRandomNumberInRange(0.2f,0.25f);
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), beam.getWidth(), 0.8f, dur, Color.red);
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), size, 0.8f, dur, Color.orange);
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), beam.getWidth(), 0.8f, dur, Color.white);
        }






        }

    }

