package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class vic_devostatorFire implements EveryFrameWeaponEffectPlugin {

    private float shot = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {


        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) {
            return;
        }

        if (MagicRender.screenCheck(0.25f, weapon.getLocation())) {
            if (shot == 1) shot = 2;
            if (weapon.getChargeLevel() == 1) shot = 1;
            if (shot == 2) {
                Global.getSoundPlayer().playSound("vic_besomar_shot", 1, 1f, weapon.getLocation(), weapon.getShip().getVelocity());
                for (DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200)) {

                    if (p.getWeapon() != weapon) continue;

                    p.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));

                }
                shot = 0;
            }

        }
    }
}