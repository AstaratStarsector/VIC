package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BoostIndustryInstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.ids.vic_Items;
import data.campaign.ids.vic_industries;
import data.scripts.plugins.timer.VIC_TimeTracker;
import data.scripts.plugins.vic_brandEngineUpgradesDetectionRange;
import data.scripts.weapons.ai.VIC_SwarmMirvAI;
import data.scripts.weapons.ai.vic_disruptorShot_AI;
import data.scripts.weapons.ai.vic_gaganaStuckAI;
import data.scripts.weapons.autofireAI.vic_VerliokaAutofireAI;
import data.world.VICGen;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo.HABITABLE;
import static com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo.NO_ATMOSPHERE;


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
                TextureData.readTextureDataCSV("data/lights/vic_texture_data.csv");
            }
        }

        //add special items
        ItemEffectsRepo.ITEM_EFFECTS.put(vic_Items.GMOfarm, GMO);
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
        if (!haveNexerelin || SectorManager.getManager().isCorvusMode()) {
            new VICGen().generate(Global.getSector());
        }

        if (!Global.getSector().hasScript(VIC_TimeTracker.class)) {
            Global.getSector().addScript(new VIC_TimeTracker());
            Global.getSector().addScript(new vic_brandEngineUpgradesDetectionRange());
        }
    }

    public void onNewGameAfterProcGen() {


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

            //admin.getStats().setSkillLevel(Skills.SPACE_OPERATIONS , 3);
            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
            //admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);

            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin, 0);
            market.addPerson(admin);
        }

    }

    public static void placeDryDocks() {

        HashMap<String, String> h = new HashMap<>();
        h.put("yama", null);
        h.put("yesod", null);
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


    //Transfiguration Solutions
    BoostIndustryInstallableItemEffect GMO = new BoostIndustryInstallableItemEffect(
            vic_Items.GMOfarm, 0, 0) {
        final int production = 4;
        final int demand = 4;
        final float hazard = 0.25f;

        public void apply(Industry industry) {
            if (industry instanceof BaseIndustry) {
                BaseIndustry b = (BaseIndustry) industry;
                int productionBonus = production - Math.round(getShortage(industry));
                b.demand(9, vic_Items.GENETECH, demand, Misc.ucFirst(spec.getName().toLowerCase()));

                industry.getSupplyBonus().modifyFlat(spec.getId(), productionBonus, Misc.ucFirst(spec.getName().toLowerCase()));

                //industry.getMarket().getHazard().modifyFlat(spec.getId(), hazard, Misc.ucFirst(spec.getName().toLowerCase()));
                industry.getMarket().getAccessibilityMod().modifyFlat(spec.getId(), -hazard, Misc.ucFirst(spec.getName().toLowerCase()));
            }
        }

        public void unapply(Industry industry) {
            BaseIndustry b = (BaseIndustry) industry;
            b.demand(9, vic_Items.GENETECH, 0, null);
            industry.getSupplyBonus().modifyFlat(spec.getId(), 0, Misc.ucFirst(spec.getName().toLowerCase()));

            industry.getMarket().getHazard().unmodifyFlat(spec.getId());
        }

        float getShortage(Industry industry) {
            float available = industry.getMarket().getCommodityData(vic_Items.GENETECH).getAvailable();
            float shortageAmount = 4 - available;
            if (shortageAmount < 0) shortageAmount = 0;
            return shortageAmount;
        }

        protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
                                              InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, String pre, float pad) {
            text.addPara(pre + "Increases farming production by %s units. Adds demand for %s units of genetech. Reduces accessibility by %s",
                    pad, Misc.getHighlightColor(),
                    "" + production, "" + demand, Math.round(hazard * 100) + "%");
        }

        @Override
        public String[] getSimpleReqs(Industry industry) {
            return new String[]{"organics deposits"};
        }

        @Override
        public List<String> getUnmetRequirements(Industry industry) {
            List<String> unmet = new ArrayList<>();
            if (industry == null) return unmet;

            MarketAPI market = industry.getMarket();

            for (String curr : getRequirements(industry)) {
                if (NO_ATMOSPHERE.equals(curr)) {
                    if (!market.hasCondition(Conditions.NO_ATMOSPHERE)) {
                        unmet.add(curr);
                    }
                } else if (HABITABLE.equals(curr)) {
                    if (!market.hasCondition(Conditions.HABITABLE)) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.NOT_HABITABLE.equals(curr)) {
                    if (market.hasCondition(Conditions.HABITABLE)) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.GAS_GIANT.equals(curr)) {
                    if (market.getPlanetEntity() != null && !market.getPlanetEntity().isGasGiant()) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.NOT_A_GAS_GIANT.equals(curr)) {
                    if (market.getPlanetEntity() != null && market.getPlanetEntity().isGasGiant()) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.NOT_EXTREME_WEATHER.equals(curr)) {
                    if (market.hasCondition(Conditions.EXTREME_WEATHER)) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.NOT_EXTREME_TECTONIC_ACTIVITY.equals(curr)) {
                    if (market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.NO_TRANSPLUTONIC_ORE_DEPOSITS.equals(curr)) {
                    if (market.hasCondition(Conditions.RARE_ORE_SPARSE) ||
                            market.hasCondition(Conditions.RARE_ORE_MODERATE) ||
                            market.hasCondition(Conditions.RARE_ORE_ABUNDANT) ||
                            market.hasCondition(Conditions.RARE_ORE_RICH) ||
                            market.hasCondition(Conditions.RARE_ORE_ULTRARICH)) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.NO_VOLATILES_DEPOSITS.equals(curr)) {
                    if (market.hasCondition(Conditions.VOLATILES_TRACE) ||
                            market.hasCondition(Conditions.VOLATILES_DIFFUSE) ||
                            market.hasCondition(Conditions.VOLATILES_ABUNDANT) ||
                            market.hasCondition(Conditions.VOLATILES_PLENTIFUL)) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.HOT_OR_EXTREME_HEAT.equals(curr)) {
                    if (!market.hasCondition(Conditions.HOT) &&
                            !market.hasCondition(Conditions.VERY_HOT)) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.COLD_OR_EXTREME_COLD.equals(curr)) {
                    if (!market.hasCondition(Conditions.COLD) &&
                            !market.hasCondition(Conditions.VERY_COLD)) {
                        unmet.add(curr);
                    }
                } else if (ItemEffectsRepo.CORONAL_TAP_RANGE.equals(curr)) {
                    Pair<SectorEntityToken, Float> p = PopulationAndInfrastructure.getNearestCoronalTap(
                            market.getLocationInHyperspace(), true);
                    float dist = Float.MAX_VALUE;
                    if (p != null) dist = p.two;
                    if (dist > ItemEffectsRepo.CORONAL_TAP_LIGHT_YEARS) {
                        unmet.add(curr);
                    }
                } else if ("organics deposits".equals(curr)){
                    if (!market.hasCondition(Conditions.ORGANICS_TRACE) &&
                            !market.hasCondition(Conditions.ORGANICS_ABUNDANT) &&
                            !market.hasCondition(Conditions.ORGANICS_COMMON) &&
                            !market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
                        unmet.add(curr);
                    }
                }
            }
            return unmet;
        }
    };
}