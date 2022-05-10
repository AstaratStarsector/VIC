package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.launcher.ModManager;
import data.scripts.utilities.StringHelper;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VIC_MarketCMD extends BaseCommandPlugin {

    public static Logger log = Global.getLogger(VIC_MarketCMD.class);
    public static String
            ENGAGE = "mktEngage",
            UMOMenu = "UMOMenu",
            VBombMenu = "VBombMenu",
            VBombConfirm = "VBombConfirm",
            VBombResults = "VBombResults",
            GO_BACK = "mktGoBack",
            NEX_GO_BACK = "marketConsiderHostile",
            MOD_TO_CHECK = "vic_vBombHmod";
    protected static VIC_TempData temp = new VIC_TempData();
    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected MarketAPI market;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected FactionAPI faction;

    public VIC_MarketCMD() {
    }

    public VIC_MarketCMD(SectorEntityToken entity) {
        init(entity);
    }

    float genetechCostFraction = 0.05f;
    public static int getBombardmentCost(MarketAPI market, CampaignFleetAPI fleet) {
        float planetSize = 150f; //used if market on station
        if (market.getPlanetEntity() != null){
            planetSize = market.getPlanetEntity().getRadius();
            if (planetSize > 350) planetSize = 350;
            if (planetSize< 50) planetSize = 50;
        }
        float str = getDefenderStr(market);

        float result = (str * 0.1f) + (planetSize * 8);

        if (fleet != null && result < 0) result = 0;

        float hazardMult = market.getHazardValue();
        if (hazardMult > 2) hazardMult = 2;
        else if (hazardMult < 0.5f) hazardMult = 0.5f;

        return Math.round(result * hazardMult * HmodBonus());
    }

    public static float HmodBonus() {
        int HmodsAmount = 0;
        for (FleetMemberAPI shipToCheck : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (shipToCheck.getVariant().hasHullMod(MOD_TO_CHECK)) {
                HmodsAmount++;
            }
        }
        return (float) (1 * Math.pow(0.9f, HmodsAmount - 1));
    }

    public static float getDefenderStr(MarketAPI market) {
        StatBonus stat = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);
        return (float) Math.round(stat.computeEffective(0f));
    }

    public static TooltipMakerAPI.StatModValueGetter statPrinter(final boolean withNegative) {
        return new TooltipMakerAPI.StatModValueGetter() {
            public String getPercentValue(MutableStat.StatMod mod) {
                String prefix = mod.getValue() > 0 ? "+" : "";
                return prefix + (int) (mod.getValue()) + "%";
            }

            public String getMultValue(MutableStat.StatMod mod) {
                return Strings.X + "" + Misc.getRoundedValue(mod.getValue());
            }

            public String getFlatValue(MutableStat.StatMod mod) {
                String prefix = mod.getValue() > 0 ? "+" : "";
                return prefix + (int) (mod.getValue()) + "";
            }

            public Color getModColor(MutableStat.StatMod mod) {
                if (withNegative && mod.getValue() < 1f) return Misc.getNegativeHighlightColor();
                return null;
            }
        };
    }

    public static void addBombardVisual(SectorEntityToken target) {
        if (target != null && target.isInCurrentLocation()) {
            int num = (int) Math.round((3.15 * Math.pow(target.getRadius(), 2)) / 800);
            num *= 2;
            if (num > 200) num = 200;
            if (num < 10) num = 10;
            target.addScript(new VIC_MarketCMD.ViralBombardmentAnimation(num, target));
        }
    }

    protected void clearTemp() {
        if (temp != null) {
            temp.raidLoot = null;
            temp.raidValuables = null;
            temp.target = null;
            temp.willBecomeHostile.clear();
        }
    }

    protected void init(SectorEntityToken entity) {

        memory = entity.getMemoryWithoutUpdate();
        this.entity = entity;
        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        faction = entity.getFaction();

        market = entity.getMarket();


        //DebugFlags.MARKET_HOSTILITIES_DEBUG = false;
        //market.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET, true, 0.1f);

    }

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        //super.execute(ruleId, dialog, params, memoryMap);

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        entity = dialog.getInteractionTarget();
        init(entity);

        memory = getEntityMemory(memoryMap);

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        switch (command) {
            case "UMOMenu":
                clearTemp();
                UMOShowMenu();
                break;
            case "VBombMenu":
                VBombMenu();
                break;
            case "VBombConfirm":
                VBombConfirm();
                break;
            case "VBombResults":
                VBombResults();
                break;
        }

        return true;

    }

    protected void UMOShowMenu() {
        options.clearOptions();
        CampaignFleetAPI primary = getInteractionTargetForFIDPI();
        CampaignFleetAPI station = getStationFleet();

        boolean hasNonStation = false;
        boolean hasStation = station != null;
        boolean otherWantsToFight = false;
        BattleAPI b;
        FleetEncounterContext context = null;
        FleetInteractionDialogPluginImpl plugin = null;

        boolean ongoingBattle = false;

        boolean playerOnDefenderSide = false;
        boolean playerCanNotJoin = false;

        String stationType = "station";

        StationState state = getStationState();

        if (market != null) {
            Global.getSector().getEconomy().tripleStep();
        }

        //Desc
        text.addPara(StringHelper.getString("VIC_VBomb", "UMO_desc0"));
        text.addPara(StringHelper.getString("VIC_VBomb", "UMO_desc1"));
        text.addPara(StringHelper.getString("VIC_VBomb", "UMO_desc2"));

        if (primary == null) {
            if (state != StationState.NONE) {
                printStationState();
                text.addPara("There are no nearby fleets to defend the colony.");
            }
        } else {
            ongoingBattle = primary.getBattle() != null;

            FleetInteractionDialogPluginImpl.FIDConfig params = new FleetInteractionDialogPluginImpl.FIDConfig();
            params.justShowFleets = true;
            params.showPullInText = true;
            plugin = new FleetInteractionDialogPluginImpl(params);
            dialog.setInteractionTarget(primary);
            plugin.init(dialog);
            dialog.setInteractionTarget(entity);


            context = (FleetEncounterContext) plugin.getContext();
            b = context.getBattle();

            BattleAPI.BattleSide playerSide = b.pickSide(playerFleet);
            playerCanNotJoin = playerSide == BattleAPI.BattleSide.NO_JOIN;
            if (!playerCanNotJoin) {
                playerOnDefenderSide = b.getSide(playerSide) == b.getSideFor(primary);
            }
            if (!ongoingBattle) {
                playerOnDefenderSide = false;
            }

            if (playerSide != BattleAPI.BattleSide.NO_JOIN) {
                //for (CampaignFleetAPI fleet : b.getNonPlayerSide()) {
                for (CampaignFleetAPI fleet : b.getOtherSide(playerSide)) {
                    if (!fleet.isStationMode()) {
                        hasNonStation = true;
                        break;
                    }
                }
            }

            otherWantsToFight = hasStation || plugin.otherFleetWantsToFight(true);

            if (hasStation) {
                String name = "An orbital station";
                if (station != null) {
                    FleetMemberAPI flagship = station.getFlagship();
                    if (flagship != null) {
                        name = flagship.getVariant().getDesignation().toLowerCase();
                        stationType = name;
                        name = Misc.ucFirst(station.getFaction().getPersonNamePrefixAOrAn()) + " " +
                                station.getFaction().getPersonNamePrefix() + " " + name;
                    }
                }
                text.addPara(name + " dominates the orbit and prevents any hostile action.");

            } else if (hasNonStation && otherWantsToFight) {
                printStationState();
                text.addPara("Defending ships are present in sufficient strength to prevent any hostile action " +
                        "until they are dealt with.");
            } else if (hasNonStation && !otherWantsToFight) {
                printStationState();
                text.addPara("Defending ships are present, but not in sufficient strength " +
                        "to want to give battle or prevent any hostile action you might take.");
            }

            plugin.printOngoingBattleInfo();
        }

        options.clearOptions();

        String engageText = "Engage the defenders";

        if (playerCanNotJoin) {
            engageText = "Engage the defenders";
        } else if (playerOnDefenderSide) {
            if (hasStation && hasNonStation) {
                engageText = "Aid the " + stationType + " and its defenders";
            } else if (hasStation) {
                engageText = "Aid the " + stationType + "";
            } else {
                engageText = "Aid the defenders";
            }
        } else {
            if (ongoingBattle) {
                engageText = "Aid the attacking forces";
            } else {
                if (hasStation && hasNonStation) {
                    engageText = "Engage the " + stationType + " and its defenders";
                } else if (hasStation) {
                    engageText = "Engage the " + stationType + "";
                } else {
                    engageText = "Engage the defenders";
                }
            }
        }


        temp.canBombard = (!hasNonStation || (hasNonStation && !otherWantsToFight)) && !hasStation;

        if (DebugFlags.MARKET_HOSTILITIES_DEBUG) {
            if (!temp.canRaid || !temp.canBombard) {
                text.addPara("(DEBUG mode: can raid and bombard anyway)");
            }
            temp.canBombard = true;
        }


        if (context != null && otherWantsToFight && !playerCanNotJoin) {
            options.addOption(engageText, ENGAGE);
            boolean knows = context.getBattle() != null && context.getBattle().getNonPlayerSide() != null &&
                    context.getBattle().knowsWhoPlayerIs(context.getBattle().getNonPlayerSide());
            boolean lowImpact = context.isLowRepImpact();
            FactionAPI nonHostile = plugin.getNonHostileOtherFaction();
            if (nonHostile != null && knows && !lowImpact && !context.isEngagedInHostilities()) {
                options.addOptionConfirmation(ENGAGE,
                        "The " + nonHostile.getDisplayNameLong() +
                                " " + nonHostile.getDisplayNameIsOrAre() +
                                " not currently hostile, and you have been positively identified. " +
                                "Are you sure you want to engage in open hostilities?", "Yes", "Never mind");
            }
        }

        boolean haveVBomb = false;
        for (FleetMemberAPI shipToCheck : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (!haveVBomb && shipToCheck.getVariant().hasHullMod(MOD_TO_CHECK)) {
                haveVBomb = true;
            }
        }
        if (haveVBomb) {
            options.addOption("Viral Bombardment", VBombMenu);
        }
        if (!temp.canBombard) {
            options.setEnabled(VBombMenu, false);
        }
        if (ModManager.getInstance().isModEnabled("nexerelin")) {
            options.addOption("Go back", NEX_GO_BACK);
        } else {
            options.addOption("Go back", GO_BACK);
        }
        options.setShortcut(GO_BACK, Keyboard.KEY_ESCAPE, false, false, false, true);

        if (plugin != null) {
            plugin.cleanUpBattle();
        }

    }

    protected void VBombMenu() {

        float width = 350;
        float opad = 10f;
        float small = 5f;

        Color h = Misc.getHighlightColor();
        Color b = Misc.getNegativeHighlightColor();

        dialog.getVisualPanel().showImagePortion("illustrations", "bombard_prepare", 640, 400, 0, 0, 480, 300);

        StatBonus defender = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);

        //Custom stats for menu table
        float bomardBonus = Misc.getFleetwideTotalMod(playerFleet, Stats.FLEET_BOMBARD_COST_REDUCTION, 0f);
        String increasedBombardKey = "core_addedBombard";
        StatBonus bombardBonusStat = new StatBonus();
        if (bomardBonus > 0) {
            bombardBonusStat.modifyFlat(increasedBombardKey, -bomardBonus, "Specialized fleet bombardment capability");
        }
        StatBonus hazardMult = new StatBonus();
        float hazardMultClamp = market.getHazardValue();
        if (hazardMultClamp > 2) hazardMultClamp = 2;
        else if (hazardMultClamp < 0.5f) {
            hazardMultClamp = 0.5f;
        }
        hazardMult.modifyMult("VIC_PlanetHazad", hazardMultClamp, "Planet hazard rating");

        StatBonus HmodBonus = new StatBonus();
        HmodBonus.modifyMult("VIC_PlanetHazad", HmodBonus(), "Fleet capability for virus bombing");

        float defenderStr = (int) Math.round(defender.computeEffective(0f));
        defenderStr -= bomardBonus;
        if (defenderStr < 0) defenderStr = 0;

        temp.defenderStr = defenderStr;

        TooltipMakerAPI info = text.beginTooltip();

        info.setParaSmallInsignia();

        String has = faction.getDisplayNameHasOrHave();
        String is = faction.getDisplayNameIsOrAre();
        boolean hostile = faction.isHostileTo(Factions.PLAYER);
        boolean tOn = playerFleet.isTransponderOn();
        float initPad = 0f;
        if (!hostile) {
            if (tOn) {
                info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is +
                                " not currently hostile. A bombardment is a major enough hostile action that it can't be concealed, " +
                                "regardless of transponder status.",
                        initPad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
            }
            initPad = opad;
        }

        info.addPara(StringHelper.getString("VIC_VBomb", "VBomb_desc0"), initPad);
        info.addPara(StringHelper.getString("VIC_VBomb", "VBomb_desc1"), initPad);


        /*
        info.addPara("Ground defense strength: %s", opad, h, "" + (int) defenderStr);
        info.addStatModGrid(width, 50, opad, small, defender, true, statPrinter(true));

         */
        info.addPara("Bombardment difficulty rating: %s", opad, h, "" + getBombardmentCost(market, playerFleet));

        float planetSize = 150f; //used if market on station
        if (market.getPlanetEntity() != null){
            planetSize = market.getPlanetEntity().getRadius();
            if (planetSize > 350) planetSize = 350;
            if (planetSize< 50) planetSize = 50;
        }
        StatBonus planetSizeBonus = new StatBonus();
        planetSizeBonus.modifyFlat("VIC_PlanetSize", planetSize * 10, "Planet size");
        planetSizeBonus.modifyFlat("VIC_ColonyDefence", getDefenderStr(market) * 0.1f, "Ground defences");


        info.addStatModGrid(width, 50, opad, small, planetSizeBonus, true, statPrinter(false));

        if (market.getHazardValue() != 1) {
            info.addStatModGrid(width, 50, opad, small, hazardMult, true, statPrinter(false));
        }
        if (HmodBonus.getBonusMult() != 1) {
            info.addStatModGrid(width, 50, opad, small, HmodBonus, true, statPrinter(false));
        }

        text.addTooltip();

        temp.bombardCostOrganics = Math.round(getBombardmentCost(market, playerFleet));
        temp.bombardCostGenetech = Math.round(getBombardmentCost(market, playerFleet) * genetechCostFraction);


        int organics = (int) playerFleet.getCargo().getCommodityQuantity("organics");
        int genetech = (int) playerFleet.getCargo().getCommodityQuantity("vic_genetech");
        boolean canBombard = organics >= temp.bombardCostOrganics && genetech >= temp.bombardCostGenetech;

        LabelAPI label = text.addPara("A bombardment requires %s organics and %s genetechs. " +
                        "You have %s organics and %s genetechs.",
                h, "" + temp.bombardCostOrganics, "" + temp.bombardCostGenetech, "" + organics, "" + genetech);
        //label.setHighlight("" + temp.bombardCostOrganics, "" + temp.bombardCostGenetech, "" + organics, "" + genetech);
        label.setHighlightColors(canBombard ? h : b, canBombard ? h : b, h);

        options.clearOptions();
        options.addOption("Commence Viral Bombardment", VBombConfirm);


        if (ModManager.getInstance().isModEnabled("nexerelin")) {
            options.addOption("Go back", NEX_GO_BACK);
        } else {
            options.addOption("Go back", UMOMenu);
        }

        if (DebugFlags.MARKET_HOSTILITIES_DEBUG) {
            canBombard = true;
        }
        if (market.hasCondition("VIC_VBomb_scar")) {
            options.setEnabled(VBombConfirm, false);
            options.setTooltip(VBombConfirm, Misc.ucFirst(market.getName()) + " already contaminated.");
        } else if (!canBombard) {
            options.setEnabled(VBombConfirm, false);
            options.setTooltip(VBombConfirm, "Not enough organics.");
        }

    }

    protected void VBombConfirm() {

        List<FactionAPI> nonHostile = new ArrayList<>();

        temp.willBecomeHostile.clear();
        temp.willBecomeHostile.add(faction);

        boolean hidden = market.isHidden();

        List<FactionAPI> vengeful = new ArrayList<>();

        if (!hidden) {
            for (FactionAPI faction : Global.getSector().getAllFactions()) {
                if (temp.willBecomeHostile.contains(faction)) continue;

                if (faction.getCustomBoolean(Factions.CUSTOM_CARES_ABOUT_ATROCITIES)) {
                    if (faction.getRelationshipLevel(market.getFaction()) == RepLevel.VENGEFUL) {
                        vengeful.add(faction);
                    } else {
                        boolean hostile = faction.isHostileTo(Factions.PLAYER);
                        temp.willBecomeHostile.add(faction);
                        if (!hostile) {
                            nonHostile.add(faction);
                        }
                    }
                }
            }
        }
        //text.addPara(temp.willBecomeHostile.toString(), Misc.getHighlightColor());

        if (nonHostile.isEmpty()) {
            text.addPara("An atrocity of this scale can not be hidden, but any factions that would " +
                    "be dismayed by such actions are already hostile to you.");
        } else {
            text.addPara("An atrocity of this scale can not be hidden, " +
                    "and will make the following factions hostile:");
        }

        if (!nonHostile.isEmpty()) {
            TooltipMakerAPI info = text.beginTooltip();
            info.setParaFontDefault();

            info.setBulletedListMode(BaseIntelPlugin.INDENT);
            float initPad = 0f;
            for (FactionAPI fac : nonHostile) {
                info.addPara(Misc.ucFirst(fac.getDisplayName()), fac.getBaseUIColor(), initPad);
                initPad = 3f;
            }
            info.setBulletedListMode(null);

            text.addTooltip();
        }

        text.addPara(StringHelper.getString("VIC_VBomb", "VBomb_conf0"));
        options.clearOptions();
        options.addOption("Confirm", VBombResults);
        options.addOption("Never mind", VBombMenu);

        if (nonHostile.size() == 1) {
            FactionAPI faction = nonHostile.get(0);
            options.addOptionConfirmation(VBombResults,
                    "The " + faction.getDisplayNameLong() +
                            " " + faction.getDisplayNameIsOrAre() +
                            " not currently hostile, and will become hostile if you carry out the bombardment. " +
                            "Are you sure?", "Yes", "Never mind");
        } else if (nonHostile.size() > 1) {
            options.addOptionConfirmation(VBombResults,
                    "Multiple factions that are not currently hostile " +
                            "will become hostile if you carry out the bombardment. " +
                            "Are you sure?", "Yes", "Never mind");
        }

    }

    protected void VBombResults() {

        dialog.getVisualPanel().showImagePortion("illustrations", "bombard_tactical_result", 640, 400, 0, 0, 480, 300);

        Misc.increaseMarketHostileTimeout(market, 120f);

        int organicsCost = Math.round(getBombardmentCost(market, playerFleet));
        int genetechCost = Math.round(organicsCost * genetechCostFraction);
        playerFleet.getCargo().removeCommodity(Commodities.ORGANICS, organicsCost);
        AddRemoveCommodity.addCommodityLossText(Commodities.ORGANICS, organicsCost, text);
        playerFleet.getCargo().removeCommodity("vic_genetech", genetechCost);
        AddRemoveCommodity.addCommodityLossText("vic_genetech", genetechCost, text);


        //text.addPara(temp.willBecomeHostile.toString(), Misc.getHighlightColor());
        int size = market.getSize();
        for (FactionAPI curr : temp.willBecomeHostile) {
            CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
            impact.delta = market.getSize() * -0.02f * 1f;
            if (curr == faction) {
                impact.ensureAtBest = RepLevel.VENGEFUL;
                impact.delta *= 2;
            } else if (size <= 3) {
                impact.ensureAtBest = RepLevel.NEUTRAL;
            }
            Global.getSector().adjustPlayerReputation(
                    new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM,
                            impact, null, text, true, true),
                    curr.getId());
        }

        int stabilityPenalty = 2;

        RecentUnrest.get(market).add(stabilityPenalty, "Recent bombardment");
        String str = "Stability of %s reduced by %s.";
        text.addPara(str, Misc.getHighlightColor(), "" + market.getName(), "" + stabilityPenalty);

        market.addCondition("VIC_VBomb_scar");
        Color toxinColor = new Color(121, 182, 5, 164);
        if (market.getPlanetEntity() != null) {
            PlanetAPI planet = market.getPlanetEntity();
            if (planet.getSpec() != null){
                planet.getSpec().setAtmosphereColor(toxinColor);
                if (planet.getSpec().getCloudTexture() != null){
                    planet.getSpec().setCloudColor(toxinColor);
                }
                planet.applySpecChanges();
            }
        }


        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
            if (dialog.getInteractionTarget() != null &&
                    dialog.getInteractionTarget().getMarket() != null) {
                Global.getSector().setPaused(false);
                dialog.getInteractionTarget().getMarket().getMemoryWithoutUpdate().advance(0.0001f);
                Global.getSector().setPaused(true);
            }
            ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }

        Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_BOMBARDED,
                Factions.PLAYER, true, 30f);


        addBombardVisual(market.getPrimaryEntity());
        text.addPara(StringHelper.getString("VIC_VBomb", "VBomb_end0"));

        options.clearOptions();

        if (ModManager.getInstance().isModEnabled("nexerelin")) {
            options.addOption("Continue", NEX_GO_BACK);
        } else {
            options.addOption("Continue", GO_BACK);
        }
    }

    protected StationState getStationState() {
        CampaignFleetAPI fleet = Misc.getStationFleet(market);
        boolean destroyed = false;
        if (fleet == null) {
            fleet = Misc.getStationBaseFleet(market);
            if (fleet != null) {
                destroyed = true;
            }
        }

        if (fleet == null) return StationState.NONE;

        MarketAPI market = Misc.getStationMarket(fleet);
        if (market != null) {
            for (Industry ind : market.getIndustries()) {
                if (ind.getSpec().hasTag(Industries.TAG_STATION)) {
                    if (ind.isBuilding() && !ind.isDisrupted() && !ind.isUpgrading()) {
                        return StationState.UNDER_CONSTRUCTION;
                    }
                }
            }
        }

        if (destroyed) return StationState.REPAIRS;

        return StationState.OPERATIONAL;
    }

    protected void printStationState() {
        StationState state = getStationState();
        if (state == StationState.REPAIRS || state == StationState.UNDER_CONSTRUCTION) {
            CampaignFleetAPI fleet = Misc.getStationBaseFleet(market);
            String name = "orbital station";
            if (fleet != null) {
                FleetMemberAPI flagship = fleet.getFlagship();
                if (flagship != null) {
                    name = flagship.getVariant().getDesignation().toLowerCase();
                }
            }
            if (state == StationState.REPAIRS) {
                text.addPara("The " + name + " has suffered extensive damage and is not currently combat-capable.");
            } else {
                text.addPara("The " + name + " is under construction and is not currently combat-capable.");
            }
        }
    }

    protected CampaignFleetAPI getInteractionTargetForFIDPI() {
        CampaignFleetAPI primary = getStationFleet();
        if (primary == null) {
            CampaignFleetAPI best = null;
            float minDist = Float.MAX_VALUE;
            for (CampaignFleetAPI fleet : Misc.getNearbyFleets(entity, 2000)) {
                if (fleet.getBattle() != null) continue;

                if (fleet.getFaction() != market.getFaction()) continue;
                if (fleet.getFleetData().getNumMembers() <= 0) continue;

                float dist = Misc.getDistance(entity.getLocation(), fleet.getLocation());
                dist -= entity.getRadius();
                dist -= fleet.getRadius();

                if (dist < Misc.getBattleJoinRange()) {
                    if (dist < minDist) {
                        best = fleet;
                        minDist = dist;
                    }
                }
            }
            primary = best;
        }
        /*
        else {
            primary.setLocation(entity.getLocation().x, entity.getLocation().y);
        }
         */
        return primary;
    }

    protected CampaignFleetAPI getStationFleet() {
        CampaignFleetAPI station = Misc.getStationFleet(market);
        if (station == null) return null;

        if (station.getFleetData().getMembersListCopy().isEmpty()) return null;

        return station;
    }

    public enum StationState {
        NONE,
        OPERATIONAL,
        UNDER_CONSTRUCTION,
        REPAIRS
    }

    protected static class VIC_TempData {
        public boolean canRaid;
        public boolean canBombard;

        public int bombardCostOrganics;
        public int bombardCostGenetech;

        public float defenderStr;

        public Map<CommodityOnMarketAPI, Float> raidValuables;
        public CargoAPI raidLoot;
        public Industry target = null;
        public List<FactionAPI> willBecomeHostile = new ArrayList<>();
    }

    public static class ViralBombardmentAnimation implements EveryFrameScript {
        int num;
        SectorEntityToken target;
        int added = 0;
        float elapsed = 0;

        public ViralBombardmentAnimation(int num, SectorEntityToken target) {
            this.num = num;
            this.target = target;
        }

        public boolean runWhilePaused() {
            return false;
        }

        public boolean isDone() {
            return added >= num;
        }

        public void advance(float amount) {
            elapsed += amount * MathUtils.getRandomNumberInRange(0.6f, 1f);
            if (elapsed < 0.15f) return;

            elapsed = 0f;

            int curr = Math.round(MathUtils.getRandomNumberInRange(0.6f, 1f) * num / 10);
            if (curr < 1) curr = 1;

            Color color = new Color(166, 255, 0, 255);

            Vector2f vel = new Vector2f();

            if (target.getOrbit() != null &&
                    target.getCircularOrbitRadius() > 0 &&
                    target.getCircularOrbitPeriod() > 0 &&
                    target.getOrbitFocus() != null) {
                float circumference = 2f * (float) Math.PI * target.getCircularOrbitRadius();
                float speed = circumference / target.getCircularOrbitPeriod();

                float dir = Misc.getAngleInDegrees(target.getLocation(), target.getOrbitFocus().getLocation()) + 90f;
                vel = Misc.getUnitVectorAtDegreeAngle(dir);
                vel.scale(speed / Global.getSector().getClock().getSecondsPerDay());
            }

            for (int i = 0; i < curr; i++) {
                float glowSize = 25f + 75f * (float) Math.random();
                float angle = (float) Math.random() * 360f;
                float dist = (float) Math.sqrt(MathUtils.getRandomNumberInRange(0.01f, 0.95f)) * target.getRadius();

                float factor = 0.5f + 0.5f * (1f - (float) Math.sqrt(dist / target.getRadius()));
                glowSize *= factor;
                Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
                loc.scale(dist);
                Vector2f.add(loc, target.getLocation(), loc);

                Color c2 = Misc.scaleColor(color, factor);
                //c2 = color;
                Misc.addHitGlow(target.getContainingLocation(), loc, vel, glowSize, c2);
                added++;

                if (i == 0) {

                    dist = Misc.getDistance(loc, Global.getSector().getPlayerFleet().getLocation());
                    if (dist < HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE) {

                        float volumeMult = 1f - (dist / HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE);
                        volumeMult = (float) Math.sqrt(volumeMult);
                        volumeMult *= 0.1f * factor;

                        if (volumeMult > 0) {
                            Global.getSoundPlayer().playSound("mine_explosion", 1f, volumeMult, loc, Misc.ZERO);
                        }

                    }

                }
            }
        }
    }
}