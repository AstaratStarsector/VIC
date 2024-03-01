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
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
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
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.ids.vic_Items;
import data.campaign.ids.vic_industries;
import data.campaign.listners.vic_stolasSpawn;
import data.scripts.plugins.timer.VIC_TimeTracker;
import data.scripts.plugins.vic_brandEngineUpgradesDetectionRange;
import data.scripts.plugins.vic_combatPlugin;
import data.scripts.plugins.vic_systemWeaponFromLootRemoval;
import data.scripts.weapons.ai.*;
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
import static data.scripts.plugins.vic_addBooze.addBooze;
import static data.scripts.plugins.vic_addBooze.addBoozeToFaction;
import static data.scripts.utilities.vic_trailSpawner.getTrailData;


public class VIC_ModPlugin extends BaseModPlugin {

    public final String
            DISRUPTOR = "vic_disruptorShot_mie",
            ABYSSAL = "vic_abyssalfangs_srm",
            QUTRUB = "vic_qutrubShot_sub",
            AFREET = "vic_afreet_main",
            AFREETLARGE = "vic_afreet_main_large",
            hungruf_main = "vic_hungruf_main",
            hungruf_bomb = "vic_hungruf_sub",
            hatif_main = "vic_hatif_missile_main",
            rokhMain = "vic_rokh_main",
            rokhAlt = "vic_rokh_alt",
            rokhAltSub = "vic_rokh_alt_sub";


    public final String
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

        if (Global.getSettings().getModManager().isModEnabled("alcoholism")) {
            addBooze();
        } else {
            Global.getSettings().getCommoditySpec("vic_booze_c").setBasePrice(0);
            Global.getSettings().getCommoditySpec("vic_booze_c").setExportValue(0);
            Global.getSettings().getCommoditySpec("vic_booze_c").getTags().add("nonecon");
        }

