package data.scripts.weapons.decos;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import data.scripts.util.MagicSettings;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;

public class vic_OmniLuneVisualEffect implements EveryFrameWeaponEffectPlugin {

    public final ArrayList<Color> rainbow = new ArrayList<>();
    private final IntervalUtil CD = new IntervalUtil(0.01f, 0.01f);
    private boolean caramelldansenMode = false;
    int colorNumber = 0;
    private Color color;
    private boolean doOnce = true;

    {
        rainbow.add(new Color(255, 0, 0, 160));
        rainbow.add(new Color(255, 127, 0, 160));
        rainbow.add(new Color(255, 255, 0, 160));
        rainbow.add(new Color(0, 255, 0, 160));
        rainbow.add(new Color(20, 0, 255, 160));
        rainbow.add(new Color(75, 0, 130, 160));
        rainbow.add(new Color(148, 0, 211, 160));
    }


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) return;

        ShipAPI ship = weapon.getShip();

        if (doOnce) {
            caramelldansenMode = MagicSettings.getBoolean("vic", "OmniLunge_rainbowMode");
            ShipEngineControllerAPI.ShipEngineAPI thruster = ship.getEngineController().getShipEngines().get(0);
            int Red = thruster.getEngineColor().getRed();
            int Green = thruster.getEngineColor().getGreen();
            int Blue = thruster.getEngineColor().getBlue();

            if (ship.getVariant().hasHullMod("safetyoverrides")) {
                Red = Math.round((Red * 0.8f) + (255 * 0.2f)) - 1;
                Green = Math.round((Green * 0.8f) + (100 * 0.2f)) - 1;
                Blue = Math.round((Blue * 0.8f) + (255 * 0.2f)) - 1;
            }

            color = new Color(Red, Green, Blue, 128);

            doOnce = false;
        }

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
}
