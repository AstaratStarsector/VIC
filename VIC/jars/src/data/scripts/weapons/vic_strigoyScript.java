package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

public class vic_strigoyScript implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        Global.getSoundPlayer().playSound("flak_fire", 1, 1f, weapon.getLocation(), weapon.getShip().getVelocity());
        projectile.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));
    }
}