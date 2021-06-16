package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;


public class vic_mineleyer_ai implements ShipSystemAIScript {

    public final IntervalUtil
            timer = new IntervalUtil(1f, 1.5f);
    public ShipAPI ship = null;
    public float missileCheckRange = 700;
    public float fighterCheckRange = 500;
    public boolean ignoreFlares;


    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.ignoreFlares = ship.getMutableStats().getDynamic().getMod(Stats.PD_IGNORES_FLARES).getFlatBonus() >= 1;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (ship == null) return;
        timer.advance(amount);
        if (!timer.intervalElapsed() || ship.getSystem().getAmmo() == 0) return;
        boolean useShipSystem = false;
        for (MissileAPI missile : AIUtils.getEnemyMissilesOnMap(ship)) {
            if (ignoreFlares) {
                if (missile.isFlare()) continue;
            }
            if (MathUtils.isWithinRange(ship.getLocation(), missile.getLocation(), missileCheckRange)) {
                useShipSystem = true;
                break;
            }
        }
        for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, fighterCheckRange)) {
            if (enemy.getHullSize().equals(ShipAPI.HullSize.FIGHTER)) {
                useShipSystem = true;
                break;
            } else if (ship.getSystem().getAmmo() > 1) {
                useShipSystem = true;
                break;
            }
        }
        if (useShipSystem) {
            ship.useSystem();
        }
    }

}
