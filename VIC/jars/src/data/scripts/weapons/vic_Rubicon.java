package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class vic_Rubicon implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) {
            return;
        }


        if (weapon.getChargeLevel() == 1) {
            Vector2f trueCenterLocation = new Vector2f();
            if (weapon.getSlot().isHardpoint()) {
                trueCenterLocation.x += weapon.getSpec().getHardpointFireOffsets().get(0).x;
                trueCenterLocation.y += weapon.getSpec().getHardpointFireOffsets().get(0).y;
            } else if (weapon.getSlot().isTurret()) {
                trueCenterLocation.x += weapon.getSpec().getTurretFireOffsets().get(0).x;
                trueCenterLocation.y += weapon.getSpec().getTurretFireOffsets().get(0).y;
            } else {
                trueCenterLocation.x += weapon.getSpec().getHiddenFireOffsets().get(0).x;
                trueCenterLocation.y += weapon.getSpec().getHiddenFireOffsets().get(0).y;
            }

            trueCenterLocation = VectorUtils.rotate(trueCenterLocation, weapon.getCurrAngle(), new Vector2f(0f, 0f));
            trueCenterLocation.x += weapon.getLocation().x;
            trueCenterLocation.y += weapon.getLocation().y;


            float currentDeviation = (weapon.getCurrSpread() - weapon.getSpec().getSpreadBuildup()) * MathUtils.getRandomNumberInRange(-1f, 1f);

            engine.spawnProjectile(weapon.getShip(),
                    weapon,
                    "vic_rubicon_real",
                    trueCenterLocation,
                    weapon.getCurrAngle() + currentDeviation,
                    weapon.getShip().getVelocity());
        }
    }

}