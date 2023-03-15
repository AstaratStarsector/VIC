package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.industry.Brewery;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import static com.fs.starfarer.api.alcoholism.memory.AlcoholRepo.ALCOHOL_MAP;
import static data.campaign.ids.vic_Items.GENETECH;

public class vic_addBooze {
    public static void addBooze () {

        Global.getSettings().getHullModSpec("vic_booze").setEffectClass("data.hullmods.vic_booze");
        ALCOHOL_MAP.put("vic_booze", new Alcohol("vic_booze", 2.5f, "vic", -5, -5, Commodities.ORGANICS, GENETECH));


    }

    public static void addBrewery(MarketAPI market){

        market.addIndustry(Brewery.INDUSTRY_ID);
        market.getIndustry(Brewery.INDUSTRY_ID).setSpecialItem(new SpecialItemData(AlcoholRepo.get("vic_booze").getIndustryItemId(), null));
    }
}
