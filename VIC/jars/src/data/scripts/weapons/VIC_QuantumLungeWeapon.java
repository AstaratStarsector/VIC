package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Made by PureTilt for Astarat
public class VIC_QuantumLungeWeapon implements EveryFrameWeaponEffectPlugin {

    private final IntervalUtil timer = new IntervalUtil(0.5f, 0.5f);
    private final Map<ShipAPI.HullSize, Float> MULT = new HashMap<>();
    private boolean runOnce = false;
    private boolean runOnce2 = false;
    private ShipSystemAPI SYSTEM;
    private ShipAPI SHIP;
    private float ElapsedTIme = 0f;

    {
        MULT.put(ShipAPI.HullSize.DEFAULT, 1.0F);
        MULT.put(ShipAPI.HullSize.FIGHTER, 0.75F);
        MULT.put(ShipAPI.HullSize.FRIGATE, 0.5F);
        MULT.put(ShipAPI.HullSize.DESTROYER, 0.3F);
        MULT.put(ShipAPI.HullSize.CRUISER, 0.2F);
        MULT.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.1F);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (!this.runOnce) {
            this.runOnce = true;
            this.SHIP = weapon.getShip();
            this.SYSTEM = this.SHIP.getSystem();
        }

        if (this.SYSTEM != null && !engine.isPaused() && this.SHIP.isAlive()) {
            //engine.maintainStatusForPlayerShip("Jump time", "graphics/icons/hullsys/ammo_feeder.png", "Debug 1", ElapsedTIme + "", false);
            //engine.maintainStatusForPlayerShip("Jump time", "graphics/icons/hullsys/ammo_feeder.png", "Debug 1", this.SYSTEM.getState().toString(), false);
            if (this.SYSTEM.isStateActive()) {
                ElapsedTIme += amount;
                if (!this.runOnce2 && ElapsedTIme >= this.SYSTEM.getChargeActiveDur()) {
                    this.SHIP.useSystem();
                    //engine.addFloatingText(this.SHIP.getLocation(), "aaaaaaaaaaaaaaaaaaaaaaa", 60, Color.WHITE, this.SHIP, 1, 2);
                    this.runOnce2 = true;
                }
            }
            if (this.SYSTEM.isChargedown()) {
                this.runOnce2 = false;

                ElapsedTIme = 0f;

                Iterator<CombatEntityAPI> i$ = CombatUtils.getEntitiesWithinRange(this.SHIP.getLocation(), 550.0F).iterator();

                while (true) {
                    CombatEntityAPI c;
                    do {
                        do {
                            do {
                                if (!i$.hasNext()) {
                                    return;
                                }
                                c = i$.next();
                            } while (c == this.SHIP);
                        } while (c instanceof DamagingProjectileAPI && ((DamagingProjectileAPI) c).getSource() == this.SHIP);
                    } while (c instanceof ShipAPI && ((ShipAPI) c).isPhased());

                    if (MathUtils.isWithinRange(c.getLocation(), this.SHIP.getLocation(), 550.0F)) {
                        Vector2f repulsion = MathUtils.getPoint(this.SHIP.getLocation(), 550.0F, VectorUtils.getAngle(this.SHIP.getLocation(), c.getLocation()));
                        Vector2f.sub(repulsion, c.getLocation(), repulsion);
                        repulsion.scale(amount * 4.0f);
                        Vector2f cLoc = c.getLocation();
                        Vector2f.add(cLoc, repulsion, cLoc);
                        Vector2f push = new Vector2f(repulsion);
                        if (c instanceof ShipAPI) {
                            push.scale(this.MULT.get(((ShipAPI) c).getHullSize()));
                        }

                        Vector2f cVel = c.getVelocity();
                        Vector2f.add(cVel, push, cVel);
                        if (c.getOwner() != this.SHIP.getOwner()) {
                            if (c instanceof DamagingProjectileAPI) {
                                c.setCollisionClass(CollisionClass.PROJECTILE_FF);
                            }

                            if (c instanceof MissileAPI) {
                                ((MissileAPI) c).flameOut();
                            }
                        }
                    }
                }
            }
        }
    }
}