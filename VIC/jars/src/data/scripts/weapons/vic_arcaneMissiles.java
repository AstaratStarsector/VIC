package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import data.scripts.plugins.vic_combatPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_arcaneMissiles implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        final float startingSpeed = MathUtils.getRandomNumberInRange(0.25f, 0.6f);
        final float rangeBeforeCurving = MathUtils.getRandomNumberInRange(0.25f, 0.6f);

        Vector2f target = weapon.getShip().getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTarget();
        if (target == null && weapon.getShip() == engine.getPlayerShip()) {
            Vector2f mousePosition = CombatUtils.toWorldCoordinates(new Vector2f(Mouse.getX(), Mouse.getY()));
            engine.addHitParticle(mousePosition, new Vector2f(), 20, 1, 1.5f, new Color(1f, 0.3f, 0f));
            float rangeFromMouse = MathUtils.getDistance(weapon.getLocation(), mousePosition);
            rangeFromMouse = MathUtils.clamp(rangeFromMouse, weapon.getRange() * 0.5f, weapon.getRange());
            target = MathUtils.getPoint(weapon.getLocation(), rangeFromMouse, weapon.getCurrAngle());
        }

        projectile.getVelocity().scale(startingSpeed);
        float flightTime = weapon.getRange() / projectile.getMoveSpeed();
        if (target == null) {
            target = MathUtils.getPoint(weapon.getLocation(), weapon.getRange(), weapon.getCurrAngle());
            target = new Vector2f(target.x + weapon.getShip().getVelocity().x * flightTime, target.y + weapon.getShip().getVelocity().y * flightTime);
        }
        projectile.setFacing(VectorUtils.getFacing(projectile.getVelocity()));
        engine.addHitParticle(target, new Vector2f(), 20, 1, 1.5f, new Color(0.3f, 1f, 0f));
        //Global.getLogger(vic_arcaneMissiles.class).info(MouseInfo.getPointerInfo().getLocation().x);
        vic_combatPlugin.AddArcaneMissiles(projectile, target, flightTime * rangeBeforeCurving, rangeBeforeCurving, startingSpeed);
    }
}
