package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class vic_orbitalLazor extends BaseMarketConditionPlugin {

    public List<String> lazorCommodities = new ArrayList<>();

    {
        lazorCommodities.add(Commodities.ORE);
        lazorCommodities.add(Commodities.RARE_ORE);
    }

    protected Set<String> getAffectedCommodities(Industry industry) {
        MarketAPI market = industry.getMarket();

        Set<String> result = new LinkedHashSet<String>();
        for (MarketConditionAPI mc : market.getConditions()) {
            String cid = mc.getId();
            String commodity = ResourceDepositsCondition.COMMODITY.get(cid);
            for (String curr : lazorCommodities) {
                if (curr.equals(commodity)) {
                    result.add(curr);
                }
            }
        }
        return result;
    }

    @Override
    public void apply(String id) {
        super.apply(id);
        for (Industry industry : market.getIndustries()) {
            if (industry instanceof BaseIndustry) {
                BaseIndustry b = (BaseIndustry) industry;
                Set<String> list = getAffectedCommodities(industry);

                if (!list.isEmpty()) {
                    for (String curr : list) {
                        b.supply(condition.getId(), curr, 1, Misc.ucFirst(condition.getName().toLowerCase()));
                    }
                }
            }
        }
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(getModId(), 500, condition.getName());
        market.getHazard().modifyFlat(id, 0.25f, getName());
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        for (Industry industry : market.getIndustries()) {
            if (industry instanceof BaseIndustry) {
                BaseIndustry b = (BaseIndustry) industry;

                for (String curr : lazorCommodities) {
                    b.supply(condition.getId(), curr, 0, null);
                }
            }
        }
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyFlat(getModId());
        market.getHazard().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        List<String> commodities = new ArrayList<String>();
        for (String curr : lazorCommodities) {
            CommoditySpecAPI c = Global.getSettings().getCommoditySpec(curr);
            commodities.add(c.getName().toLowerCase());
        }
        String comoddities = Misc.getAndJoined(commodities);
        tooltip.addPara("Increases " + comoddities + " production by %s units.", 5, Misc.getHighlightColor(), 1 + "");
        tooltip.addPara("Increases ground defences by %s.", 5, Misc.getHighlightColor(), 500 + "");
        tooltip.addPara("Increases hazard rating by %s.", 5, Misc.getHighlightColor(), 25 + "%");
    }
}
