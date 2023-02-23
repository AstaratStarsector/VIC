package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import data.scripts.weapons.ai.vic_apocryphaSub;
import data.scripts.weapons.ai.vic_hatifMissileAI;
import data.scripts.weapons.ai.vic_swervingDumbfire;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_apocryphaScript extends vic_missileFluxGen {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        super.onFire(projectile, weapon, engine);
        float spread = 15f;
        for (int i = 0; i < 16; i++) {
            float angle = spread * 0.5f - (spread / 3 * (i % 4)) + MathUtils.getRandomNumberInRange(spread * 0.3f, spread * -0.3f);
            CombatEntityAPI proj = engine.spawnProjectile(weapon.getShip(), weapon, "vic_apocrypha_sub", weapon.getFirePoint(0), weapon.getCurrAngle() + angle, weapon.getShip().getVelocity());
            Vector2f vel = new Vector2f(150 * (Math.round(i * 0.25f - 0.5f) + MathUtils.getRandomNumberInRange(-0.5f,0.5f)), 0);
            VectorUtils.rotate(vel, proj.getFacing());
            proj.getVelocity().set(proj.getVelocity().x + vel.x, proj.getVelocity().y + vel.y);
            ((MissileAPI) proj).setMissileAI(new vic_apocryphaSub(((MissileAPI) proj), ((MissileAPI) projectile)));
        }
    }
}
