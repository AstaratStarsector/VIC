package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import data.scripts.plugins.vic_combatPlugin;

public class vic_missileFluxGen implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {



    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;

        //engine.maintainStatusForPlayerShip(weapon.getSlot().getId(),null, "Angle diff", Math.round(Misc.getAngleDiff(weapon.getCurrAngle(), weapon.getShip().getFacing())) + "/" + Math.round(weapon.getShip().getFacing()), false);
        if (weapon.getAmmo() < weapon.getMaxAmmo()) {
            FluxTrackerAPI fluxTracker = weapon.getShip().getFluxTracker();
            float reloadFractionPerSecond = weapon.getAmmoTracker().getAmmoPerSecond() / weapon.getAmmoTracker().getReloadSize();
            float addFlux = (weapon.getFluxCostToFire() / weapon.getSpec().getBurstSize()) * weapon.getAmmoTracker().getAmmoPerSecond() * weapon.getShip().getMutableStats().getMissileAmmoRegenMult().getModifiedValue();
            addFlux *= amount;
            float availableReloadFraction = 1;
            if (fluxTracker.getCurrFlux() + addFlux > fluxTracker.getMaxFlux()){
                availableReloadFraction = (fluxTracker.getMaxFlux() - fluxTracker.getCurrFlux()) / addFlux;
                weapon.getAmmoTracker().setReloadProgress(weapon.getAmmoTracker().getReloadProgress() - (reloadFractionPerSecond * amount * (1 - availableReloadFraction)));

            }
            fluxTracker.increaseFlux(addFlux * availableReloadFraction, false);
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        weapon.getShip().getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() / weapon.getSpec().getBurstSize());
    }
}
