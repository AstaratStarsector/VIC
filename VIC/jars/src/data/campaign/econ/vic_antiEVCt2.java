package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import java.awt.*;

public class vic_antiEVCt2 extends BaseIndustry {

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
    }

    public void unapply() {
        super.unapply();
    }

    @Override
    public boolean canUpgrade() {
        return market.getFaction().getRelationship("vic") >= 0.25f;
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        float stabChange = 4;
        float hazardChange = -0.50f;
        float groundDefenceChange = 0.25f;
        float accessibilityChange = 0.2f;
        float productionQualityChange = 0.15f;
        float fleetSizeChange = 0.25f;
        float growChange = 0.95f;

        float power = maxDeficitPercent();

        float opad = 10f;
        float pad = 3f;


        Color h = Misc.getHighlightColor();

        tooltip.addPara("Has the following effect on Extreme Viral Contamination:", opad);

        tooltip.addPara("Stability: %s", opad, h, "+" + Math.round(stabChange) );
        tooltip.addPara("Accessibility: %s", pad, h, "+" + Math.round(accessibilityChange * 100) + "%");
        tooltip.addPara("Hazard: %s", pad, h, "-" + Math.round(hazardChange * -100) + "%" );
        tooltip.addPara("Ground defences from %s to %s", pad, h, "x0.25", "x" + (0.25f + groundDefenceChange));
        tooltip.addPara("Fleet size from %s to %s", pad, h, "x0.5", "x" + (0.5f + fleetSizeChange));
        tooltip.addPara("Production quality: %s", pad, h, "+" + Math.round(productionQualityChange * 100) + "%" );
        tooltip.addPara("Decrease of growth reduction penalty: %s", pad, h,  Math.round(growChange * 100) + "%");

        tooltip.addPara("Colony cant decline in size due to EVC even if this building is disabled or has a shortage.", opad);
        if (power > 0){
            tooltip.addPara("Efficiency reduced by %s due to shortages.", opad, Misc.getNegativeHighlightColor(), Math.round(power * 100) + "%");
        }
    }

    public boolean isAvailableToBuild() {
        return true;
    }

    public boolean showWhenUnavailable() {
        return false;
    }

}



























