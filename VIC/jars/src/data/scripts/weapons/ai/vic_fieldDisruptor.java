package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class vic_fieldDisruptor implements MissileAIPlugin, GuidedMissileAI {

    private final MissileAPI missile;
    private CombatEngineAPI engine;
    private CombatEntityAPI target;
    private IntervalUtil timer = new IntervalUtil(0.05f, 0.15f);
    private List<MissileAPI> missiles = new ArrayList<>();
    private float
            fadingTime = 0f,
            timePassed = 0f;

    public vic_fieldDisruptor(MissileAPI missile, ShipAPI launchingShip) {
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        this.missile = missile;
        target = null;
        missile.setCollisionClass(CollisionClass.NONE);
        missile.getSpriteAPI().setAdditiveBlend();
    }

    public static List<MissileAPI> getMissilesWithinAlmostOval(Vector2f location, float facing, float range) {
        List<MissileAPI> missiles = new ArrayList<>();
        Vector2f leftSide = Misc.getUnitVectorAtDegreeAngle(facing + 90);
        Vector2f rightSide = Misc.getUnitVectorAtDegreeAngle(facing - 90);
        Vector2f leftLoc = new Vector2f(location.x + leftSide.x * range, location.y + leftSide.y * range);
        Vector2f rightLoc = new Vector2f(location.x + rightSide.x * range, location.y + rightSide.y * range);
        Vector2f leftLoc2 = new Vector2f(location.x + leftSide.x * range * 2, location.y + leftSide.y * range * 2);
        Vector2f rightLoc2 = new Vector2f(location.x + rightSide.x * range * 2, location.y + rightSide.y * range * 2);

        for (MissileAPI tmp : Global.getCombatEngine().getMissiles()) {
            if (MathUtils.isWithinRange(tmp.getLocation(), location, range) ||
                    MathUtils.isWithinRange(tmp.getLocation(), leftLoc, range) ||
                    MathUtils.isWithinRange(tmp.getLocation(), rightLoc, range) ||
                    MathUtils.isWithinRange(tmp.getLocation(), leftLoc2, range) ||
                    MathUtils.isWithinRange(tmp.getLocation(), rightLoc2, range)) {

                missiles.add(tmp);
            }
        }
        return missiles;
    }

    @Override
    public void advance(float amount) {

        /*
        SpriteAPI trail = Global.getSettings().getSprite("fx", "vic_exsilium_fx_trail");
        Vector2f misDir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing());
        Vector2f trailLoc = new Vector2f(missile.getLocation().x - 30 * misDir.x, missile.getLocation().y - 30 * misDir.y);
        MagicRender.singleframe(trail, trailLoc, new Vector2f(250, 180), missile.getFacing() - 90, new Color(255, 255, 255), false);

        trailLoc = new Vector2f(missile.getLocation().x - 15 * misDir.x - 15 * misDir.x * MathUtils.getRandomNumberInRange(0.5f, 1f), missile.getLocation().y - 15 * misDir.y - 15 * misDir.y * MathUtils.getRandomNumberInRange(0.5f, 1f));
        trail.setColor(new Color(230 + (int) (25 * Math.random()), 202, 43, 255));
        MagicRender.singleframe(trail, trailLoc, new Vector2f(250, 180), missile.getFacing() - 90, new Color(255, 255, 255), false);

         */


        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused() || missile.isFading()) {
            return;
        }

        //if (timePassed <= 0) timePassed = 0.0125f;

        timePassed += amount;
        float power = (float) Math.sqrt(Math.sqrt(timePassed));


        missile.giveCommand(ShipCommand.ACCELERATE);

        float alpha = (0.5f / power);
        if (alpha > 1) alpha = 1f;
        if (missile.isFizzling()) {
            fadingTime += amount;
            alpha = alpha - (fadingTime);
            if (alpha <= 0) {
                engine.removeEntity(missile);
                return;
            }
        }
        missile.getSpriteAPI().setColor(
                new Color(
                        1,
                        1,
                        1,
                        alpha
                ));

        /*
        SpriteAPI trail = missile.getSpriteAPI();
        Vector2f misDir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing());
        Vector2f trailLoc = new Vector2f(missile.getLocation().x, missile.getLocation().y);
        MagicRender.singleframe(trail, trailLoc, new Vector2f(250, 180), missile.getFacing() - 90, new Color(255, 255, 255), false);



         */

        int flip = 1;
        if (Math.random() > 0.5) {
            flip = -1;
        }

        missile.getSpriteAPI().setSize(
                210 * (0.95f + 0.05f * (float) Math.random()) * power
                ,
                150 * (0.95f + 0.05f * (float) Math.random()) * power
        );

        missile.getSpriteAPI().setCenter(
                missile.getSpriteAPI().getWidth() / 2,
                110 * power
        );


        timer.advance(amount);

        missile.setFacing(VectorUtils.getFacing(missile.getVelocity()));

        if (timer.intervalElapsed()) {
            missiles = getMissilesWithinAlmostOval(missile.getLocation(), missile.getFacing(), 70 * power);
        }
        for (MissileAPI m : missiles) {
            if (m == missile) continue;
            if (m.getCollisionClass() != CollisionClass.NONE) {
                float dis = 16.5f * power / MathUtils.getDistance(m.getLocation(), missile.getLocation());
                //engine.addFloatingText(new Vector2f(m.getLocation().x, m.getLocation().y), MathUtils.getDistance(m.getLocation(), missile.getLocation()) + "", 60, Color.WHITE,m, 0.25f, 0.25f);
                if (dis > 1) dis = 1;
                m.getVelocity().scale(1 - 0.95f * (float) Math.pow(dis, 1.5f));

                engine.applyDamage(m, m.getLocation(), 500 * amount * 1 / power, DamageType.FRAGMENTATION, 0f, false, false, missile.getSource());
                m.setJitter(missile, new Color(40, 255, 187), 10 * dis, Math.round(15 * dis), 3);
            }
        }

        if (timer.intervalElapsed()) {
            Vector2f leftSide = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + 90);


            if (MagicRender.screenCheck(0.1f, missile.getLocation())) {
                /*
                engine.addHitParticle(
                        missile.getLocation(),
                        missile.getVelocity(),
                        200 * (0.8f + 0.2f * (float) Math.random()),
                        1,
                        0.1f,
                        new Color(243, 250, 50, 250)
                );

             */
                float range = ((float) Math.random() - 0.5f) * 230f * power;
                Vector2f point = new Vector2f(missile.getLocation().x + leftSide.x * range, missile.getLocation().y + leftSide.y * range);
                engine.addHitParticle(
                        point,
                        new Vector2f(),
                        10,
                        1,
                        1f,
                        new Color(250, 183, 50, 250)
                );

                range = ((float) Math.random() - 0.5f) * 230f * power;
                point = new Vector2f(missile.getLocation().x + leftSide.x * range, missile.getLocation().y + leftSide.y * range);

                engine.addHitParticle(
                        point,
                        new Vector2f(),
                        20,
                        1,
                        0.5f,
                        new Color(250, 153, 50, 250)
                );
            }

            List<ShipAPI> ships = AIUtils.getNearbyEnemies(missile, 50);
            for (ShipAPI s : ships) {
                if (s.getCollisionClass() != CollisionClass.NONE) {
                    engine.spawnEmpArc(missile.getSource(),
                            missile.getLocation(),
                            s,
                            s,
                            DamageType.ENERGY,
                            20,
                            200,
                            1000,
                            null,
                            3,
                            new Color(150, 50, 250, 64),
                            new Color(150, 200, 250, 64)
                    );
                }
            }


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