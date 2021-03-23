package data.campaign.econ;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.util.Pair;

public class vic_tourism extends BaseIndustry {

    public static final float BASE_ACCESSIBILITY = 0.1F;
    public static final float ALPHA_CORE_ACCESSIBILITY = 0.2F;
    public static float MULT_PER_DEFICIT = 0.25F;


    private int getMaxDeficit() {
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

    public void apply() {
        super.apply(true);
        int size = this.market.getSize();

        demand(Commodities.FOOD, size + 1);
        demand(Commodities.DOMESTIC_GOODS, size);
        demand(Commodities.LUXURY_GOODS, size - 1);



        income.modifyMult(getModId(), 1 - (maxDeficitPercent()), "Deficit");

        if (market.hasCondition(Conditions.RECENT_UNREST)) {
            income.modifyMult(getModId(), 0, "Closed due to unrest");
        } else if (market.getStabilityValue() < 10.0f) {
            float reduction = market.getStabilityValue() < 6 ? 0 : (0.25f * (market.getStabilityValue() - 6));

            income.modifyMult(getModId(), reduction, "Instability");
        }
    }

    public void unapply() {
        super.unapply();
        MemoryAPI memory = this.market.getMemoryWithoutUpdate();
        income.unmodify(getModId());
    }

    public boolean isAvailableToBuild() {
        return false;
    }

    public boolean showWhenUnavailable() {
        return false;
    }

}



























