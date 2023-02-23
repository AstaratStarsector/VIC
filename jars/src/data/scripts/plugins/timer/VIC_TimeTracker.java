package data.scripts.plugins.timer;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import data.campaign.intel.VIC_declineIntel;

import java.util.ArrayList;

public class VIC_TimeTracker implements EveryFrameScript {

    public boolean firstTick = true;
    public int lastDayChecked = 0;

    public VIC_TimeTracker() {
    }

    //debug-Logger
    private static void debugMessage(String Text) {
        boolean DEBUG = Global.getSettings().isDevMode(); //set to false once done
        if (DEBUG) {
            Global.getLogger(VIC_TimeTracker.class).info(Text);
        }
    }

    public void advance(float amount) {
        if (newDay()) {
            debugMessage("newDay");
            doVirusThings(checkVbomb());
        }
    }

    public  ArrayList<MarketAPI> checkVbomb() {

        ArrayList<MarketAPI> MarketsWithScar = new ArrayList<>();

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.hasCondition("VIC_VBomb_scar")) {
                MarketsWithScar.add(market);
                debugMessage(market.getName() + "are contaminated");
            }
        }
        if (MarketsWithScar.isEmpty()) debugMessage("All markets clear");
        return MarketsWithScar;
    }

    public void doVirusThings(ArrayList<MarketAPI> MarketsWithScar) {
        for (MarketAPI market : MarketsWithScar) {
            float lowPopSize = (float) ((Math.pow(2f, market.getSize() - 2f)) * 150f);
            Global.getLogger(VIC_TimeTracker.class).info("Vbomb " + market.getName() + " goest down at " + lowPopSize + " / " + " current " + market.getPopulation().getWeight().getModifiedValue());

            float total = market.getIncoming().getWeight().getModifiedValue();

            if (market.getFactionId().equals("vic")){
                if (!(market.hasIndustry("vic_antiEVCt1") || market.hasIndustry("vic_antiEVCt2") || market.hasIndustry("vic_antiEVCt3"))){
                    market.addIndustry("vic_antiEVCt1");
                    market.getIndustry("vic_antiEVCt1").startBuilding();
                } else if (market.hasIndustry("vic_antiEVCt1")){
                    market.getIndustry("vic_antiEVCt1").startUpgrading();
                } else if (market.hasIndustry("vic_antiEVCt2")){
                    market.getIndustry("vic_antiEVCt2").startUpgrading();
                }
            }
            if (market.hasIndustry("vic_antiEVCt2") || market.hasIndustry("vic_antiEVCt3")) continue;
            if (market.getPopulation().getWeight().getModifiedValue() <= lowPopSize && total < 0) {
                if (market.getSize() >= 3) {
                    market.setSize(market.getSize() - 1);
                    IntelInfoPlugin intel = new VIC_declineIntel(market, market.getSize());
                    Global.getSector().addScript((EveryFrameScript) intel);
                    Global.getSector().getIntelManager().addIntel(intel);
                } else if (market.getSize() == 2) {
                    DecivTracker.decivilize(market, true);
                }
            }
        }
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    private boolean newDay() {
        CampaignClockAPI clock = Global.getSector().getClock();
        if (firstTick) {
            lastDayChecked = clock.getDay();
            firstTick = false;
            return false;
        } else if (clock.getDay() != lastDayChecked) {
            lastDayChecked = clock.getDay();
            return true;
        }
        return false;
    }

}