package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.ids.vic_Items;
import data.scripts.utilities.StringHelper;

import java.awt.*;

public class vic_antiEVCt3 extends BaseIndustry {

    public static float MULT_PER_DEFICIT = 0.25F;


    public int getMaxDeficit() {
        int max = -Integer.MAX_VALUE;
        for (Pair<String, Integer> p : getAllDeficit()) {
            if (p.two > max) max = p.two;
        }

        return Math.max(max, 0);
    }

    public float maxDeficitPercent() {
        float max = 0;
        for (Pair<String, Integer> p : getAllDeficit()) {
            float percent = p.two / getDemand(p.one).getQuantity().getModifiedValue();
            if (percent > max) max = percent;
        }

        return Math.max(max, 0);
    }

    public boolean isQueued (){
        if (currTooltipMode == null) return false;
        return currTooltipMode == IndustryTooltipMode.QUEUED;
    }

    public void apply() {
        super.apply(true);
        int size = this.market.getSize();
        demand(Commodities.ORGANICS, size);
        demand(Commodities.MARINES, size);
        demand(Commodities.ORGANS, size - 2);
        demand(vic_Items.GENETECH, Math.round(size * 0.5f));
    }

    public void unapply() {
        super.unapply();
    }

    @Override
    public String getUnavailableReason() {
        if (market.getFaction().getRelationship("vic") < 0.25f){
            return StringHelper.getString("VIC_Industries", "antiEVCt3LowRep");
        }
        return super.getUnavailableReason();
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        float growChange = 0.995f;

        float power = maxDeficitPercent();

        float opad = 10f;
        float pad = 3f;


        Color h = Misc.getHighlightColor();

        tooltip.addPara("Has the following effect on Extreme Viral Contamination:", opad);

        tooltip.addPara("Decrease of growth reduction penalty: %s", opad, h,  (growChange * 100) + "%");
        tooltip.addPara("All others penalties negated", pad);

        tooltip.addPara("Colony cant decline in size due to EVC.", opad);

        tooltip.addPara("Colony cant decline in size due to EVC even if this building is disabled or has a shortage.", opad);
        if (power > 0){
            tooltip.addPara("Efficiency reduced by %s due to shortages.", opad, Misc.getNegativeHighlightColor(), Math.round(power * 100) + "%");
        }

    }

    public boolean isAvailableToBuild() {
        return market.getFaction().getRelationship("vic") >= 0.25f;
    }

    public boolean showWhenUnavailable() {
        return false;
    }

}



























