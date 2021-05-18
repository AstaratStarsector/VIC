package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;

public class vic_balachkoScript implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    boolean firstBarrel = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (firstBarrel){
            engine.spawnProjectile(weapon.getShip(), weapon, "vic_balachkoIce", projectile.getLocation(), projectile.getFacing(), weapon.getShip().getVelocity());
            firstBarrel = false;
        } else {
            engine.spawnProjectile(weapon.getShip(), weapon, "vic_balachkoFire", projectile.getLocation(), projectile.getFacing(), weapon.getShip().getVelocity());
            firstBarrel = true;
        }
        engine.removeEntity(projectile);

    }
}
