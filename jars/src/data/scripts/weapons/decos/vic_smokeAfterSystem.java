package data.scripts.weapons.decos;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_smokeAfterSystem implements EveryFrameWeaponEffectPlugin {

    boolean systemActive = false;
    boolean emitSmoke = false;

    float
            duration = 2f,
            time = 0f;


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!systemActive && weapon.getShip().getSystem().getState().equals(ShipSystemAPI.SystemState.IN)) systemActive = true;
        if (systemActive && weapon.getShip().getSystem().getState().equals(ShipSystemAPI.SystemState.OUT)){
            emitSmoke = true;
            systemActive = false;
        }

        if (weapon.getShip().getHullSpec().getHullId().startsWith("vic_stolas_bou")) {
            duration = 3f;
        }

        if (time >= duration){
            time = 0f;
            emitSmoke = false;
        }
        if (emitSmoke) {
            time += amount;

            float smokeDir = weapon.getCurrAngle();
            float smokeDirAngle = smokeDir + MathUtils.getRandomNumberInRange(-20, 20);

            Vector2f vel = (Vector2f) Misc.getUnitVectorAtDegreeAngle(smokeDirAngle).scale(180f);
            Vector2f loc = new Vector2f(weapon.getLocation().x + vel.x * 0.05f, weapon.getLocation().y + vel.y * 0.05f);
            if (weapon.getShip().getHullSpec().getHullId().startsWith("vic_stolas_bou")) {
                engine.addNebulaSmokeParticle(loc, vel, MathUtils.getRandomNumberInRange(15f, 25f), 2.5f, 0.3f, 0.3f, 0.6f, new Color(33, 33, 33, 200));
                engine.addSmokeParticle(loc, vel, MathUtils.getRandomNumberInRange(5f, 15f), 2.5f, 0.2f,  new Color(255, 75, 33, 200));
                //engine.addSmokeParticle(loc, vel, MathUtils.getRandomNumberInRange(10f, 30f), MathUtils.getRandomNumberInRange(0.5f, 0.9f), 0.5f, new Color(50, 50, 50, 50));
            }
            else engine.addNebulaSmokeParticle(loc, vel, MathUtils.getRandomNumberInRange(15f, 25f), 2.5f, 0.3f, 0.3f, 0.6f, new Color(255, 255, 255, 50));
        }
    }
}
