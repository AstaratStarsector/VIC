package data.scripts.weapons.bigIron;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.Random;

public class vic_notoriousBigIronDragonBreathShot implements OnFireEffectPlugin {

    float count = 0;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        projectile.getVelocity().scale(1f + MathUtils.getRandomNumberInRange(-0.15f, 0.15f));


        if (count == 0){
            Global.getSoundPlayer().playSound("vic_notorious_big_iron_dragon_breath_shot", 1, 1f, weapon.getLocation(), weapon.getShip().getVelocity());
        }
        if (count == 9){
            count = 0;
        } else {
            count++;
        }

    }
}