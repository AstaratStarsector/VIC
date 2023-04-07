package data.scripts.plugins.SubAb;

import com.fs.starfarer.api.alcoholism.itemPlugins.RecipeItemPlugin;
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin;

public class vic_makeRecipePlugin {

    public static GenericSpecialItemPlugin makePlugin(){
        return new RecipeItemPlugin();
    }
}
