package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicUI;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class vic_shieldHardening extends BaseShipSystemScript {

    public static float
            shieldDamageTakenReduction = 0.4f,
            weaponRoFReduction = 0.3f;

    public float baseOverShields = 0;
    public float currentOverShield = 0;

    boolean doOnce = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        final ShipAPI ship = (ShipAPI) stats.getEntity();

        if (doOnce){
            baseOverShields = ship.getMaxFlux() * 1.5f;
            currentOverShield = baseOverShields;
            ship.addListener(new DamageTakenModifier() {
                @Override
                public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
                    if (currentOverShield <= 0) return null;
                    if (target instanceof ShipAPI){
                        if (ship.getSystem().isStateActive() && ship.getFluxLevel() > 0.5f){
                            float absorptionRate = Math.max(0,Math.min(1,(ship.getFluxLevel() - 0.5f) * 2.5f)) * 0.7f * ship.getSystem().getEffectLevel();
                            float actualDamage = damage.computeDamageDealt(Global.getCombatEngine().getElapsedInLastFrame());
                            switch (damage.getType()){
                                case KINETIC:
                                    actualDamage *=2;
                                    break;
                                case HIGH_EXPLOSIVE:
                                    actualDamage *=0.5;
                                    break;
                                case FRAGMENTATION:
                                    actualDamage *=0.25;
                                    break;
                            }
                            /*
                            actualDamage *= ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
                            actualDamage *= ship.getMutableStats().getShieldAbsorptionMult().getModifiedValue();

                             */
                            float damagePrevented = actualDamage * absorptionRate;
                            float fluxPerDamage = ship.getShield().getFluxPerPointOfDamage() * ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
                            currentOverShield -= damagePrevented * fluxPerDamage;
                            if (damagePrevented >= currentOverShield){
                                absorptionRate = currentOverShield / damage.getDamage();
                                damagePrevented = currentOverShield;
                                currentOverShield = 0;
                            }
                            //Global.getLogger(vic_shieldHardening.class).info(damage.getDamage() + "\\" + actualDamage + "\\" + damage.getDpsDuration());
                            damage.getModifier().modifyMult("vic_fortressProtocol", 1 - absorptionRate);
                            return "vic_fortressProtocol";
                        }
                    }
                    return null;
                }
            });
            Global.getCombatEngine().addPlugin(new BaseEveryFrameCombatPlugin() {
                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    float everShieldLevel = currentOverShield/baseOverShields;
                    MagicUI.drawInterfaceStatusBar(
                            ship,
                            everShieldLevel,
                            Misc.getPositiveHighlightColor(),
                            Misc.getPositiveHighlightColor(),
                            1,
                            "Shield",
                            Math.round(currentOverShield)
                    );
                }
            });
            doOnce = false;
        }

        stats.getShieldDamageTakenMult().modifyMult(id, 1f - shieldDamageTakenReduction * effectLevel);

        stats.getShieldUnfoldRateMult().modifyMult(id, 1f + (1f * effectLevel));

        stats.getBallisticRoFMult().modifyMult(id, 1f - weaponRoFReduction * effectLevel);

        stats.getEnergyRoFMult().modifyMult(id, 1f - weaponRoFReduction * effectLevel);

        float absorptionRate = Math.max(0,Math.min(1,(ship.getFluxLevel() - 0.5f) * 2.5f)) * 0.7f;
        if (Global.getCombatEngine().getPlayerShip() == ship && currentOverShield > 0){
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_shieldHardeningPassive", "graphics/icons/hullsys/fortress_shield.png", "Absorption rate", Math.round(absorptionRate * 100f) + "%", false);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0){
            if (index == 0) {
                return new StatusData("Shield damage taken -" + Math.round(shieldDamageTakenReduction * 100f * effectLevel) + "%", false);
            } else if (index == 1){
                return new StatusData("Weapon rate of fire -" + Math.round(weaponRoFReduction * 100f * effectLevel) + "%", true);
            }
        }
        return null;
    }
}
