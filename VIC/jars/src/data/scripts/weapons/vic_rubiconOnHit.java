package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_rubiconOnHit implements OnHitEffectPlugin {

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, CombatEngineAPI engine) {

        //if (!(target instanceof ShipAPI)) return;
        //if (projectile.isFading()) return;
        for (int i = 0; i < 4; i++) {

            float toDaysRandom = MathUtils.getRandomNumberInRange(-15, 15);
            float toDaysRandom2 = MathUtils.getRandomNumberInRange(0.8f, 1.2f);
            Vector2f Dir = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing() + toDaysRandom);
            Vector2f SpawnPoint = new Vector2f(point.x + Dir.x * -300 * toDaysRandom2, point.y + Dir.y * -300 * toDaysRandom2);

            DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(projectile.getSource(),
                    projectile.getWeapon(),
                    "vic_rubicon_sub",
                    SpawnPoint,
                    projectile.getFacing() + (toDaysRandom + MathUtils.getRandomNumberInRange(-5, 5)),
                    target.getVelocity());

            if (projectile.isFading()) {
                proj.setDamageAmount(projectile.getBaseDamageAmount() * 0.125f);
            } else {
                proj.setDamageAmount(projectile.getBaseDamageAmount() * 0.25f);
            }

            if (MagicRender.screenCheck (100, SpawnPoint)) engine.addPlugin(new vic_nawiaVisuals(SpawnPoint, proj.getFacing()));

        }
    }
}

