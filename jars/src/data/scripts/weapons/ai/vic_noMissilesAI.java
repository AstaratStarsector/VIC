package data.scripts.weapons.ai;

import com.fs.starfarer.api.combat.*;

public class vic_noMissilesAI implements MissileAIPlugin, GuidedMissileAI {

    private CombatEntityAPI target;
    private final MissileAPI missile;

    public vic_noMissilesAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
    }

    @Override
    public void advance(float amount) {
        missile.getVelocity().scale(0);
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
}