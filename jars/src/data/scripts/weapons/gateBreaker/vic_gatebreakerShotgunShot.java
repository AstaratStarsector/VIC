package data.scripts.weapons.gateBreaker;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.Random;

public class vic_gatebreakerShotgunShot implements OnFireEffectPlugin {

    float count = 0;
    String smallID = "vic_gatebreaker_shotgun_small";
    String bigID = "vic_gatebreaker_shotgun_big";

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        projectile.getVelocity().scale(1f + MathUtils.getRandomNumberInRange(-0.15f, 0.15f));


        float randomNum = (float) Math.random();
        if (randomNum <= 0.33f){
            DamagingProjectileAPI spawnedProjSmall = (DamagingProjectileAPI) engine.spawnProjectile(projectile.getSource(), weapon, smallID, projectile.getLocation(), projectile.getFacing(), weapon.getShip().getVelocity());
            spawnedProjSmall.getVelocity().scale(1f + MathUtils.getRandomNumberInRange(-0.15f, 0.15f));
            engine.removeEntity(projectile);
        } else if (randomNum >= 0.66) {
            DamagingProjectileAPI spawnedProjBig = (DamagingProjectileAPI) engine.spawnProjectile(projectile.getSource(), weapon, bigID, projectile.getLocation(), projectile.getFacing(), weapon.getShip().getVelocity());
            spawnedProjBig.getVelocity().scale(1f + MathUtils.getRandomNumberInRange(-0.15f, 0.15f));
            engine.removeEntity(projectile);
        }


        if (count == 0){
            Global.getSoundPlayer().playSound("vic_gatebreaker_shotgun", 1, 1f, weapon.getLocation(), weapon.getShip().getVelocity());
        }
        if (count == 19){
            count = 0;
        } else {
            count++;
        }

    }
}