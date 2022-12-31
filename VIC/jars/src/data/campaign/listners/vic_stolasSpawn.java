package data.campaign.listners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class vic_stolasSpawn extends BaseCampaignEventListener {

    public vic_stolasSpawn(boolean permaRegister) {
        super(permaRegister);
    }

    WeightedRandomPicker<String> variants = new WeightedRandomPicker<>();

    {
        variants.add("vic_stolas_standart");
        variants.add("vic_stolas_hunter");
        variants.add("vic_stolasPlasma", 0.5f);
        variants.add("vic_stolas_gauss", 0.25f);
        variants.add("vic_stolas_crusher", 0.25f);
    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        String factionID = fleet.getFaction().getId();
        if (factionID.equals("vic")) {
            for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()) {
                if ((ship.isFlagship() && ship.getHullSpec().getFleetPoints() >= 15 && Math.random() >= 0.9f)) {
                    ship.setVariant(Global.getSettings().getVariant(variants.pick()), false, false);
                    ship.setVariant(ship.getVariant().clone(), false, false);
                    ship.getVariant().addTag("no_autofit");
                    ship.getCaptain().setPersonality(Personalities.RECKLESS);
                    ship.getCaptain().setPortraitSprite("graphics/portraits/vic_acehole4.png");
                    ship.getRepairTracker().setCR(ship.getRepairTracker().getMaxCR());
                    fleet.getFleetData().sort();
                }
            }
        }
    }
}
