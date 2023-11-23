package data.scripts.weapons.decos;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.EngineSlot;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class vic_decoEnginesController implements EveryFrameWeaponEffectPlugin {

    boolean doOnce = true;
    ArrayList<decoEngine> engines = new ArrayList<>();

    final Map<ShipAPI.HullSize, Float> strafeMulti = new HashMap<>();

    {
        strafeMulti.put(ShipAPI.HullSize.FIGHTER, 1f);
        strafeMulti.put(ShipAPI.HullSize.FRIGATE, 1f);
        strafeMulti.put(ShipAPI.HullSize.DESTROYER, 0.75f);
        strafeMulti.put(ShipAPI.HullSize.CRUISER, 0.5f);
        strafeMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.25f);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon.getShip().getOwner() == -1) return;
        ShipAPI ship = weapon.getShip();
        if (doOnce) {
            for (ShipEngineControllerAPI.ShipEngineAPI e : ship.getEngineController().getShipEngines()) {
                if (e.getStyleId().startsWith("Vectored")) {
                    engines.add(new decoEngine(ship, e));
                }
            }
            doOnce = false;
        }

        Vector2f newVector = new Vector2f();
        if (ship.getEngineController().isAccelerating()) {
            newVector.y += 1 * ship.getAcceleration();
        }
        if (ship.getEngineController().isAcceleratingBackwards()) {
            newVector.y -= 1 * ship.getDeceleration();
        }
        if (ship.getEngineController().isStrafingLeft()) {
            newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isStrafingRight()) {
            newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isDecelerating()) {
            if (ship.getVelocity().lengthSquared() > 0) {
                Vector2f normalizedVel = new Vector2f(ship.getVelocity());
                normalizedVel = Misc.normalise(normalizedVel);
                normalizedVel = VectorUtils.rotate(normalizedVel, -ship.getFacing() - 90);
                Vector2f.add(newVector, normalizedVel, newVector);
            }
        }
        newVector.scale(-1);
        float currAngle = Misc.getAngleInDegrees(newVector);

        int turn = 0;
        if (ship.getEngineController().isTurningRight()) {
            turn++;
        }
        if (ship.getEngineController().isTurningLeft()) {
            turn--;
        }

            /*
            int Red = Math.min(255, Math.round(engineColor.getRed() * (1f - ratio) + shift.getRed() * ratio));
            int Green = Math.min(255, Math.round(engineColor.getGreen() * (1f - ratio) + shift.getGreen() * ratio));
            int Blue = Math.min(255, Math.round(engineColor.getBlue() * (1f - ratio) + shift.getBlue() * ratio));

             */
        //engine.addHitParticle(weapon.getLocation(), (Vector2f) Misc.getUnitVectorAtDegreeAngle(currAngle + ship.getFacing() - 90).scale(300f), 20, 1, amount * 10, Color.red);
        for (decoEngine e : engines) {
            float thrust = 0;

            if (!VectorUtils.isZeroVector(newVector) && Math.abs(MathUtils.getShortestRotation(e.angle, currAngle)) <= 60) {
                thrust += 1;

                //engine.addHitParticle(e.weapon.getLocation(), (Vector2f) Misc.getUnitVectorAtDegreeAngle(e.angle + ship.getFacing() - 90).scale(300f), 20, 1, amount * 10, colorToUse);
            }
            if (turn != 0 && e.turn == turn) {
                thrust += 1f;
            }
            if (turn != 0 && e.turn != turn){
                thrust -= 0.08f;
            }


            thrust(e, MathUtils.clamp(thrust, 0.4f, 1f));
        }
    }


    private void thrust(decoEngine data, float thrust) {
        ShipAPI ship = data.ship;
        Vector2f size = new Vector2f(15, 80);
        float smooth = 0.2f;
        if (data.engine.isDisabled()) thrust = 0f;
        //Global.getLogger(ptes_decoEnginesController.class).info(weapon.getSlot().getId());


        //target angle
        float length = thrust;

        float amount = Global.getCombatEngine().getElapsedInLastFrame();

        //thrust is reduced while the engine isn't facing the target angle, then smoothed
        /*
        length -= data.previousThrust;
        length *= smooth;
        length += data.previousThrust;
        data.previousThrust = length;*/
        EngineSlot engineslot = (EngineSlot) data.engine.getEngineSlot();

        if (data.previousThrust < thrust) {
            data.previousThrust += engineslot.getAccelTimeToMaxGlow() * amount;
            data.previousThrust = Math.min(thrust, data.previousThrust);
        } else if (data.previousThrust > thrust) {
            data.previousThrust -= engineslot.getAccelTimeToMaxGlow() * amount;
            data.previousThrust = Math.max(thrust, data.previousThrust);
        }

        data.ship.getEngineController().setFlameLevel(data.engine.getEngineSlot(), data.previousThrust);
        //data.ship.getEngineController().forceShowAccelerating();
    }

    static class decoEngine {
        public decoEngine(ShipAPI ship, ShipEngineControllerAPI.ShipEngineAPI engine) {
            this.ship = ship;
            this.engine = engine;
            angle = engine.getEngineSlot().getAngle() + 90;
            Vector2f loc = engine.getEngineSlot().computePosition(new Vector2f(), 0);
            float leverRatio = loc.length() / ship.getCollisionRadius();
            float absAngle = engine.getEngineSlot().getAngle();
            VectorUtils.getAngle(new Vector2f(1, 0), new Vector2f(0, 1));
            Vector2f pushDirection = Misc.getUnitVectorAtDegreeAngle(absAngle);
            float displacementAngle = MathUtils.getShortestRotation(Misc.getAngleInDegrees(loc), Misc.getAngleInDegrees(pushDirection));

            float tolerance = (float) (75f * Math.pow(leverRatio, 1.3f));

            //Global.getLogger(vic_decoEnginesController.class).info(tolerance + "/" + Math.abs(displacementAngle - 90) + "//" +  Math.abs(Math.abs(displacementAngle) - 90) + "/" + leverRatio + "/" +  displacementAngle);

            if (leverRatio > 0.25) {
                if (displacementAngle > 0) {
                    if (Math.abs(displacementAngle - 90) < tolerance) {
                        turn = 1;
                    }
                } else {
                    if (Math.abs(Math.abs(displacementAngle) - 90) < tolerance) {
                        turn = -1;
                    }
                }
            }

            EngineSlot engineslot = (EngineSlot) engine.getEngineSlot();

            engineslot.setGlowParams(engineslot.getWidth(), engineslot.getLength(), 10f, 1);

            sizeMulti = engine.getEngineSlot().getWidth() / 3;
        }

        ShipAPI ship;
        ShipEngineControllerAPI.ShipEngineAPI engine;
        int turn;
        float angle;
        float previousThrust = 0.4f;
        float sizeMulti;
    }
}
