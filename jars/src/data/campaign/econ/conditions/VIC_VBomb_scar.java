package data.campaign.econ.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.vic_antiEVCt1;
import data.campaign.econ.vic_antiEVCt2;
import data.campaign.econ.vic_antiEVCt3;


public class VIC_VBomb_scar extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

    private final antiEVCStats tier0 = new antiEVCStats();
    private final antiEVCStats tier1 = new antiEVCStats();
    private final antiEVCStats tier2 = new antiEVCStats();
    private final antiEVCStats tier3 = new antiEVCStats();
    public float
            STABILITY_PENALTY = 10,
            hazardRating = 2f,
            DefMult = 0.25f,
            qualityPenalty = -0.45f,
            fleetSizeMult = 0.5f,
            Access = -0.45f;

    {
        tier1.stabChange = 2;
        tier1.hazardChange = -0.25f;
        tier1.groundDefenceChange = 0;
        tier1.accessibilityChange = 0.1f;
        tier1.productionQualityChange = 0;
        tier1.fleetSizeChange = 0;
        tier1.growChange = 0.5f;

        tier2.stabChange = 4;
        tier2.hazardChange = -0.50f;
        tier2.groundDefenceChange = 0.25f;
        tier2.accessibilityChange = 0.2f;
        tier2.productionQualityChange = 0.15f;
        tier2.fleetSizeChange = 0.25f;
        tier2.growChange = 0.95f;

        tier3.stabChange = STABILITY_PENALTY;
        tier3.hazardChange = -hazardRating;
        tier3.groundDefenceChange = 1 - DefMult;
        tier3.accessibilityChange = -Access;
        tier3.productionQualityChange = -qualityPenalty;
        tier3.fleetSizeChange = 1 - fleetSizeMult;
        tier3.growChange = 0.995f;
    }

    public void apply(String id) {
        Industry antiEVCIndustry;

        antiEVCStats antiEVC = tier0;
        float industryPower = 1f;
        if (market.hasIndustry("vic_antiEVCt1")) {
            antiEVCIndustry = market.getIndustry("vic_antiEVCt1");
            if (antiEVCIndustry.isFunctional() && !antiEVCIndustry.isDisrupted() && !((vic_antiEVCt1) antiEVCIndustry).isQueued()) {
                antiEVC = tier1;
                industryPower -= ((vic_antiEVCt1) antiEVCIndustry).maxDeficitPercent();
            }
        }
        if (market.hasIndustry("vic_antiEVCt2")) {
            antiEVCIndustry = market.getIndustry("vic_antiEVCt2");
            if (antiEVCIndustry.isFunctional() && !antiEVCIndustry.isDisrupted() && !((vic_antiEVCt2) antiEVCIndustry).isQueued()) {
                antiEVC = tier2;
                industryPower -= ((vic_antiEVCt2) antiEVCIndustry).maxDeficitPercent();
            }
        }
        if (market.hasIndustry("vic_antiEVCt3")) {
            antiEVCIndustry = market.getIndustry("vic_antiEVCt3");
            if (antiEVCIndustry.isFunctional() && !antiEVCIndustry.isDisrupted() && !((vic_antiEVCt3) antiEVCIndustry).isQueued()) {
                antiEVC = tier3;
                industryPower -= ((vic_antiEVCt3) antiEVCIndustry).maxDeficitPercent();
            }
        }

        market.getStability().modifyFlat(id, -STABILITY_PENALTY + (antiEVC.stabChange * industryPower), getName());
        market.getHazard().modifyFlat(id, hazardRating + (antiEVC.hazardChange * industryPower), getName());
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DefMult + (antiEVC.groundDefenceChange * industryPower), getName());
        market.getAccessibilityMod().modifyFlat(id, Access + (antiEVC.accessibilityChange * industryPower), getName());
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, qualityPenalty + (antiEVC.productionQualityChange * industryPower), getName());
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(id, fleetSizeMult + (antiEVC.fleetSizeChange * industryPower), getName());

        if (antiEVC != tier3){
            for (Industry industry : market.getIndustries()) {
                if (industry.isIndustry()) {
                    for (MutableCommodityQuantity supply : industry.getAllSupply()) {
                        industry.getSupply(supply.getCommodityId()).getQuantity().modifyFlat(getModId(), -1, getName());
                    }
                }
            }
        }
        market.addTransientImmigrationModifier(this);
    }

    public void unapply(String id) {
        super.unapply(id);
        market.getStability().unmodify(id);
        market.getHazard().unmodify(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.getAccessibilityMod().unmodify(id);
        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).unmodify(id);
        market.removeTransientImmigrationModifier(this);

        for (Industry industry : market.getIndustries()) {
            if (industry.isIndustry()) {
                for (MutableCommodityQuantity supply : industry.getAllSupply()) {
                    industry.getSupply(supply.getCommodityId()).getQuantity().unmodify(getModId());
                }
            }
        }
    }

    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        tooltip.addPara("%s stability",
                10f, Misc.getHighlightColor(),
                "" + (int) -STABILITY_PENALTY);

        tooltip.addPara("%s accessibility.",
                10f, Misc.getHighlightColor(),
                (int) (Access * 100) + "%");

        tooltip.addPara("%s hazard rating.",
                10f, Misc.getHighlightColor(),
                "+" + (int) (hazardRating * 100) + "%");

        tooltip.addPara("%s ground defences.",
                10f, Misc.getHighlightColor(),
                "x" + DefMult);

        tooltip.addPara("%s fleet size.",
                10f, Misc.getHighlightColor(),
                "x" + fleetSizeMult);

        tooltip.addPara("%s production quality.",
                10f, Misc.getHighlightColor(),
                Math.round(qualityPenalty * 100) + "%");

        tooltip.addPara("%s population growth.",
                10f, Misc.getHighlightColor(),
                "" + Math.round(growReduction(market)));

        tooltip.addPara("%s output of all industries on this colony", 10f, Misc.getHighlightColor(), -1 + "");

        tooltip.addPara("Colony can decline in size if it hit 0% total growth while having negative growth rate", 10f);
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        Industry antiEVCIndustry;

        antiEVCStats antiEVC = tier0;
        float industryPower = 1f;
        if (market.hasIndustry("vic_antiEVCt1")) {
            antiEVCIndustry = market.getIndustry("vic_antiEVCt1");
            if (antiEVCIndustry.isFunctional() && !antiEVCIndustry.isDisrupted() && !((vic_antiEVCt1) antiEVCIndustry).isQueued()) {
                antiEVC = tier1;
                industryPower -= ((vic_antiEVCt1) antiEVCIndustry).maxDeficitPercent();
            }
        }
        if (market.hasIndustry("vic_antiEVCt2")) {
            antiEVCIndustry = market.getIndustry("vic_antiEVCt2");
            if (antiEVCIndustry.isFunctional() && !antiEVCIndustry.isDisrupted() && !((vic_antiEVCt2) antiEVCIndustry).isQueued()) {
                antiEVC = tier2;
                industryPower -= ((vic_antiEVCt2) antiEVCIndustry).maxDeficitPercent();
            }
        }
        if (market.hasIndustry("vic_antiEVCt3")) {
            antiEVCIndustry = market.getIndustry("vic_antiEVCt3");
            if (antiEVCIndustry.isFunctional() && !antiEVCIndustry.isDisrupted() && !((vic_antiEVCt3) antiEVCIndustry).isQueued()) {
                antiEVC = tier3;
                industryPower -= ((vic_antiEVCt3) antiEVCIndustry).maxDeficitPercent();
            }
        }

        incoming.getWeight().modifyFlat(getModId(), growReduction(market) * (1 - (antiEVC.growChange * industryPower)), getName());
    }

    public boolean isPlanetary() {
        return true;
    }

    public float growReduction(MarketAPI market) {
        return (float) (Math.pow(2f, market.getSize()) * -25);
    }

    protected static class antiEVCStats {
        float stabChange = 0f;
        float hazardChange = 0f;
        float groundDefenceChange = 0f;
        float accessibilityChange = 0f;
        float productionQualityChange = 0f;
        float fleetSizeChange = 0f;
        float growChange = 0f;
    }
}





