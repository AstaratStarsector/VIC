package data.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.ids.vic_Items;
import data.scripts.utilities.StringHelper;

import java.awt.*;

public class vic_revCenter extends BaseIndustry implements MarketImmigrationModifier {


    public float maxDeficitPercent() {
        float max = 0;
        for (Pair<String, Integer> p : getAllDeficit()) {
            float percent = p.two / getDemand(p.one).getQuantity().getModifiedValue();
            if (percent > max) max = percent;
        }

        return Math.max(max, 0);
    }

    @Override
    public boolean isFunctional() {
        if (market.getFaction().getRelationship("vic") <= -0.5f) return false;
        if (maxDeficitPercent() == 1f) return false;
        return super.isFunctional();
    }


    public void apply() {
        super.apply(true);
        int size = this.market.getSize();


        demand(Commodities.ORGANICS, size - 1);

        int genetechDemand = size;
        if (market.getFaction().getId().equals("vic")){
            genetechDemand -= 1;
        }
        demand(vic_Items.GENETECH, genetechDemand);



        upkeep.modifyMult(getModId(), 1 + (maxDeficitPercent()), "Deficit");
        income.modifyMult(getModId(), 1 - (maxDeficitPercent()), "Deficit");

        market.addTransientImmigrationModifier(this);
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {

        incoming.getWeight().modifyFlat(getModId(), this.market.getSize() * 2 * (1 - maxDeficitPercent()), "VIC Revitalization Center");
    }

    public void unapply() {
        super.unapply();
        MemoryAPI memory = this.market.getMemoryWithoutUpdate();
        income.unmodify(getModId());
        market.removeTransientImmigrationModifier(this);
    }

    public boolean isAvailableToBuild() {
        return market.getFaction().getRelationship("vic") >= -0.25f;
    }

    public boolean showWhenUnavailable() {
        return false;
    }

    @Override
    public String getUnavailableReason() {
        if (market.getFaction().getRelationship("vic") < -0.25f){
            return StringHelper.getString("VIC_Industries", "antiEVCt3LowRep");
        }
        return super.getUnavailableReason();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        float power = maxDeficitPercent();

        float opad = 10f;
        float pad = 3f;


        Color h = Misc.getHighlightColor();

        tooltip.addPara("Increases colony growth by %s", pad, h,  this.market.getSize() * 2 + "");

        tooltip.addPara("Gives access to the services of the Revitalization Center.", opad);

        /*
        if (power > 0){
            tooltip.addPara("Efficiency reduced by %s due to shortages.", opad, Misc.getNegativeHighlightColor(), Math.round(power * 100) + "%");
        }

         */

    }

}



























