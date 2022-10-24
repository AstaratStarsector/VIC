package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class vic_gatebreakerShotgunShot implements OnFireEffectPlugin {

    float count = 0;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (count == 0){
            Global.getSoundPlayer().playSound("vic_gatebreaker_shotgun", 1, 1f, weapon.getLocation(), weapon.getShip().getVelocity());
        }
        if (count == 19){
            count = 0;
        } else {
            count++;
        }
        projectile.getVelocity().scale(1f + MathUtils.getRandomNumberInRange(-0.15f, 0.15f));

    }
}