package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_psotnikOnHit implements OnHitEffectPlugin {


    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            if (Math.random() > 0.80f) {
                engine.spawnEmpArc(projectile.getSource(),
                        point,
                        target,
                        target,
                        DamageType.FRAGMENTATION,
                        0,
                        50,
                        3000,
                        "tachyon_lance_emp_impact",
                        4,
                        Color.WHITE,
                        Color.CYAN);
            }
        }
    }
}
