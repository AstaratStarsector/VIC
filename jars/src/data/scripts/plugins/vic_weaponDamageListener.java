package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class vic_weaponDamageListener implements DamageDealtModifier {

    @Override
    public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {

        if (param instanceof DamagingProjectileAPI) {
            if (((DamagingProjectileAPI) param).getProjectileSpecId() != null) {
                String projID = ((DamagingProjectileAPI) param).getProjectileSpecId();
                switch (projID) {
                    case "vic_arcaneBolt":
                        if (target.getOwner() == ((DamagingProjectileAPI) param).getOwner()) {
                            damage.getModifier().modifyMult("vic_damage", 0);
                            return "vic_damage";
                        }
                        break;
                    case "vic_heavylaidlawaccelerator_shot":
                    case "vic_laidlawMassDriver_shot":
                        if (shieldHit) {
                            damage.setSoftFlux(true);
                        }
                        break;
                    case "vic_balachkoFire":
                    case "vic_balachkoIce":
                        if (!(target instanceof ShipAPI)) break;
                        if (shieldHit) break;
                        float damageMult = 0.65f;
                        ((ShipAPI) target).getMutableStats().getHullDamageTakenMult().modifyMult("vic_damage", damageMult);
                        if (!((ShipAPI) target).hasListenerOfClass(vic_bolachkoRefers.class)) {
                            ((ShipAPI) target).addListener(new vic_bolachkoRefers());
                        }
                }
            }
        }
        return null;
    }

    public static class vic_bolachkoRefers implements DamageTakenModifier {

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (param instanceof DamagingProjectileAPI) {
                if (((DamagingProjectileAPI) param).getProjectileSpecId() != null) {
                    String projID = ((DamagingProjectileAPI) param).getProjectileSpecId();
                    switch (projID) {
                        case "vic_balachkoFire":
                        case "vic_balachkoIce":
                            return null;

                    }
                }
            }
            ((ShipAPI) target).getMutableStats().getHullDamageTakenMult().unmodify("vic_damage");
            ((ShipAPI) target).removeListener(this);
            return null;
        }
    }
}