        getTrailData();
    }

    public void onDevModeF8Reload() {
        addTags();
        getTrailData();
    }

    void addTags() {
        List<String> variants = new ArrayList<>();
        {
            variants.add("vic_stolas_standard");
            variants.add("vic_stolas_hunter");
            variants.add("vic_stolasPlasma");
            variants.add("vic_stolas_gauss");
            variants.add("vic_stolas_crusher");
        }
        for (String variantID : variants) {
            ShipVariantAPI variant = Global.getSettings().getVariant(variantID);
            if (!variant.hasTag("no_autofit")) {
                variant.addTag("no_autofit");
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
            case QUTRUB:
                return new PluginPick<MissileAIPlugin>(new vic_qutrubStuckAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            //case "vic_apocrypha_sub":
            case AFREET:
                return new PluginPick<MissileAIPlugin>(new vic_swervingDumbfire(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case AFREETLARGE:
                return new PluginPick<MissileAIPlugin>(new vic_swervingHoming(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case hungruf_main:
                return new PluginPick<MissileAIPlugin>(new vic_hungrufMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case hungruf_bomb:
                return new PluginPick<MissileAIPlugin>(new vic_hungrufBombAI(missile), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case hatif_main:
                return new PluginPick<MissileAIPlugin>(new vic_hatifMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case rokhMain:
                return new PluginPick<MissileAIPlugin>(new vic_rokhMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case rokhAlt:
                return new PluginPick<MissileAIPlugin>(new vic_rokhAltMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case rokhAltSub:
                return new PluginPick<MissileAIPlugin>(new vic_rokhAltSubMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
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

        Global.getSector().getMemoryWithoutUpdate().set("$vicGenerated", true);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientListener(new vic_stolasSpawn(false));
        Global.getSector().getListenerManager().addListener(new vic_systemWeaponFromLootRemoval(false), true);

        if (Global.getSector().getEntityById("vic_star_empyrean") == null) {
            onNewGame();
            onNewGameAfterEconomyLoad();
        }
        addTags();

        if (Global.getSettings().getModManager().isModEnabled("alcoholism")) {
            addBoozeToFaction();
        }

        if (Global.getSector().getImportantPeople().getPerson("vic_JeremyHoloGuide") == null) {
            PersonAPI vicJeremyHoloGuide = Global.getFactory().createPerson();
            vicJeremyHoloGuide.setFaction("vic");
            vicJeremyHoloGuide.setId("vic_JeremyHoloGuide");
            vicJeremyHoloGuide.getName().setFirst("Jeremiah");
            vicJeremyHoloGuide.getName().setLast("Rainier-Galante");
            vicJeremyHoloGuide.setRankId("vicHighExecutive");
            vicJeremyHoloGuide.setPostId("vicPRDirector");
            vicJeremyHoloGuide.setGender(FullName.Gender.MALE);
            vicJeremyHoloGuide.setPortraitSprite("graphics/portraits/characters/vic_jeremy_tv.png");
            Global.getSector().getImportantPeople().addPerson(vicJeremyHoloGuide);
        }

        if (Global.getSector().getImportantPeople().getPerson("vic_TatianaHolo") == null) {
            PersonAPI vicTatianaHolo = Global.getFactory().createPerson();
            vicTatianaHolo.setFaction("vic");
            vicTatianaHolo.setId("vic_TatianaHolo");
            vicTatianaHolo.getName().setFirst("Tatiana");
            vicTatianaHolo.getName().setLast("Vasilevskaya, VIC CEO");
            vicTatianaHolo.setRankId("factionLeader");
            vicTatianaHolo.setPostId("factionLeader");
            vicTatianaHolo.setGender(FullName.Gender.FEMALE);
            vicTatianaHolo.setPortraitSprite("graphics/portraits/characters/vic_tatiana_tv.png");
            Global.getSector().getImportantPeople().addPerson(vicTatianaHolo);
        }

        if (Global.getSector().getImportantPeople().getPerson("vic_ErnestoHolo") == null) {
            PersonAPI vicErnestoHolo = Global.getFactory().createPerson();
            vicErnestoHolo.setFaction("vic");
            vicErnestoHolo.setId("vic_ErnestoHolo");
            vicErnestoHolo.getName().setFirst("Ernesto");
            vicErnestoHolo.getName().setLast("Laidlaw, Technological Advancement Dept. Director");
            vicErnestoHolo.setRankId("vicHighExecutive");
            vicErnestoHolo.setPostId("vicTECHDirector");
            vicErnestoHolo.setGender(FullName.Gender.MALE);
            vicErnestoHolo.setPortraitSprite("graphics/portraits/characters/vic_ernesto_tv.png");
            Global.getSector().getImportantPeople().addPerson(vicErnestoHolo);
        }

        if (Global.getSector().getImportantPeople().getPerson("vic_MurkwellHolo") == null) {
            PersonAPI vicMurkwellHolo = Global.getFactory().createPerson();
            vicMurkwellHolo.setFaction("vic");
            vicMurkwellHolo.setId("vic_MurkwellHolo");
            vicMurkwellHolo.getName().setFirst("Axton");
            vicMurkwellHolo.getName().setLast("Murkwell");
            vicMurkwellHolo.setRankId("vicHighExecutive");
            vicMurkwellHolo.setPostId("vicINTELDirector");
            vicMurkwellHolo.setGender(FullName.Gender.MALE);
            vicMurkwellHolo.setPortraitSprite("graphics/portraits/characters/vic_murkwell_tv.png");
            Global.getSector().getImportantPeople().addPerson(vicMurkwellHolo);
        }

        if (Global.getSector().getImportantPeople().getPerson("vic_AgniezskaHolo") == null) {
            PersonAPI vicAgniezskaHolo = Global.getFactory().createPerson();
            vicAgniezskaHolo.setFaction("vic");
            vicAgniezskaHolo.setId("vic_AgniezskaHolo");
            vicAgniezskaHolo.getName().setFirst("Agniezska");
            vicAgniezskaHolo.getName().setLast("Lazari, Bio-Research Dept. Director");
            vicAgniezskaHolo.setRankId("vicHighExecutive");
            vicAgniezskaHolo.setPostId("vicBIODirector");
            vicAgniezskaHolo.setGender(FullName.Gender.ANY);
            vicAgniezskaHolo.setPortraitSprite("graphics/portraits/characters/vic_agniezska_tv.png");
            Global.getSector().getImportantPeople().addPerson(vicAgniezskaHolo);
        }

        if (Global.getSector().getImportantPeople().getPerson("vic_VolkovHolo") == null) {
            PersonAPI vicVolkovHolo = Global.getFactory().createPerson();
            vicVolkovHolo.setFaction("vic");
            vicVolkovHolo.setId("vic_VolkovHolo");
            vicVolkovHolo.getName().setFirst("Victor");
            vicVolkovHolo.getName().setLast("Volkov, VIC Founder");
            vicVolkovHolo.setRankId("formerFactionLeader");
            vicVolkovHolo.setPostId("factionLeader");
            vicVolkovHolo.setGender(FullName.Gender.MALE);
            vicVolkovHolo.setPortraitSprite("graphics/portraits/characters/vic_volkov_tv.png");
            Global.getSector().getImportantPeople().addPerson(vicVolkovHolo);
        }


    }

    public void onNewGameAfterProcGen() {


    }

    @Override
    public void onEnabled(boolean wasEnabledBefore) {
        /*
        if (wasEnabledBefore) return;
        onNewGame();
        onNewGameAfterEconomyLoad();

         */
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        placeRevCentres();

        MarketAPI market = Global.getSector().getEconomy().getMarket("vic_planet_cocytus_market");
        if (market != null) {
            PersonAPI vicTatiana = Global.getFactory().createPerson();
            vicTatiana.setFaction("vic");
            vicTatiana.setGender(FullName.Gender.FEMALE);
            vicTatiana.setPostId(Ranks.POST_FACTION_LEADER);
            vicTatiana.setRankId(Ranks.FACTION_LEADER);
            vicTatiana.getName().setFirst("Tatiana");
            vicTatiana.getName().setLast("Vasilevskaya");
            vicTatiana.setPortraitSprite("graphics/portraits/characters/vic_tatiana.png");

            //admin.getStats().setSkillLevel(Skills.SPACE_OPERATIONS , 3);
            vicTatiana.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
            //admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);

            market.setAdmin(vicTatiana);
            market.getCommDirectory().addPerson(vicTatiana, 0);
            market.addPerson(vicTatiana);
        }
    }

    public static void placeRevCentres() {

        HashMap<String, String> h = new HashMap<>();
        h.put("yama", null);
        h.put("yesod", null);
        h.put("ailmar", null);

        placeIndustries(h, vic_industries.VIC_REVCENTER);
    }

    private static void placeIndustries(Map<String, String> planetIdMap, String industryId) {
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
        final Map<String, Integer> productionDemand = new HashMap<>();

        {
            productionDemand.put(Conditions.FARMLAND_POOR, 4);
            productionDemand.put(Conditions.FARMLAND_ADEQUATE, 3);
            productionDemand.put(Conditions.FARMLAND_BOUNTIFUL, 2);
            productionDemand.put(Conditions.FARMLAND_RICH, 1);
        }

        public void apply(Industry industry) {
            if (industry instanceof BaseIndustry) {
                BaseIndustry b = (BaseIndustry) industry;
                int production = 0;
                for (MarketConditionAPI condition : industry.getMarket().getConditions()) {
                    if (condition.getId().startsWith("farmland")) {
                        production = productionDemand.get(condition.getId());
                        break;
                    }
                }
                int productionBonus = production - Math.round(getShortage(industry, production));
                b.demand(9, vic_Items.GENETECH, production, Misc.ucFirst(spec.getName().toLowerCase()));
                industry.getSupplyBonus().modifyFlat(spec.getId(), productionBonus, Misc.ucFirst(spec.getName().toLowerCase()));

                //industry.getMarket().getHazard().modifyFlat(spec.getId(), hazard, Misc.ucFirst(spec.getName().toLowerCase()));
                //industry.getMarket().getAccessibilityMod().modifyFlat(spec.getId(), -hazard, Misc.ucFirst(spec.getName().toLowerCase()));
            }
        }

        public void unapply(Industry industry) {
            BaseIndustry b = (BaseIndustry) industry;
            b.demand(9, vic_Items.GENETECH, 0, null);
            industry.getSupplyBonus().modifyFlat(spec.getId(), 0, Misc.ucFirst(spec.getName().toLowerCase()));

            //fix for one of the last fuck ups gona keep for now
            industry.getMarket().getAccessibilityMod().unmodifyFlat(spec.getId());
            industry.getMarket().getHazard().unmodifyFlat(spec.getId());
        }

        float getShortage(Industry industry, int demand) {
            float available = industry.getMarket().getCommodityData(vic_Items.GENETECH).getAvailable();
            float shortageAmount = demand - available;
            if (shortageAmount < 0) shortageAmount = 0;
            return shortageAmount;
        }

        protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
                                              InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, String pre, float pad) {
            text.addPara(pre + "Increases farming production by from %s to %s units depending on poorness of soil and adds demand for same amount of units of genetech.",
                    pad, Misc.getHighlightColor(),
                    "" + productionDemand.get(Conditions.FARMLAND_POOR), "" + productionDemand.get(Conditions.FARMLAND_RICH));
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
                } else if ("organics deposits".equals(curr)) {
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