package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
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
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.campaign.ui.UITable;
import com.fs.starfarer.campaign.util.UIIndicator;
import com.fs.starfarer.launcher.ModManager;
import data.scripts.utilities.StringHelper;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import sun.text.normalizer.UCharacterIterator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VIC_ChangePersonality extends BaseCommandPlugin {

    public static Logger log = Global.getLogger(VIC_ChangePersonality.class);
    public static String
            ENGAGE = "mktEngage",
            UMOMenu = "UMOMenu",
            VBombMenu = "VBombMenu",
            VBombConfirm = "VBombConfirm",
            VBombResults = "VBombResults",
            GO_BACK = "mktGoBack",
            NEX_GO_BACK = "marketConsiderHostile",
            MOD_TO_CHECK = "vic_vBombHmod";
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

    public VIC_ChangePersonality() {
    }

    public VIC_ChangePersonality(SectorEntityToken entity) {
        init(entity);
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
            case "ChangePortrait":
                ChangePortrait();
                break;
        }

        return true;

    }

    protected void ChangePortrait (){

        for (String s : Global.getSector().getPlayerFaction().getPortraits(FullName.Gender.ANY).getItems())
            text.addPara( s + "");
        Global.getSector().getPlayerPerson().setPortraitSprite(Global.getSector().getPlayerFaction().getPortraits(FullName.Gender.ANY).pick(new Random()));
        
    }

}
