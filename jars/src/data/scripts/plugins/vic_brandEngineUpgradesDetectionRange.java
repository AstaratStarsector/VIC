package data.scripts.plugins;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.HashMap;
import java.util.Map;

public class vic_brandEngineUpgradesDetectionRange implements EveryFrameScript {

    private final IntervalUtil timer = new IntervalUtil(10f, 10f);
    private float
            total = 1;
    public final Map<ShipAPI.HullSize, Float> MULT = new HashMap<>();
    {
        MULT.put(ShipAPI.HullSize.FRIGATE, 0.25F);
        MULT.put(ShipAPI.HullSize.DESTROYER, 0.5F);
        MULT.put(ShipAPI.HullSize.CRUISER, 0.75F);
        MULT.put(ShipAPI.HullSize.CAPITAL_SHIP, 1.5F);
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        timer.advance(amount);
        if (timer.intervalElapsed()){
            total = 0;
            for (FleetMemberAPI s : player.getFleetData().getMembersListCopy()) {
                if (s.getVariant().hasHullMod("vic_brandengineupgrades")) {
                    total += MULT.get(s.getHullSpec().getHullSize());
                }
            }
            if (total > 40) total = 40;
        }
        String ID = "vic_brandEngineUpgrades";
        player.getStats().getDetectedRangeMod().modifyPercent(ID, total, "VIC engines");

    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
