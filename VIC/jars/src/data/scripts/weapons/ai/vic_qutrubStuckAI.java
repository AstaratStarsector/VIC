//By Tartiflette.
package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import data.scripts.weapons.vic_qutrubScript;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class vic_qutrubStuckAI implements MissileAIPlugin, GuidedMissileAI {

    private final MissileAPI missile;
    private final IntervalUtil timer = new IntervalUtil(0.1f, 0.2f);
    private final IntervalUtil accelerationCheck = new IntervalUtil(0.1f, 0.1f);
    private final float TEAR_OFF_THRESHOLD = 35f;
    private final float elapsed = 0f;
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private CombatEntityAPI target;
    private CombatEntityAPI anchor;
    private Vector2f offset = new Vector2f();
    private float angle = 0;
    private boolean runOnce = false;
    private Vector2f projected = new Vector2f();
    private Vector2f previousLoc = new Vector2f();
    private boolean tearOff = false;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////

    public vic_qutrubStuckAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        this.ship = launchingShip;
    }

    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////

    @Override
    public void advance(float amount) {

        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused()) {
            return;
        }

        //visual effect
        Vector2f loc = new Vector2f(offset);
        if (MagicRender.screenCheck(0.25f, loc)) {
            missile.setJitter(missile,new Color (255, 102,0,255),5 * Math.min(missile.getElapsed(), 0.5f) * 2,5,3);
        }

        if (!runOnce) {
            runOnce = true;
            List<CombatEntityAPI> list = ((vic_qutrubScript) missile.getWeapon().getEffectPlugin()).getHITS();

            if (list.isEmpty()) {
                missile.flameOut();
                return;
            }

            //get the anchor
            float range = 1000000;
            for (CombatEntityAPI e : list) {
                if (MathUtils.getDistanceSquared(missile, e) < range) {
                    target = e;
                    anchor = e; //some scripts change the target so I can't really use that for the anchor
                }
            }

            if (anchor == null) {
                return;
            }

            //put the anchor in the weapon's detonation list
            ((vic_qutrubScript) missile.getWeapon().getEffectPlugin()).setDetonation(anchor);

            projected = new Vector2f(anchor.getVelocity());
            projected.scale(0.1f);
            Vector2f.add(missile.getLocation(), projected, projected);
            previousLoc = new Vector2f(missile.getLocation());

            offset = new Vector2f(missile.getLocation());
            Vector2f.sub(offset, new Vector2f(anchor.getLocation()), offset);
            VectorUtils.rotate(offset, -anchor.getFacing(), offset);

            angle = MathUtils.getShortestRotation(anchor.getFacing(), missile.getFacing());
            return;
        } else {
            if (anchor == null || ((vic_qutrubScript) missile.getWeapon().getEffectPlugin()).getDetonation(anchor)) {
                missile.setCollisionClass(CollisionClass.MISSILE_FF);
                return;
            }
        }


        //acceleration check for tear off
        accelerationCheck.advance(amount);
        if (accelerationCheck.intervalElapsed()) {
            //acceleration tear off


            //debug
//            float dist = MathUtils.getDistance(missile.getLocation(),projected);
//            engine.addHitParticle(projected, new Vector2f(), 20, 2, 0.2f, Color.GREEN);
//            engine.addFloatingText(missile.getLocation(), ""+(float)(Math.round(dist*10000))/10000,12, Color.green, missile,0.1f,0.1f);

            boolean fooled = (target != anchor);
            boolean escaped = (anchor.getCollisionClass() == CollisionClass.NONE);
            //boolean outlasted = missile.isFading();

            if (fooled ||
                    escaped
            ) {
                tearOff = true;
                missile.setArmingTime(missile.getElapsed() + 0.25f);
                missile.setCollisionClass(CollisionClass.MISSILE_FF);
                return;
            }

            Vector2f.sub(new Vector2f(missile.getLocation()), previousLoc, projected);
            Vector2f.add(new Vector2f(missile.getLocation()), projected, projected);
            previousLoc = new Vector2f(missile.getLocation());
        }

        //stuck effect

        VectorUtils.rotate(offset, anchor.getFacing(), loc);
        Vector2f.add(loc, anchor.getLocation(), loc);
        missile.getLocation().set(loc);
        missile.setFacing(anchor.getFacing() + angle);

        //detonation
        if (missile.getElapsed() > 0.5f) {
            missile.setCollisionClass(CollisionClass.MISSILE_FF);

            //if detonation fails
            if (missile.getElapsed() > 0.6f) engine.removeEntity(missile);
        }



    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }

    public void init(CombatEngineAPI engine) {
    }
}