package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_disruptorShot_AI implements MissileAIPlugin, GuidedMissileAI {

    private final MissileAPI missile;
    private final IntervalUtil timer = new IntervalUtil(0.05f, 0.15f);
    private CombatEngineAPI engine;
    private CombatEntityAPI target;

    private float rotation =0f;

    private Vector2f firstLoc;

    public vic_disruptorShot_AI(MissileAPI missile, ShipAPI launchingShip) {
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        this.missile = missile;
        //target = null;
        //missile.setCollisionClass(CollisionClass.NONE);
        missile.getSpriteAPI().setAdditiveBlend();
    }



    @Override
    public void advance(float amount) {

        setTarget(target);

        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused()) {
            return;
        }
        if (firstLoc == null) {
            firstLoc = new Vector2f(missile.getLocation());
        }
        //init vars

        if (missile.getFlightTime() > 1)
            missile.setUntilMineExplosion(3);


        float facing = missile.getFacing();
        Vector2f location = missile.getLocation();

        missile.getSpriteAPI().setColor(new Color(255, 255, 255, 100));
        missile.setJitter(missile, new Color(255, 255, 255, 100), 3, 10, 1.5f);

        rotation += 120 * amount;
        if (rotation > 360)rotation -= 360;
        SpriteAPI innerRIng = Global.getSettings().getSprite("fx", "vic_disruptorInnerRing");
        //innerRIng.setSize(missile.getSpriteAPI().getWidth(),missile.getSpriteAPI().getHeight());

        MagicRender.singleframe(innerRIng, location, new Vector2f(missile.getSpriteAPI().getWidth(),missile.getSpriteAPI().getHeight()), rotation, new Color(255, 255, 255, 220), false);

        SpriteAPI outerRIng = Global.getSettings().getSprite("fx", "vic_disruptorOuterRing");
        //outerRIng.setSize(missile.getSpriteAPI().getWidth() * 1.6f,missile.getSpriteAPI().getHeight()* 1.6f);

        MagicRender.singleframe(outerRIng, location, new Vector2f(missile.getSpriteAPI().getWidth()* 1.6f,missile.getSpriteAPI().getHeight()* 1.6f), -rotation, new Color(255, 255, 255, 220), false);

        timer.advance(amount);

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