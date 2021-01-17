package data.scripts.plugins.timer;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.util.*;

//Copy of SirHartley's IndEvo code
public class VIC_TimeTracker implements EveryFrameScript {

    private static ArrayList<ArrayList<Object>> tagTrackers = new ArrayList<>();
    public boolean firstTick = true;
    public int lastDayChecked = 0;

    public VIC_TimeTracker() {
    }

    //debug-Logger
    private static void debugMessage(String Text) {
        boolean DEBUG = true; //set to false once done
        if (DEBUG) {
            Global.getLogger(VIC_TimeTracker.class).info(Text);
        }
    }

    public static void addMarketTimeTagTracker(MarketAPI market, String ident) {
        ArrayList<Object> l = new ArrayList<>();
        l.add(market);
        l.add(ident);

        if (!marketHasTimeTag(market, ident)) {
            market.addTag(ident + 0);
        }

        tagTrackers.add(l);
    }

    public static void removeMarketTimeTagTracker(MarketAPI market, String ident) {
        for (Iterator<ArrayList<Object>> i = tagTrackers.iterator(); i.hasNext(); ) {
            ArrayList<Object> l = i.next();

            if (l.get(0) == market && l.get(1) == ident) {
                tagTrackers.remove(l);
                break;
            }
        }
    }

    public static int getTimeTagPassed(MarketAPI market, String ident) {
        int timePassed = 0;
        for (String s : market.getTags()) {
            if (s.contains(ident)) {
                timePassed = Integer.parseInt(s.substring(ident.length()));
                break;
            }
        }
        return timePassed;
    }

    public static boolean marketHasTimeTag(MarketAPI market, String ident) {
        for (String s : market.getTags()) {
            if (s.contains(ident)) {
                return true;
            }
        }
        return false;
    }

    public void advance(float amount) {
        if (newDay()) {
            debugMessage("newDay");
            updateMarketTagTimePassed();
        }
    }

    public Object getMap (String key){
        if (!Global.getSector().getMemory().contains(key)){
            Map<String, Integer> map = new HashMap<>();
            Global.getSector().getMemory().set(key, map);
        }
        return Global.getSector().getMemory().get(key);
    }

    public int addDayToCounter (String MarketId){
        String key = "Vbomb_daysPast";
        Map<String, Integer> map = (Map<String, Integer>) getMap(key);
        if (map.containsKey(MarketId)){
            map.put(MarketId, map.get(MarketId) + 1);
        } else {
            map.put(MarketId,  1);
        }
        return map.get(MarketId);
    }

    public void removeMarket(String MarketId){
        String key = "Vbomb_daysPast";
        Map<String, Integer> map = (Map<String, Integer>) getMap(key);
        map.remove(MarketId);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    private void onNewDay() {
        List<VIC_newDayListener> list = Global.getSector().getListenerManager().getListeners(VIC_newDayListener.class);

        for (Iterator<VIC_newDayListener> i = list.iterator(); i.hasNext(); ) {
            VIC_newDayListener x = i.next();
            debugMessage("running OnNewDay for " + x.getClass().getName());
            x.onNewDay();
        }
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

    private void updateMarketTagTimePassed() {
        for (ArrayList<Object> l : tagTrackers) {
            if (l.size() > 2) {
                continue;
            }

            MarketAPI market = (MarketAPI) l.get(0);
            String ident = (String) l.get(1);

            for (String tag : market.getTags()) {
                if (tag.contains(ident)) {
                    int timePassed = getTimeTagPassed(market, ident) + 1;
                    market.removeTag(tag);
                    market.addTag(ident + timePassed);
                    break;
                }
            }
        }
    }
}