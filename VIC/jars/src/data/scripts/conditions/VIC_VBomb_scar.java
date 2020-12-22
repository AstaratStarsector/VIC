package data.scripts.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.timer.VIC_TimeTracker;

import java.awt.*;


public class VIC_VBomb_scar extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

    public float
            STABILITY_PENALTY = 10,
            hazardRating = 2f,
            DefMult = 0.3f,
            PopGrow = -10f,
            year = 365,
            Access = -0.25f;

    public int daysPassed = 1;

    public Color toxinColor = new Color(121, 182, 5, 164);

    public void apply(String id) {
        super.apply(id);

        if (daysPassed >= year) market.getStability().unmodify(id);
        else market.getStability().modifyFlat(id, -STABILITY_PENALTY, getName());
        if (!market.getMemoryWithoutUpdate().getBoolean("$VirusTakeThem") && daysPassed >= year) {
            market.setSize(market.getSize() - 1);
            market.getMemoryWithoutUpdate().set("$VirusTakeThem", true);
        }

        market.getHazard().modifyFlat(id, hazardRating, getName());
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DefMult, getName());
        market.getAccessibilityMod().modifyFlat(id, Access, getName());
        for (Industry industry : market.getIndustries()) {
            if (industry.isIndustry()) {
                for (MutableCommodityQuantity supply : industry.getAllSupply()) {
                    industry.getSupply(supply.getCommodityId()).getQuantity().modifyFlat(getModId(), -1, getName());
                }
            }
        }
        market.addTransientImmigrationModifier(this);

        if (!Global.getSector().getListenerManager().hasListener(this)) {
            Global.getSector().getListenerManager().addListener(this, true);
        }

        if (market.getPlanetEntity() != null){
            PlanetAPI planet =  market.getPlanetEntity();
            planet.getSpec().setAtmosphereColor(toxinColor);
            planet.getSpec().setCloudTexture("graphics/planets/clouds_banded01.png");
            planet.getSpec().setCloudColor(toxinColor);
            planet.applySpecChanges();
        }

        VIC_TimeTracker.addMarketTimeTagTracker(market, getModId());
        daysPassed = VIC_TimeTracker.getTimeTagPassed(market, getModId());

    }

    public void unapply(String id) {
        super.unapply(id);
        market.getStability().unmodify(id);
        market.getHazard().unmodify(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.getAccessibilityMod().unmodify();
        market.removeTransientImmigrationModifier(this);

        for (Industry industry : market.getIndustries()) {
            if (industry.isIndustry()) {
                for (MutableCommodityQuantity supply : industry.getAllSupply()) {
                    industry.getSupply(supply.getCommodityId()).getQuantity().unmodify(getModId());
                }
            }
        }

        if (market.getPlanetEntity() == null)return;
        PlanetAPI planet =  market.getPlanetEntity();
        planet.getSpec().setAtmosphereColor(new Color(255, 255, 255, 253));
        Global.getSector().getListenerManager().removeListener(this);
        VIC_TimeTracker.removeMarketTimeTagTracker(market, getModId());
    }

    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        if (daysPassed <= year){
            tooltip.addPara("%s stability for another %s days.",
                    10f, Misc.getHighlightColor(),
                    "" + (int) -STABILITY_PENALTY, Math.round(year - daysPassed) + "");
        }
        tooltip.addPara("%s hazard rating.",
                10f, Misc.getHighlightColor(),
                "+" + (int)(hazardRating * 100) + "%");
        tooltip.addPara("%s ground defences.",
                10f, Misc.getHighlightColor(),
                "x" + DefMult);
        tooltip.addPara("%s population growth.",
                10f, Misc.getHighlightColor(),
                "" + (int)PopGrow);
        tooltip.addPara("%s accessibility.",
                10f, Misc.getHighlightColor(),
                (int)(Access * 100) + "%");
        tooltip.addPara("%s output of all industries on this colony", 10f, Misc.getHighlightColor(), -1 + "");
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.getWeight().modifyFlat(getModId(), PopGrow, getName());
    }

    public void onNewDay() {
        daysPassed = VIC_TimeTracker.getTimeTagPassed(market, getModId());
        if (daysPassed == Math.round(year/2)) market.setSize(market.getSize() - 1);
        if (daysPassed == year) market.setSize(market.getSize() - 1);
        if (market.getSize() == 0) DecivTracker.decivilize(market, true);
    }

}





