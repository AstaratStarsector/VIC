package data.scripts.weapons.decos;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.shipsystems.vic_shockDischarger;
import data.scripts.util.MagicAnim;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.Map;

public class vic_oriaxSystemGlow implements EveryFrameWeaponEffectPlugin {

    public AnimationAPI animation;
    public boolean DoOnce = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (DoOnce){
            animation = weapon.getAnimation();
        }
        Color alpha = new Color(255, 255, 255, 0);
        if (weapon.getShip() != null && weapon.getShip().getOwner() != -1) {
            animation.setFrame(1);

            String customDataID = "vic_shockDischargerPower" + weapon.getShip().getId();

            Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();

            float power = 0;
            if (customCombatData.get(customDataID) instanceof Float)
                power = (float) customCombatData.get(customDataID);

            //Global.getCombatEngine().maintainStatusForPlayerShip("vic_oriaxSystemGlow", null, "Glow", power + "", false);

            alpha = new Color(255, 255, 255, Math.round(255 * MagicAnim.smooth(power)));
        }
        weapon.getSprite().setColor(alpha);
        if (weapon.getShip().isHulk()) {
            animation.setFrame(0);
        }
    }
}
