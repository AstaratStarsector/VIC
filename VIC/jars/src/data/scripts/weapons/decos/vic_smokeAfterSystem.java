package data.scripts.weapons.decos;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_smokeAfterSystem implements EveryFrameWeaponEffectPlugin {

    boolean systemActive = false;
    boolean emitSmoke = false;

    float
            duration = 1.5f,
            time = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!systemActive && weapon.getShip().getSystem().isStateActive()) systemActive = true;
        if (systemActive && !weapon.getShip().getSystem().isStateActive()){
            emitSmoke = true;
            systemActive = false;
        }

        if (time >= duration){
            time = 0f;
            emitSmoke = false;
        }
        if (emitSmoke){
            time += amount;

            Vector2f vel = (Vector2f) Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle()).scale(180f);
            Vector2f loc = new Vector2f(weapon.getLocation().x + vel.x * 0.15f, weapon.getLocation().y + vel.y * 0.15f);
            engine.addNebulaSmokeParticle(loc, vel, MathUtils.getRandomNumberInRange(30f, 40f), 1.25f, 0.1f, 0.3f, 0.5f, new Color(50, 50, 50, 50));
            //engine.addSmokeParticle(loc, vel, MathUtils.getRandomNumberInRange(10f, 30f), MathUtils.getRandomNumberInRange(0.5f, 0.9f), 0.5f, new Color(50, 50, 50, 50));
        }
    }
}
