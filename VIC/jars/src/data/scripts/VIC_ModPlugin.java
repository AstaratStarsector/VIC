package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import data.campaign.econ.vic_industries;
import data.scripts.plugins.timer.VIC_TimeTracker;
import data.scripts.plugins.timer.VIC_newDayListener;
import data.scripts.plugins.vic_brandEngineUpgradesDetectionRange;
import data.scripts.weapons.ai.VIC_SwarmMirvAI;
import data.scripts.weapons.ai.vic_disruptorShot_AI;
import data.scripts.weapons.ai.vic_gaganaStuckAI;
import  data.scripts.weapons.autofireAI.vic_VerliokaAutofireAI;
import data.world.VICGen;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;

import java.util.HashMap;
import java.util.Map;


public class VIC_ModPlugin extends BaseModPlugin {

    public static final String
            DISRUPTOR = "vic_disruptorShot_mie",
            ABYSSAL = "vic_abyssalfangs_srm",
            GAGANA = "vic_gaganaShot_sub";

    public static final String
            VERLIOKA = "vic_verlioka";

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
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        switch (weapon.getId()) {
            case VERLIOKA:
                return new PluginPick<AutofireAIPlugin>(new vic_VerliokaAutofireAI(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
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

    @Override
    public void onNewGameAfterEconomyLoad() {
        placeDryDocks();

        MarketAPI market = Global.getSector().getEconomy().getMarket("vic_planet_cocytus_market");
        if (market != null) {
            PersonAPI admin = Global.getFactory().createPerson();
            admin.setFaction("vic");
            admin.setGender(FullName.Gender.FEMALE);
            admin.setPostId(Ranks.POST_FACTION_LEADER);
            admin.setRankId(Ranks.FACTION_LEADER);
            admin.getName().setFirst("Tatiana");
            admin.getName().setLast("Murakami");
            admin.setPortraitSprite("graphics/portraits/characters/vic_tatiana.jpg");

            admin.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
            admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);

            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin, 0);
            market.addPerson(admin);
        }

    }

    public static void placeDryDocks() {

        HashMap<String, String> h = new HashMap<>();
        h.put("yama", null);
        h.put("mazalot", null);
        h.put("ailmar", null);

        placeIndustries(h, vic_industries.VIC_REVCENTER);
    }

    private static void placeIndustries(Map<String, String> planetIdMap, String industryId){
        for (Map.Entry<String, String> entry : planetIdMap.entrySet()) {
            MarketAPI m;

            if (Global.getSector().getEconomy().getMarket(entry.getKey()) != null) {
                m = Global.getSector().getEconomy().getMarket(entry.getKey());

                if (!m.hasIndustry(industryId)
                        && !m.isPlayerOwned()
                        && !m.getFaction().getId().equals(Global.getSector().getPlayerFaction().getId())) {

                    m.addIndustry(industryId);
                    m.getIndustry(industryId).setAICoreId(entry.getValue());
                }
            }
        }
    }

}