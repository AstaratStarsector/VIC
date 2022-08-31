package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;

public class vic_missileFluxGen implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    boolean stopReload = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //if (engine.isPaused()) return;

        //engine.maintainStatusForPlayerShip(weapon.getSlot().getId(),null, "Angle diff", Math.round(Misc.getAngleDiff(weapon.getCurrAngle(), weapon.getShip().getFacing())) + "/" + Math.round(weapon.getShip().getFacing()), false);
        if (weapon.getAmmo() < weapon.getMaxAmmo()) {
            FluxTrackerAPI fluxTracker = weapon.getShip().getFluxTracker();
            float addFlux = weapon.getFluxCostToFire() * weapon.getAmmoTracker().getAmmoPerSecond() * weapon.getShip().getMutableStats().getMissileAmmoRegenMult().getModifiedValue();
            if (stopReload) {
                if (fluxTracker.getMaxFlux() - fluxTracker.getCurrFlux() >= weapon.getFluxCostToFire() * weapon.getAmmoTracker().getReloadSize()) {
                    stopReload = false;
                }
            }
            if (fluxTracker.getCurrFlux() + addFlux > fluxTracker.getMaxFlux() || stopReload) {
                weapon.getAmmoTracker().setAmmoPerSecond(0);
                stopReload = true;
            } else {
                //engine.maintainStatusForPlayerShip(weapon.getDisplayName(),null, weapon.getDisplayName() + " Reload", "Generates: " + addFlux + " flux", false);
                fluxTracker.increaseFlux(addFlux * amount, false);
                weapon.getAmmoTracker().setAmmoPerSecond(weapon.getSpec().getAmmoPerSecond());
            }
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        weapon.getShip().getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() / weapon.getSpec().getBurstSize());
    }
}
