package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.AICoreDescriptionMode;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.CommRelayCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.submarkets.LocalResourcesSubmarketPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;


public class vic_biolabs extends BaseIndustry {



    public void apply() {
        super.apply(true);
        int size = this.market.getSize();
        demand(Commodities.ORGANICS, size);
        demand(Commodities.CREW, size);
        supply(Commodities.ORGANS, size - 1);
        supply(Commodities.MARINES, size -1 );
        String desc = this.getNameForModifier();
        Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS);
        applyDeficitToProduction(1, deficit, Commodities.ORGANS);

    }


    public void unapply() {
        super.unapply();
        MemoryAPI memory = this.market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, "$population", this.getModId(), false, -1.0F);
    }



    protected void addPostDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        if (this.market.isPlayerOwned()) {
            float opad = 10.0F;
        }
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || this.isFunctional();
    }


    public String getRouteSourceId() {
        return this.getMarket().getId() + "_" + "vicbiolabs";
    }

    public boolean isAvailableToBuild() {
        return false;
    }

    public boolean showWhenUnavailable() {
        return true;
    }

}


