package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_astronomiconOnHit1 implements OnHitEffectPlugin {


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI && !shieldHit) {
            engine.spawnEmpArc(projectile.getSource(),
                    point,
                    target,
                    target,
                    DamageType.ENERGY,
                    0,
                    projectile.getEmpAmount(),
                    1000,
                    null,
                    2,
                    new Color(MathUtils.getRandomNumberInRange(160, 200), MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(200, 255), 255), //Central color
                    new Color(MathUtils.getRandomNumberInRange(0, 30), MathUtils.getRandomNumberInRange(180, 220), MathUtils.getRandomNumberInRange(220, 255), 255));
        }
    }
}

