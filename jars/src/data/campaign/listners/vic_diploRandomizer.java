package data.campaign.listners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import exerelin.utilities.NexConfig;

import java.util.List;

import static exerelin.campaign.diplomacy.DiplomacyTraits.MEM_KEY_RANDOM_TRAITS;

public class vic_diploRandomizer extends BaseCampaignEventListener {

    public vic_diploRandomizer(boolean permaRegister) {
        super(permaRegister);
    }

    public void reportEconomyTick(int iterIndex) {
        Global.getLogger(vic_diploRandomizer.class).info("check factions");
        for (FactionAPI faction : Global.getSector().getAllFactions()){
            Global.getLogger(vic_diploRandomizer.class).info(faction.getDisplayName());
            boolean hasTrait = NexConfig.getFactionConfig(faction.getId()).diplomacyTraits.contains("vic_schizo");
            if (!hasTrait && faction.getMemoryWithoutUpdate().contains(MEM_KEY_RANDOM_TRAITS)){
                if(((List<String>) faction.getMemoryWithoutUpdate().get(MEM_KEY_RANDOM_TRAITS)).contains("vic_schizo")){
                    hasTrait = true;
                }
            }
            if (hasTrait) {
                Global.getLogger(vic_diploRandomizer.class).info("has Trait");
                if (Global.getSector().getFaction(faction.getId()).getMemoryWithoutUpdate().contains("$nex_alignments"))
                    Global.getLogger(vic_diploRandomizer.class).info("has alignments key");
            }

        }

    }
}
