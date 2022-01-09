package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.MagicTrailPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class vic_fighterTrail implements EveryFrameWeaponEffectPlugin {

    private IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.05f);
    private Float trailID = null;
    private SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "trails_trail_twin");

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI ship = weapon.getShip();
        float brightness = 0.3f;
        float angle = Misc.getAngleInDegrees(new Vector2f(ship.getVelocity()));

        effectInterval.advance(engine.getElapsedInLastFrame());
        if (effectInterval.intervalElapsed()) {
            if (trailID == null) {
                trailID = MagicTrailPlugin.getUniqueID();
            }
            MagicTrailPlugin.AddTrailMemberSimple(ship, trailID, trailSprite,
                    weapon.getLocation(),
                    0f,
                    angle,
                    10f,
                    3f,
                    Color.cyan,
                    brightness,
                    0f,
                    0f,
                    2f,
                    true);
        }

        //Glows off in refit screen
        if (ship.getOriginalOwner() == -1) {
            brightness = 0f;
            trailID = null;
        }

        //Switches to the proper sprite
        if (brightness > 0) {
            weapon.getAnimation().setFrame(1);
        } else {
            weapon.getAnimation().setFrame(0);
        }

        Color colorToUse = new Color(0f, 1f, 1f, brightness);
        weapon.getSprite().setColor(colorToUse);
    }
}
