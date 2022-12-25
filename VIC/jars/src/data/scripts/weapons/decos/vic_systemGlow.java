package data.scripts.weapons.decos;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.awt.*;

public class vic_systemGlow implements EveryFrameWeaponEffectPlugin {


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        weapon.getAnimation().setFrame(0);
        ShipAPI ship = weapon.getShip();
        float effectLevel = ship.getSystem().getEffectLevel();
        if (effectLevel > 0) {
            weapon.getAnimation().setFrame(1);

            float currentBrightness = effectLevel;

            if (ship.isHulk() || ship.isPhased()) {
                currentBrightness = 0f;
            }

            Color colorToUse = new Color(255, 255, 255, Math.round(currentBrightness * 255));
            weapon.getSprite().setColor(colorToUse);
        }
    }
}
