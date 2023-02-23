package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

public class vic_devostatorFire implements OnFireEffectPlugin {

    float invert = 1;
    float count = 0;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (count == 0){
            Global.getSoundPlayer().playSound("vic_besomar_shot", 1, 1f, weapon.getLocation(), weapon.getShip().getVelocity());
        }
        if (count == 6){
            count = 0;
        } else {
            count++;
        }
        projectile.getVelocity().scale(1f + (MathUtils.getRandomNumberInRange(0f, 0.1f) * invert));
        if (Math.random() <= 0.75f) invert *= -1;
    }
}