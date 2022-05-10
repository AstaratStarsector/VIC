package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;

import java.util.EnumSet;

import data.scripts.plugins.vic_combatPlugin.LocalData;

public class vic_layerRenderPlugin extends BaseCombatLayeredRenderingPlugin {

    String DATA_KEY;
    CombatEngineAPI engine;

    vic_layerRenderPlugin(String dataKey, CombatEngineAPI engine){
        DATA_KEY = dataKey;
        this.engine = engine;
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        for (vic_combatPlugin.spriteRender renderData : localData.spritesRender){
            if (renderData.layer == layer){

                renderData.sprite.renderAtCenter(renderData.location.x + renderData.velocity.x * renderData.timePast,renderData.location.y + renderData.velocity.y * renderData.timePast);
            }
        }
    }

    public float getRenderRadius() {
        return 9.9999999E14F;
    }

    public EnumSet<CombatEngineLayers> getActiveLayers() {
        EnumSet<CombatEngineLayers> set = EnumSet.noneOf(CombatEngineLayers.class);
        set.add(CombatEngineLayers.BELOW_SHIPS_LAYER);
        set.add(CombatEngineLayers.ABOVE_SHIPS_LAYER);
        return set;
        //return EnumSet.allOf(CombatEngineLayers.class);
    }


}
