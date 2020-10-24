package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.vic_combatPlugin;
import data.scripts.util.MagicLensFlare;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class vic_fieldDisruptor implements MissileAIPlugin, GuidedMissileAI {

    private final MissileAPI missile;
    private CombatEngineAPI engine;
    private CombatEntityAPI target;
    private IntervalUtil timer = new IntervalUtil(0.05f, 0.15f);
    private IntervalUtil timerTrail = new IntervalUtil(0.25f, 0.25f);
    private List<MissileAPI> missiles = new ArrayList<>();
    private List<ShipAPI> fighters = new ArrayList<>();

    private float
            fadingTime = 0f,
            timePassed = 0f;
    private Vector2f firstLoc;

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
        if (firstLoc == null) {
            firstLoc = new Vector2f(missile.getLocation());
        }
        //init vars
        float flyDist = missile.getFlightTime() * missile.getMaxSpeed();
        float disLEft = 1 - missile.getFlightTime() / missile.getMaxFlightTime();
        float power = (float) Math.sqrt(Math.sqrt(timePassed));

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
                    MagicLensFlare.createSharpFlare(
                            engine,
                            missile.getSource(),
                            MathUtils.getRandomPointInCircle(
                                    missile.getLocation(),
                                    range * 0.5f
                            ),
                            3,
                            100,
                            missile.getFacing() - 80 - MathUtils.getRandomNumberInRange(-10, 10),
                            new Color(255, 228, 50),
                            new Color(200, 200, 255)
                    );
                }
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 3); i++) {
                    MagicLensFlare.createSharpFlare(
                            engine,
                            missile.getSource(),
                            MathUtils.getRandomPointInCircle(
                                    leftLoc,
                                    range * 0.5f
                            ),
                            3,
                            100,
                            missile.getFacing() - 80 - MathUtils.getRandomNumberInRange(-10, 10),
                            new Color(255, 228, 50),
                            new Color(200, 200, 255)
                    );
                }
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 3); i++) {
                    MagicLensFlare.createSharpFlare(
                            engine,
                            missile.getSource(),
                            MathUtils.getRandomPointInCircle(
                                    rightLoc,
                                    range * 0.5f
                            ),
                            3,
                            100,
                            missile.getFacing() - 80 - MathUtils.getRandomNumberInRange(-10, 10),
                            new Color(255, 228, 50),
                            new Color(200, 200, 255)
                    );
                }
                return;
            }
        }


        int alpha = MathUtils.clamp(Math.round((255 * disLEft) / 4), 0, 255);
        missile.getSpriteAPI().setColor(new Color(255, 255, 255, 200));
        missile.setJitter(missile, new Color(255, 255, 255, 200), 3 * ((1 - disLEft) * 3), 10, 1.5f * ((1 - disLEft) * 2.5f));
        Global.getCombatEngine().maintainStatusForPlayerShip("vic_fieldDisruptor", "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "Alpha", missile.getMaxFlightTime() + "/" + alpha + "", false);


        int flipTrail = 1;
        if (Math.random() > 0.5) {
            flipTrail = -1;
        }

        //MagicAutoTrails.addProjTrail("vic_fieldDisruptor_shot", new MagicAutoTrails.trailData("trails_trail_contrail", 10,0,0.2f,0.5f,40,20,new Color(255, 255, 255, 200),new Color(255, 255, 255, 200),1,0,200,-2000,0,1,true,false,0,0,0,0,0,0,0,false,CombatEngineLayers.CONTRAILS_LAYER));

        //MagicAutoTrails.getTrailData();

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
        trail = Global.getSettings().getSprite("fx", "vic_virlioka_trail");
        dir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + randomDeg);
        trailLoc = new Vector2f(missile.getLocation().x - (dir.x * randomTrail), missile.getLocation().y - (dir.y * randomTrail));
        trailSize = new Vector2f(flipTrail * size, size / 2f);
        trail.setCenter((flipTrail * size) / 2, size * 0.85f / 2f);
        MagicRender.singleframe(trail, trailLoc, trailSize, missile.getFacing() - 90, new Color(255, 255, 255, 40), false);

        randomTrail = MathUtils.getRandomNumberInRange(-size * 0.05f, size * 0.05f);
        randomDeg = MathUtils.getRandomNumberInRange(0, 90);
        trail = Global.getSettings().getSprite("fx", "vic_virlioka_trail");
        dir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + randomDeg);
        trailLoc = new Vector2f(missile.getLocation().x - (dir.x * randomTrail), missile.getLocation().y - (dir.y * randomTrail));
        trailSize = new Vector2f(flipTrail * size, size / 2f);
        trail.setCenter((flipTrail * size) / 2, size * 0.85f / 2f);
        MagicRender.singleframe(trail, trailLoc, trailSize, missile.getFacing() - 90, new Color(255, 255, 255, 40), false);




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
                flip * flyDist * 0.53f//* (0.95f + 0.05f * (float) Math.random())
                ,
                (flyDist * 0.53f) / 3//* (0.95f + 0.05f * (float) Math.random())
        );

        missile.getSpriteAPI().setCenter(
                missile.getSpriteAPI().getWidth() / 2,
                missile.getSpriteAPI().getHeight() / 2
        );

        for (ShipEngineControllerAPI.ShipEngineAPI engineS : missile.getEngineController().getShipEngines()) {
            engine.addFloatingText(new Vector2f(engineS.getLocation()), "text" + "", 60, Color.WHITE, missile, 0.25f, 0.25f);
            engineS.getEngineSlot().setContrailWidthMultiplier(15 * power);
        }


        timer.advance(amount);

        missile.setFacing(VectorUtils.getFacing(missile.getVelocity()));

        if (timer.intervalElapsed()) {
            missiles = getMissilesWithinAlmostOval(missile.getLocation(), missile.getFacing(), range);
            for (MissileAPI m : missiles) {
                if (m == missile) continue;
                if (m.getOwner() == missile.getOwner()) continue;
                if (m.getCollisionClass() != CollisionClass.NONE) {
                    vic_combatPlugin.addToListM(m);
                }
            }
            fighters = getFightersWithinAlmostOval(missile.getLocation(), missile.getFacing(), range);
            for (ShipAPI f : fighters) {
                if (f.getOwner() == missile.getOwner()) continue;
                if (f.getCollisionClass() != CollisionClass.NONE) {
                    vic_combatPlugin.addToListF(f);
                }
            }
        }
        /*
        for (MissileAPI m : missiles) {
            if (m == missile) continue;
            if (m.getCollisionClass() != CollisionClass.NONE) {
                float dis = 1;//16.5f * power / MathUtils.getDistance(m.getLocation(), missile.getLocation());
                //engine.addFloatingText(new Vector2f(m.getLocation().x, m.getLocation().y), MathUtils.getDistance(m.getLocation(), missile.getLocation()) + "", 60, Color.WHITE,m, 0.25f, 0.25f);
                if (dis > 1) dis = 1;


                m.getVelocity().scale(1 - 0.95f * (float) Math.pow(dis, 1.5f));

                engine.applyDamage(m, m.getLocation(), 350 * amount, DamageType.FRAGMENTATION, 0f, false, false, missile.getSource());
                m.setJitter(missile, new Color(44, 255, 255), 10, 15, 2);

            }
        }
        */

        /*
        timerTrail.advance(amount);
        if (timerTrail.intervalElapsed()){
            MagicRender.battlespace(trail, trailLoc, new Vector2f(), trailSize, new Vector2f(), facing - 90, 0, new Color(255, 255, 255, 120), false, 0.1f, 0.2f, 0.1f, CombatEngineLayers.ASTEROIDS_LAYER);
        }
        */


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


                range = ((float) Math.random() - 0.5f) * size;
                Vector2f point = new Vector2f(missile.getLocation().x + leftSide.x * range, missile.getLocation().y + leftSide.y * range);
                engine.addHitParticle(
                        point,
                        new Vector2f(),
                        10,
                        1,
                        1f,
                        new Color(250, 183, 50, 250)
                );

                range = ((float) Math.random() - 0.5f) * size;
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

            /*
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
            */


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