package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class vic_fighterFlareAI implements ShipSystemAIScript {

    ShipAPI ship;
    CombatEngineAPI engine;

    public final IntervalUtil
            timer = new IntervalUtil(1f, 1.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        boolean isLeader = ship.isWingLeader();
        boolean useShipSystem = false;
        boolean hasAmmo = false;
        for (ShipAPI member : ship.getWing().getWingMembers()){
            if (member.getSystem().getAmmo() > 0){
                hasAmmo = true;
                break;
            }
        }
        if (isLeader){
            timer.advance(amount);
            if (!timer.intervalElapsed() || !hasAmmo) return;
            for (MissileAPI missile : AIUtils.getEnemyMissilesOnMap(ship)) {
                if (missile.isFlare()) continue;
                if (MathUtils.isWithinRange(ship.getLocation(), missile.getLocation(), 1000)) {
                    useShipSystem = true;
                    break;
                }
            }
            if (useShipSystem){
                for (ShipAPI member : ship.getWing().getWingMembers()){
                    if (member.getSystem().getAmmo() != 0) member.useSystem();
                }
            }
        }
    }
}
