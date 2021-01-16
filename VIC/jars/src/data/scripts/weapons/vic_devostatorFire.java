package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.ArrayList;
import java.util.List;

public class vic_devostatorFire implements EveryFrameWeaponEffectPlugin {

    private final List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();

    private final IntervalUtil
            checkTime = new IntervalUtil(0.5f, 0.5f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {


        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) {
            return;
        }

        checkTime.advance(amount);
        if (checkTime.intervalElapsed()) {
            List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
            for (DamagingProjectileAPI proj : cloneList) {
                if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                    alreadyRegisteredProjectiles.remove(proj);
                }
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
                proj.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));
                alreadyRegisteredProjectiles.add(proj);
            }
        }
    }
}