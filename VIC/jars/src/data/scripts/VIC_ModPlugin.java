package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.plugins.timer.VIC_TimeTracker;
import data.scripts.plugins.vic_brandEngineUpgradesDetectionRange;
import data.scripts.weapons.ai.VIC_SwarmMirvAI;
import data.scripts.weapons.ai.vic_disruptorShot_AI;
import data.scripts.weapons.ai.vic_gaganaStuckAI;
import data.scripts.weapons.ai.vic_verlioka;
import data.world.VICGen;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;


public class VIC_ModPlugin extends BaseModPlugin {

    public static final String
            VERLIOKA = "vic_verlioka_shot",
            DISRUPTOR = "vic_disruptorShot_mie",
            ABYSSAL = "vic_abyssalfangs_srm",
            GAGANA = "vic_gaganaShot_sub";

    public static boolean hasShaderLib;

    @Override
    public void onApplicationLoad() {
        hasShaderLib = Global.getSettings().getModManager().isModEnabled("shaderLib");

        if (hasShaderLib) {
            ShaderLib.init();
            if (ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed()) {
                LightData.readLightDataCSV("data/lights/vic_light_data.csv");
                //TextureData.readTextureDataCSV("data/lights/vic_texture_data.csv");
            }
        }
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case VERLIOKA:
                return new PluginPick<MissileAIPlugin>(new vic_verlioka(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case DISRUPTOR:
                return new PluginPick<MissileAIPlugin>(new vic_disruptorShot_AI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ABYSSAL:
                return new PluginPick<MissileAIPlugin>(new VIC_SwarmMirvAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case GAGANA:
                return new PluginPick<MissileAIPlugin>(new vic_gaganaStuckAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }


    @Override
    public void onNewGame() {
        //Nex compatibility setting, if there is no nex or corvus mode(Nex), just generate the system
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()) {
            new VICGen().generate(Global.getSector());
        }

        if (!Global.getSector().hasScript(VIC_TimeTracker.class)) {
            Global.getSector().addScript(new VIC_TimeTracker());
            Global.getSector().addScript(new vic_brandEngineUpgradesDetectionRange());
        }
    }

}