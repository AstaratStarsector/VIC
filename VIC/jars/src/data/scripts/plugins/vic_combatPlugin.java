package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public  class vic_combatPlugin extends BaseEveryFrameCombatPlugin {

    //verlioka
    private static final Map<MissileAPI , Float> debuffed_missiles = new HashMap<>();
    private static final Map<ShipAPI, Float> debuffed_fighters = new HashMap<>();
    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.25f);

    @Override
    public void init(CombatEngineAPI engine) {
        super.init(engine);
        debuffed_missiles.clear();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        super.advance(amount, events);

        //verlioka
        for (Iterator<Map.Entry< MissileAPI , Float >> iter = debuffed_missiles.entrySet().iterator(); iter.hasNext();) {
            Map.Entry< MissileAPI , Float > entry = iter.next();
            if(entry.getKey().getHitpoints() <= 0 || entry.getValue()-amount<0){
                iter.remove();
            } else {
                entry.setValue(entry.getValue()-amount);
                entry.getKey().setJitter(entry.getKey(), new Color(44, 255, 255), 3, 6, 1);
                entry.getKey().getVelocity().scale(0.79f);
                Global.getCombatEngine().applyDamage(entry.getKey(), entry.getKey().getLocation(), 85 * amount, DamageType.FRAGMENTATION, 0f, false, false, null);
            }
        }
        timer.advance(amount);
        if (timer.intervalElapsed()){
            for (Map.Entry<ShipAPI, Float> entry : debuffed_fighters.entrySet()) {
                Global.getCombatEngine().applyDamage(entry.getKey(), entry.getKey().getLocation(), 85 * 0.25f, DamageType.FRAGMENTATION, 0f, false, false, null);
            }
        }
        for (Iterator<Map.Entry< ShipAPI , Float >> iter = debuffed_fighters.entrySet().iterator(); iter.hasNext();) {
            Map.Entry< ShipAPI , Float > entry = iter.next();
            if(!entry.getKey().isAlive() || entry.getValue()-amount<0){
                iter.remove();
                entry.getKey().getMutableStats().getMaxSpeed().unmodify("vic_virlioka");
                entry.getKey().getMutableStats().getAcceleration().unmodify("vic_virlioka");
                entry.getKey().getMutableStats().getDeceleration().unmodify("vic_virlioka");
            } else {
                entry.setValue(entry.getValue()-amount);
                entry.getKey().setJitterUnder(entry.getKey(), new Color(44, 255, 255), 4, 8, 2);
                entry.getKey().getMutableStats().getMaxSpeed().modifyMult("vic_virlioka", 0.65f);
                entry.getKey().getMutableStats().getAcceleration().modifyMult("vic_virlioka", 0.65f);
                entry.getKey().getMutableStats().getDeceleration().modifyMult("vic_virlioka", 0.65f);
            }
        }
    }

    public static void addToListM(MissileAPI missile){
        debuffed_missiles.put(missile, 2f);
    }

    public static void addToListF(ShipAPI fighter){
        debuffed_fighters.put(fighter, 2f);
    }


}