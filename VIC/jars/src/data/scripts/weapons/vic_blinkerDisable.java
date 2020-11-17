package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class vic_blinkerDisable implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //weapon.getSprite().setAdditiveBlend();
        if (weapon.getShip() != null && weapon.getShip().isAlive())
            weapon.getAnimation().setAlphaMult(1f);
        else
            weapon.getAnimation().setAlphaMult(0f);
    }
}
