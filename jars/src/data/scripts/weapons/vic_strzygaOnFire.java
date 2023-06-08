package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_strzygaOnFire implements OnFireEffectPlugin {

    float invert = 1;

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        projectile.getVelocity().scale(1f + (MathUtils.getRandomNumberInRange(0f, 0.10f) * invert));
        if (Math.random() <= 0.75f) invert *= -1;
    }

}


