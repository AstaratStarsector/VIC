package data.campaign.econ;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.ids.vic_Items;

public class vic_biolabs extends BaseIndustry {


    public void apply() {
        super.apply(true);
        int size = this.market.getSize();



        demand(Commodities.ORGANICS, size);
        demand(Commodities.CREW, size);
        supply(Commodities.ORGANS, size - 3);
        supply(Commodities.MARINES, size - 1);

        int genetechProd = size + 2;
        if (!market.getFaction().getId().equals("vic")){
            genetechProd -= 2;
        }
        supply(vic_Items.GENETECH, genetechProd);
        String desc = this.getNameForModifier();

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS, Commodities.CREW);
        applyDeficitToProduction(1, deficit, Commodities.ORGANS);
        applyDeficitToProduction(1, deficit, vic_Items.GENETECH);
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
        return false;
    }

}


