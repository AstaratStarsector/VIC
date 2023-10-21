package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.vic_combatPlugin;
import data.scripts.util.MagicLensFlare;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class vic_verlioka implements MissileAIPlugin, GuidedMissileAI {

    private final MissileAPI missile;
    private final IntervalUtil timer = new IntervalUtil(0.05f, 0.15f);
    private CombatEngineAPI engine;
    private CombatEntityAPI target;

    private Vector2f firstLoc;

    public vic_verlioka(MissileAPI missile, ShipAPI launchingShip) {
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
        range = range / 2;
        Vector2f leftSide = Misc.getUnitVectorAtDegreeAngle(facing + 90);
        Vector2f rightSide = Misc.getUnitVectorAtDegreeAngle(facing - 90);
        Vector2f leftLoc = new Vector2f(location.x + leftSide.x * range, location.y + leftSide.y * range);
        Vector2f rightLoc = new Vector2f(location.x + rightSide.x * range, location.y + rightSide.y * range);
        Vector2f leftLoc2 = new Vector2f(location.x + leftSide.x * range * 2, location.y + leftSide.y * range * 2);
        Vector2f rightLoc2 = new Vector2f(location.x + rightSide.x * range * 2, location.y + rightSide.y * range * 2);
        range = range * 2;

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

    public static List<ShipAPI> getFightersWithinAlmostOval(Vector2f location, float facing, float range) {
        List<ShipAPI> fighters = new ArrayList<>();
        range = range / 2;
        Vector2f leftSide = Misc.getUnitVectorAtDegreeAngle(facing + 90);
        Vector2f rightSide = Misc.getUnitVectorAtDegreeAngle(facing - 90);
        Vector2f leftLoc = new Vector2f(location.x + leftSide.x * range, location.y + leftSide.y * range);
        Vector2f rightLoc = new Vector2f(location.x + rightSide.x * range, location.y + rightSide.y * range);
        Vector2f leftLoc2 = new Vector2f(location.x + leftSide.x * range * 2, location.y + leftSide.y * range * 2);
        Vector2f rightLoc2 = new Vector2f(location.x + rightSide.x * range * 2, location.y + rightSide.y * range * 2);
        range = range * 2;

        for (ShipAPI tmp : Global.getCombatEngine().getShips()) {
            if (tmp.getHullSize() != ShipAPI.HullSize.FIGHTER) continue;
            if (MathUtils.isWithinRange(tmp.getLocation(), location, range) ||
                    MathUtils.isWithinRange(tmp.getLocation(), leftLoc, range) ||
                    MathUtils.isWithinRange(tmp.getLocation(), rightLoc, range) ||
                    MathUtils.isWithinRange(tmp.getLocation(), leftLoc2, range) ||
                    MathUtils.isWithinRange(tmp.getLocation(), rightLoc2, range)) {

                fighters.add(tmp);
            }
        }
        return fighters;
    }

    private void coolFlare(Vector2f loc, float range) {
        MagicLensFlare.createSharpFlare(
                engine,
                missile.getSource(),
                MathUtils.getRandomPointInCircle(
                        loc,
                        range * 0.5f
                ),
                MathUtils.getRandomNumberInRange(2, 4),
                MathUtils.getRandomNumberInRange(70, 130),
                missile.getFacing() - 80 - MathUtils.getRandomNumberInRange(-10, 10),
                new Color(255, MathUtils.getRandomNumberInRange(180, 220), 50, 200),
                new Color(200, 200, 255, 200)
        );

    }

    @Override
    public void advance(float amount) {

        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused()) {
            return;
        }

        Global.getSoundPlayer().playLoop("vic_verlioka_loop", missile, 1, 1, missile.getLocation(), missile.getVelocity());

        if (firstLoc == null) {
            firstLoc = new Vector2f(missile.getLocation());
        }
        //init vars
        float flyDist = missile.getFlightTime() * missile.getMaxSpeed();
        float disLEft = 1 - missile.getFlightTime() / missile.getMaxFlightTime();

        float facing = missile.getFacing();
        float size = flyDist * 0.53f;
        float range = size / 3f;
        Vector2f location = missile.getLocation();

        //missile.giveCommand(ShipCommand.ACCELERATE);

        //flares on expire
        if (missile.isFizzling()) {
            if (disLEft <= 0) {
                engine.removeEntity(missile);
                Vector2f leftSide = Misc.getUnitVectorAtDegreeAngle(facing + 90);
                Vector2f rightSide = Misc.getUnitVectorAtDegreeAngle(facing - 90);
                Vector2f leftLoc = new Vector2f(location.x + leftSide.x * range, location.y + leftSide.y * range);
                Vector2f rightLoc = new Vector2f(location.x + rightSide.x * range, location.y + rightSide.y * range);
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 3); i++) {
                    coolFlare(missile.getLocation(), range);
                }
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 3); i++) {
                    coolFlare(leftLoc, range);
                }
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 3); i++) {
                    coolFlare(rightLoc, range);
                }
                return;
            }
        }


        missile.getSpriteAPI().setColor(new Color(255, 255, 255, 200));
        missile.setJitter(missile, new Color(255, 255, 255, 200), 3 * ((1 - disLEft) * 3), 10, 1.5f * ((1 - disLEft) * 2.5f));


        int flipTrail = 1;
        if (Math.random() > 0.5) {
            flipTrail = -1;
        }

        float randomTrail = MathUtils.getRandomNumberInRange(-size * 0.05f, size * 0.05f);
        float randomDeg = MathUtils.getRandomNumberInRange(0, 90);
        SpriteAPI trail = Global.getSettings().getSprite("fx", "vic_virlioka_trail");
        Vector2f dir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + randomDeg);
        Vector2f trailLoc = new Vector2f(missile.getLocation().x - (dir.x * randomTrail), missile.getLocation().y - (dir.y * randomTrail));
        Vector2f trailSize = new Vector2f(flipTrail * size, size / 2f);
        trail.setCenter((flipTrail * size) / 2, size * 0.85f / 2f);
        MagicRender.singleframe(trail, trailLoc, trailSize, missile.getFacing() - 90, new Color(255, 255, 255, 40), false);

        randomTrail = MathUtils.getRandomNumberInRange(-size * 0.05f, size * 0.05f);
        randomDeg = MathUtils.getRandomNumberInRange(0, 90);
        dir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + randomDeg);
        trailLoc = new Vector2f(missile.getLocation().x - (dir.x * randomTrail), missile.getLocation().y - (dir.y * randomTrail));
        trailSize = new Vector2f(flipTrail * size, size / 2f);
        trail.setCenter((flipTrail * size) / 2, size * 0.85f / 2f);
        MagicRender.singleframe(trail, trailLoc, trailSize, missile.getFacing() - 90, new Color(255, 255, 255, 40), false);

        randomTrail = MathUtils.getRandomNumberInRange(-size * 0.05f, size * 0.05f);
        randomDeg = MathUtils.getRandomNumberInRange(0, 90);
        dir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + randomDeg);
        trailLoc = new Vector2f(missile.getLocation().x - (dir.x * randomTrail), missile.getLocation().y - (dir.y * randomTrail));
        trailSize = new Vector2f(flipTrail * size, size / 2f);
        trail.setCenter((flipTrail * size) / 2, size * 0.85f / 2f);
        MagicRender.singleframe(trail, trailLoc, trailSize, missile.getFacing() - 90, new Color(255, 255, 255, 40), false);


        int flip = 1;
        if (Math.random() > 0.5) {
            flip = -1;
        }

        missile.getSpriteAPI().setSize(
                flip * size,
                range / 2
        );

        missile.getSpriteAPI().setCenter(
                missile.getSpriteAPI().getWidth() / 2,
                missile.getSpriteAPI().getHeight() / 2
        );

        timer.advance(amount);

        missile.setFacing(VectorUtils.getFacing(missile.getVelocity()));

        if (timer.intervalElapsed()) {
            List<MissileAPI> missiles = getMissilesWithinAlmostOval(missile.getLocation(), missile.getFacing(), range);
            for (MissileAPI m : missiles) {
                if (m == missile) continue;
                if (m.getOwner() == missile.getOwner()) continue;
                if (m.getCollisionClass() != CollisionClass.NONE) {
                    vic_combatPlugin.addToListM(m);
                }
            }
            List<ShipAPI> fighters = getFightersWithinAlmostOval(missile.getLocation(), missile.getFacing(), range);
            for (ShipAPI f : fighters) {
                if (f.getOwner() == missile.getOwner()) continue;
                if (f.getCollisionClass() != CollisionClass.NONE) {
                    vic_combatPlugin.addToListF(f);
                }
            }
        }

        if (timer.intervalElapsed()) {

            Vector2f leftSide = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + 90);

            if (MagicRender.screenCheck(0.1f, missile.getLocation())) {

                range = ((float) Math.random() - 0.5f) * size;
                Vector2f point = new Vector2f(missile.getLocation().x + leftSide.x * range, missile.getLocation().y + leftSide.y * range);
                engine.addHitParticle(
                        point,
                        new Vector2f(),
                        MathUtils.getRandomNumberInRange(7, 13),
                        MathUtils.getRandomNumberInRange(0.8f, 1),
                        MathUtils.getRandomNumberInRange(0.8f, 1.2f),
                        new Color(250, MathUtils.getRandomNumberInRange(150, 210), 50, 250)
                );

                range = ((float) Math.random() - 0.5f) * size;
                point = new Vector2f(missile.getLocation().x + leftSide.x * range, missile.getLocation().y + leftSide.y * range);

                engine.addHitParticle(
                        point,
                        new Vector2f(),
                        MathUtils.getRandomNumberInRange(15, 25),
                        MathUtils.getRandomNumberInRange(0.8f, 1),
                        MathUtils.getRandomNumberInRange(0.4f, 0.6f),
                        new Color(250, MathUtils.getRandomNumberInRange(100, 200), 50, 250)
                );
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
}