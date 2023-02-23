package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.alliances.Alliance;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class vic_starterPlugin extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        Global.getSettings().resetCached();
        if (Global.getSettings().getMissionScore("VIC_LiberationOfMind") > 0) {
            for (int i = 1; i <= NexConfig.getFactionConfig("vic").getStartFleetSet(NexFactionConfig.StartFleetType.SUPER.name()).getNumFleets(); i++) {
                if ("vic_stolas_hunter".equals(NexConfig.getFactionConfig("vic").getStartFleetSet(NexFactionConfig.StartFleetType.SUPER.name()).getFleet(i).get(0))) {break;}
                if ((i) == NexConfig.getFactionConfig("vic").getStartFleetSet(NexFactionConfig.StartFleetType.SUPER.name()).getNumFleets()) {
                    List<String> indomitableFleet = new ArrayList<>(2);
                    indomitableFleet.add("vic_stolas_hunter");
                    indomitableFleet.add("vic_buffalo_vic_standard");
                    NexConfig.getFactionConfig("vic").getStartFleetSet(NexFactionConfig.StartFleetType.SUPER.name()).addFleet(indomitableFleet);
                    break;
                }
            }
        }
        //NexConfig.getFactionConfig("vic").alignments.put(Alliance.Alignment.CORPORATE, 5f);
        //Global.getLogger(vic_starterPlugin.class).info("executed");
        return true;
    }
}
