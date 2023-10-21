package data.scripts.weapons.decos;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import data.scripts.plugins.MagicTrailPlugin;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class vic_shupalche implements EveryFrameWeaponEffectPlugin {

    //Main phase color
    private static final Color PHASE_COLOR = new Color(0.45f, 0.05f, 0.45f, 0.5f);

    //For our "drill" effects
    private final Vector2f[] drillPositions = {new Vector2f(-45f, -100f), new Vector2f(45f, -100f)};
    private final float[] drillAngles = {-5f, 5f};
    private final float drillSpeed = 1200f;
    private float[] drillTrailIDs = {0f, 0f, 0f, 0f, 0f, 0f};
    private float drillCounter = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        boolean player = ship == Global.getCombatEngine().getPlayerShip();

        //Time counter
        if (Global.getCombatEngine().isPaused()) {
            amount = 0;
        }


        //Moves the "phantom" to its appropriate location
        Vector2f phantomPos = MathUtils.getRandomPointInCircle(null, 55f);
        phantomPos.x += ship.getLocation().x;
        phantomPos.y += ship.getLocation().y;

        //If we are outside the screenspace, don't do the extra visual effects
        if (!Global.getCombatEngine().getViewport().isNearViewport(phantomPos, ship.getCollisionRadius() * 1.5f)) {
            return;
        }

        //And finally spawn our "drill trails"
        //If we have not gotten any IDs for them yet, get some IDs
        if (drillTrailIDs[0] == 0f) {
            for (int i = 0; i < drillTrailIDs.length; i++) {
                drillTrailIDs[i] = MagicTrailPlugin.getUniqueID();
            }
        }

        //Then, spawn six trails, in two different positions, and offset them by angle
        drillCounter += amount;
        SpriteAPI spriteToUse = Global.getSettings().getSprite("SRD_fx", "projectile_trail_fringe");
        for (int i = 0; i < 3; i++) {
            Vector2f positionToSpawn = new Vector2f(ship.getLocation().x, ship.getLocation().y);
            positionToSpawn.y += drillPositions[0].x;   //Quite misleading: the sprite is turned 90 degrees incorrectly when considering coordinates and angles
            positionToSpawn.x += drillPositions[0].y;
            positionToSpawn = VectorUtils.rotateAroundPivot(positionToSpawn, ship.getLocation(), ship.getFacing(), new Vector2f(0f, 0f));
            MagicTrailPlugin.AddTrailMemberAdvanced(ship, drillTrailIDs[i], spriteToUse, positionToSpawn, drillSpeed, drillSpeed * 0.5f,
                    (float)(FastTrig.sin(6f * drillCounter + (Math.toRadians(120 * i))) * 12f) + ship.getFacing() + 180f + drillAngles[0], 0f, 0f, 12f,
                    24f, PHASE_COLOR, Color.BLACK, 0.8f, 0f, 0.1f, 0.1f, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA,
                    64f, 1550f, new Vector2f(0f, 0f), null);
        }
        /*
        for (int i = 0; i < 3; i++) {
            Vector2f positionToSpawn = new Vector2f(ship.getLocation().x, ship.getLocation().y);
            positionToSpawn.y += drillPositions[1].x;   //Quite misleading: the sprite is turned 90 degrees incorrectly when considering coordinates and angles
            positionToSpawn.x += drillPositions[1].y;
            positionToSpawn = VectorUtils.rotateAroundPivot(positionToSpawn, ship.getLocation(), ship.getFacing(), new Vector2f(0f, 0f));
            MagicTrailPlugin.AddTrailMemberAdvanced(ship, drillTrailIDs[i+3], spriteToUse, positionToSpawn, drillSpeed, drillSpeed * 0.5f,
                    (float)(FastTrig.sin(6f * drillCounter + (Math.toRadians(120 * i))) * 12f) + ship.getFacing() + 180f + drillAngles[1], 0f, 0f, 12f,
                    24f, PHASE_COLOR, Color.BLACK, 0.8f, 0.1f, 0f, 0.35f, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA,
                    64f, 1550f, new Vector2f(0f, 0f), null);
        }

         */
    }
}