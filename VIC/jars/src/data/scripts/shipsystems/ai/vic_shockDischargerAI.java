package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class vic_shockDischargerAI implements ShipSystemAIScript {

    private final IntervalUtil
            timer = new IntervalUtil(0.75f, 1f);
    boolean
            doOnce = true,
            allBombers = true;
    private ShipAPI ship;


    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        for (FighterWingAPI wing : ship.getAllWings()) {
            if (!wing.getSpec().isBomber()) allBombers = false;
            if (!allBombers) break;
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        timer.advance(amount);
        if (!timer.intervalElapsed()) return;

        if (doOnce){

            doOnce = false;
        }

        if (ship.getSystem().isCoolingDown()) return;
        if (ship.getFluxLevel() >= 0.35f){
            ShipAPI enemy = AIUtils.getNearestEnemy(ship);
            if (enemy != null && MathUtils.isWithinRange(enemy.getLocation(), ship.getLocation(), 1500)) ship.useSystem();
        }
    }

}
