package data.campaign.econ;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import java.awt.*;

public class vic_antiEVCt1 extends BaseIndustry {

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
    }
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        float stabChange = 2;
        float hazardChange = -0.25f;
        float groundDefenceChange = 0;
        float accessibilityChange = 0.1f;
        float productionQualityChange = 0;
        float fleetSizeChange = 0;
        float growChange = 0.5f;

        float power = maxDeficitPercent();

        float opad = 10f;
        float pad = 3f;


        Color h = Misc.getHighlightColor();

        tooltip.addPara("Has the following effect on Extreme Viral Contamination:", opad);

        tooltip.addPara("Stability: %s", opad, h, "+" + Math.round(stabChange) );
        tooltip.addPara("Hazard: %s", pad, h, "-" + Math.round(hazardChange * -100) + "%" );
        tooltip.addPara("Accessibility: %s", pad, h, "+" + Math.round(accessibilityChange * 100) + "%");
        tooltip.addPara("Decrease of growth reduction penalty: %s", pad, h,  Math.round(growChange * 100) + "%");
    }

    public void unapply() {
        super.unapply();
    }

    public boolean isAvailableToBuild() {
        boolean build = !(market.hasIndustry("vic_antiEVCt1") || market.hasIndustry("vic_antiEVCt2") || market.hasIndustry("vic_antiEVCt3"));
        build = build && market.hasCondition("VIC_VBomb_scar");
        return market.hasCondition("VIC_VBomb_scar");
    }

    @Override
    public boolean canUpgrade() {
        return super.canUpgrade();
    }

    public boolean showWhenUnavailable() {
        return false;
    }

}



























