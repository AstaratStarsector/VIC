package data.scripts.weapons.decos;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_fevor_deco implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce = false;
    private ShipAPI SHIP;
    private Vector2f position;
    private boolean haveFevor = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        //some initial setup
        if (!this.runOnce) {
            runOnce = true;
            SHIP = weapon.getShip();
            position = new Vector2f(weapon.getSprite().getCenterX(),weapon.getSprite().getCenterY());
            if (!SHIP.getVariant().hasHullMod("vic_deathProtocol")) {
                weapon.getSprite().setColor(new Color(255,255,255, 0));
                haveFevor = false;
            }
        }
        if (haveFevor){
            Vector2f newCenter = new Vector2f(position.x + MathUtils.getRandomNumberInRange(0f,1f),position.y + MathUtils.getRandomNumberInRange(0f,1f));
            //weapon.getSprite().setCenter(newCenter.x, newCenter.y);
        }
    }
}