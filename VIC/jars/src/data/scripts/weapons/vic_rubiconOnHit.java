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

        if (!(target instanceof ShipAPI)) return;
        if (projectile.isFading()) return;
        for (int i = 0; i < 4; i++) {

            float toDaysRandom = MathUtils.getRandomNumberInRange(-30, 30);
            float toDaysRandom2 = MathUtils.getRandomNumberInRange(0.7f, 1.3f);
            Vector2f Dir = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing() + toDaysRandom);
            Vector2f SpawnPoint = new Vector2f(point.x + Dir.x * -300 * toDaysRandom2, point.y + Dir.y * -300 * toDaysRandom2);

            engine.spawnProjectile(projectile.getSource(),
                    projectile.getWeapon(),
                    "vic_rubicon_sub",
                    SpawnPoint,
                    projectile.getFacing() + (toDaysRandom * MathUtils.getRandomNumberInRange(0.6f, 1.4f)),
                    new Vector2f());

            float size = MathUtils.getRandomNumberInRange(20, 30);
            float grow = MathUtils.getRandomNumberInRange(size * -0.5f, size * 0.5f);
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_rubicon_river"),
                    SpawnPoint,
                    new Vector2f(),
                    new Vector2f(size, size),
                    new Vector2f(grow, grow),
                    //angle,
                    projectile.getFacing() + (toDaysRandom * MathUtils.getRandomNumberInRange(0.6f, 1.4f)),
                    MathUtils.getRandomNumberInRange(30, 180),
                    new Color(255, 255, 255),
                    true,
                    MathUtils.getRandomNumberInRange(0.1f, 0.3f),
                    MathUtils.getRandomNumberInRange(0, 0.1f),
                    MathUtils.getRandomNumberInRange(0.1f, 0.3f)
            );
            projectile.getSource().getFluxTracker().setCurrFlux(projectile.getSource().getFluxTracker().getCurrFlux() + (50 * projectile.getSource().getMutableStats().getBallisticWeaponFluxCostMod().getBonusMult()));
            Global.getSoundPlayer().playSound("vic_rubicon_quantum_river", 1f, 0.2f, SpawnPoint, new Vector2f());
        }
    }
}

