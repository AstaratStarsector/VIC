package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

import java.awt.*;
import java.util.EnumSet;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.vic_combatPlugin.LocalData;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;

public class vic_layerRenderPlugin extends BaseCombatLayeredRenderingPlugin {

    String DATA_KEY;
    CombatEngineAPI engine;

    vic_layerRenderPlugin(String dataKey, CombatEngineAPI engine){
        DATA_KEY = dataKey;
        this.engine = engine;
    }

    SpriteAPI printer = Global.getSettings().getSprite("weapons", "vic_rock_printer");
    SpriteAPI holo = Global.getSettings().getSprite("weapons", "vic_rock_holo");
    SpriteAPI missile = Global.getSettings().getSprite("weapons", "vic_rock_missile");
    SpriteAPI glow = Global.getSettings().getSprite("weapons", "vic_rokh_launcher_glow_under");
    {
        glow.setAdditiveBlend();
    }
    SpriteAPI glowHead = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
    {
        glowHead.setSize(9,27);
        glowHead.setCenter(4.5f,13.5f);
        glowHead.setAdditiveBlend();
        glowHead.setColor(new Color(245, 104, 4,255));
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        for (vic_combatPlugin.spriteRender renderData : localData.spritesRender){
            if (renderData.layer == layer){

                renderData.sprite.renderAtCenter(renderData.location.x + renderData.velocity.x * renderData.timePast,renderData.location.y + renderData.velocity.y * renderData.timePast);
            }
        }

        if (layer == CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER){
            for (vic_combatPlugin.rokhAnimationData data : localData.rokhs){
                WeaponAPI weapon = data.weapon;
                if (weapon == null) continue;
                missile.setAngle(weapon.getCurrAngle() - 90);
                holo.setAngle(weapon.getCurrAngle() - 90);
                holo.setAlphaMult(data.holoAlpha);
                holo.setAdditiveBlend();
                if (weapon.getAmmo() == 0){
                    float progress = weapon.getAmmoTracker().getReloadProgress();
                    if (progress >= 0.95f){
                        missile.renderAtCenter(weapon.getLocation().x,weapon.getLocation().y);
                    } else if (progress >= 0.15f){
                        progress = MagicAnim.smooth((progress - 0.15f) / (1 - 0.2f));

                        holo.renderAtCenter(weapon.getLocation().x,weapon.getLocation().y);
                        missile.renderRegionAtCenter(weapon.getLocation().x,weapon.getLocation().y, 0,1 ,1f ,-progress);
                    } else if (progress >= 0.05f){
                        progress = MagicAnim.smooth((progress - 0.05f)/ (0.1f));

                        holo.renderRegionAtCenter(weapon.getLocation().x,weapon.getLocation().y, 0, 0 ,1f ,progress);
                    }
                } else {
                    missile.renderAtCenter(weapon.getLocation().x,weapon.getLocation().y);
                }

                if (data.glowAlpha > 0){
                    glow.setAngle(weapon.getCurrAngle() - 90);
                    glow.setAlphaMult(data.glowAlpha);
                    glow.renderAtCenter(data.printerPos.x,data.printerPos.y);
                }
                if (data.headAlpha > 0){
                    glowHead.setAngle(weapon.getCurrAngle() - 90);
                    glowHead.renderAtCenter(data.headPos.x,data.headPos.y);
                }
                printer.setAngle(weapon.getCurrAngle() - 90);
                printer.renderAtCenter(data.printerPos.x,data.printerPos.y);
            }
        }
    }

    public float getRenderRadius() {
        return 9.9999999E14F;
    }

    public EnumSet<CombatEngineLayers> getActiveLayers() {
        EnumSet<CombatEngineLayers> set = EnumSet.noneOf(CombatEngineLayers.class);
        set.add(CombatEngineLayers.BELOW_SHIPS_LAYER);
        set.add(CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER);
        set.add(CombatEngineLayers.ABOVE_SHIPS_LAYER);
        return set;
        //return EnumSet.allOf(CombatEngineLayers.class);
    }


}
