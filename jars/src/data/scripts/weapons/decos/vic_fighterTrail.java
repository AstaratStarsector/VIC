package data.scripts.weapons.decos;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.magiclib.plugins.MagicTrailPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class vic_fighterTrail implements EveryFrameWeaponEffectPlugin {

    IntervalUtil effectInterval = new IntervalUtil(0.1f, 0.1f);
    Float trailID = null,
            Duration = 0f;
    SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "trails_trail_twin");


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI ship = weapon.getShip();
        float brightness = 0.4f;
        float angle = Misc.getAngleInDegrees(new Vector2f(ship.getVelocity()));

        effectInterval.advance(amount);
        if (effectInterval.intervalElapsed()) {
            if (ship.isAlive() &&
                    !ship.getEngineController().isFlamedOut() &&
                    (ship.getWing().getSourceShip() == null ||
                            !(ship.getWing().getSourceShip().isPullBackFighters() && MathUtils.isWithinRange(weapon.getShip().getLocation(),  ship.getWing().getSourceShip().getLocation(),750)))) {
                if (trailID == null) {
                    trailID = MagicTrailPlugin.getUniqueID();
                    Duration = 2f * (200f / ship.getMaxSpeedWithoutBoost());
                }
                MagicTrailPlugin.addTrailMemberSimple(
                        ship,
                        trailID,
                        trailSprite,
                        weapon.getLocation(),
                        0f,
                        angle,
                        10f,
                        3f,
                        Color.cyan,
                        brightness,
                        0f,
                        0f,
                        Duration,
                        true);
            } else {
                trailID = null;
            }

            //Glows off in refit screen
            if (ship.getOriginalOwner() == -1) {
                brightness = 0f;
                trailID = null;
            }

        /*
        //Switches to the proper sprite
        if (brightness > 0) {
            weapon.getAnimation().setFrame(1);
        } else {
            weapon.getAnimation().setFrame(0);
        }

        Color colorToUse = new Color(0f, 1f, 1f, brightness);
        weapon.getSprite().setColor(colorToUse);

         */
        }
    }
}
