package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import data.scripts.util.MagicSettings;
import org.json.JSONException;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static data.scripts.utilities.vic_getSettings.getBoolean;

public class vic_OmniLunge extends BaseShipSystemScript {

    public static float SPEED_BONUS = 300f;
    public static float TURN_BONUS = 100f;

    public final ArrayList<Color> rainbow = new ArrayList<>();
    private final IntervalUtil CD = new IntervalUtil(0.01f, 0.01f);
    private boolean caramelldansenMode = false;

    int colorNumber = 0;

    private Color
            engineColor;

    private boolean doOnce = true;
    private boolean doOnce_speedUp = true;

    private final Map<ShipAPI.HullSize, Float> strafeMulti = new HashMap<>();

    {
        strafeMulti.put(ShipAPI.HullSize.FIGHTER, 1f);
        strafeMulti.put(ShipAPI.HullSize.FRIGATE, 1f);
        strafeMulti.put(ShipAPI.HullSize.DESTROYER, 0.75f);
        strafeMulti.put(ShipAPI.HullSize.CRUISER, 0.5f);
        strafeMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.25f);
    }

    {
        rainbow.add(new Color(255, 0, 0, 160));
        rainbow.add(new Color(255, 127, 0, 160));
        rainbow.add(new Color(255, 255, 0, 160));
        rainbow.add(new Color(0, 255, 0, 160));
        rainbow.add(new Color(20, 0, 255, 160));
        rainbow.add(new Color(75, 0, 130, 160));
        rainbow.add(new Color(148, 0, 211, 160));
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);

        if (state == State.IN) {
            //stats.getAcceleration().modifyFlat(id, 100f);
            //stats.getDeceleration().modifyFlat(id, 100f);
            afterImage(ship);
        }
        if (state == State.ACTIVE) {
            if (doOnce_speedUp) {
                Vector2f newVector = new Vector2f();
                if (ship.getEngineController().isAccelerating()) {
                    newVector.y += 1 * ship.getAcceleration();
                }
                if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
                    newVector.y -= 1 * ship.getDeceleration();
                }
                if (ship.getEngineController().isStrafingLeft()) {
                    newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
                }
                if (ship.getEngineController().isStrafingRight()) {
                    newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
                }
                VectorUtils.rotate(newVector, ship.getFacing() - 90);
                Vector2f NewSpeed;
                if (VectorUtils.isZeroVector(newVector))
                    NewSpeed = (Vector2f) new Vector2f(ship.getVelocity()).normalise(newVector).scale(ship.getMaxSpeed());
                else NewSpeed = (Vector2f) newVector.normalise(newVector).scale(ship.getMaxSpeed());
                ship.getVelocity().set(NewSpeed);
                doOnce_speedUp = false;
            }
            stats.getAcceleration().modifyFlat(id, 0f);
            stats.getDeceleration().modifyFlat(id, 0f);
            stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * 6);
            stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 6f);

            stats.getMaxTurnRate().modifyFlat(id, TURN_BONUS);
            stats.getMaxTurnRate().modifyPercent(id, TURN_BONUS);
            afterImage(ship);
        }
        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);

            stats.getAcceleration().modifyMult(id, 2f);
            stats.getAcceleration().modifyFlat(id, 50f);
            stats.getDeceleration().modifyMult(id, 2f);
            stats.getDeceleration().modifyFlat(id, 50f);
            stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * 0.5f);
            stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 5f * 0.5f);

        }

        if (stats.getEntity() instanceof ShipAPI) {
            ship.getEngineController().extendFlame(this, 0.5f * effectLevel, 0.5f * effectLevel, 0.25f * effectLevel);
        }
    }

    public void afterImage(ShipAPI ship) {
        if (Global.getCombatEngine().isPaused()) return;

        if (doOnce) {
            try {
                caramelldansenMode = getBoolean("OmniLunge_rainbowMode");
            } catch (JSONException | IOException ignore) {
            }
            ShipEngineControllerAPI.ShipEngineAPI thruster = ship.getEngineController().getShipEngines().get(0);
            engineColor = thruster.getEngineColor();
            doOnce = false;
        }

        Color shift = ship.getEngineController().getFlameColorShifter().getCurr();
        float ratio = shift.getAlpha() / 255f;

        int Red = Math.min(255, Math.round(engineColor.getRed() * (1f - ratio) + shift.getRed() * ratio));
        int Green = Math.min(255, Math.round(engineColor.getGreen() * (1f - ratio) + shift.getGreen() * ratio));
        int Blue = Math.min(255, Math.round(engineColor.getBlue() * (1f - ratio) + shift.getBlue() * ratio));

        Color color = new Color(Red, Green, Blue, 128);

        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (ship.getSystem().isActive()) {
            CD.advance(amount);
            if (CD.intervalElapsed()) {
                if (!MagicRender.screenCheck(0.5f, ship.getLocation())) return;

                if (caramelldansenMode) {
                    color = rainbow.get(colorNumber);
                    colorNumber++;
                    if (colorNumber > rainbow.size() - 1) colorNumber = 0;
                }

                SpriteAPI shipSprite = Global.getSettings().getSprite(ship.getHullSpec().getSpriteName());
                MagicRender.battlespace(
                        shipSprite,
                        new Vector2f(ship.getLocation()),
                        new Vector2f(),
                        new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
                        new Vector2f(),
                        ship.getFacing() - 90,
                        0,
                        color,
                        true,
                        0.1f,
                        0f,
                        0.2f,
                        CombatEngineLayers.UNDER_SHIPS_LAYER);
            }
        }
    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        Vector2f newVector = new Vector2f();
        if (ship.getEngineController().isAccelerating()) {
            newVector.y += 1 * ship.getAcceleration();
        }
        if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
            newVector.y -= 1 * ship.getDeceleration();
        }
        if (ship.getEngineController().isStrafingLeft()) {
            newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isStrafingRight()) {
            newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        return !(VectorUtils.isZeroVector(newVector) && VectorUtils.isZeroVector(ship.getVelocity()));
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        doOnce_speedUp = true;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            if (state == State.IN || state == State.ACTIVE) {
                return new StatusData("+" + (int) SPEED_BONUS + " top speed", false);
            }
            if (state == State.OUT) {
                return new StatusData("+" + (int) (TURN_BONUS) + "% maneuverability", false);
            }
        }
        return null;
    }
}








