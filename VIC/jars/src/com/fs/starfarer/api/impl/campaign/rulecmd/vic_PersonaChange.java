package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.campaign.CommDirectory;
import com.fs.starfarer.launcher.ModManager;
import com.fs.starfarer.rpg.Person;
import data.scripts.utilities.StringHelper;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class vic_PersonaChange extends BaseCommandPlugin {

    public static Logger log = Global.getLogger(vic_PersonaChange.class);
    public String
            male = "vic_PersonaChangeMale",
            female = "vic_PersonaChangeFemale",
            result = "vic_PersonaChangeResult",
            NEX_GO_BACK = "vic_PersonaChangeEnd",
            GO_BACK = "vic_PersonaChangeEnd";
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

    public vic_PersonaChange() {
    }

    public vic_PersonaChange(SectorEntityToken entity) {
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
            case "vic_PersonaChangeFemale":
                CommsSummon(FullName.Gender.FEMALE);
                break;
            case "vic_PersonaChangeMale":
                CommsSummon(FullName.Gender.MALE);
                break;
            case "vic_PersonaChangeConfirm":
                PersonaChangeConfirm();
                break;
            case "vic_PersonaChangeMenu":
                PersonaChangeMenu();
                break;
            case "vic_PersonaChangeResult":
                PersonaChangeResult();
                break;
        }

        return true;
    }

    //generate menu
    protected void PersonaChangeMenu() {

        text.addPara(StringHelper.getString("vic_PersonaChange", "Choose gender"));

        options.clearOptions();

        options.addOption("Male", male);
        options.addOption("Female", female);

        if (playerCargo.getCredits().get() < 10000) {
            options.setEnabled(male, false);
            options.setTooltip(male, "Not enough credits.");
            options.setEnabled(female, false);
            options.setTooltip(female, "Not enough credits.");
        }

        if (ModManager.getInstance().isModEnabled("nexerelin")) {
            options.addOption("Never mind", NEX_GO_BACK);
        } else {
            options.addOption("Never mind", GO_BACK);
        }
    }

    //comms
    protected void CommsSummon(FullName.Gender gender) {

        String rank = Global.getSector().getPlayerPerson().getRankId();
        CommDirectory directory = new CommDirectory();
        int number = 0;
        for (String s : Global.getSector().getPlayerFaction().getPortraits(gender).getItems()) {
            Person dude = new Person();
            dude.setPortraitSprite(s);
            dude.setRankId(null);
            dude.addTag("vic_PortraitHolder");
            dude.setName(new FullName(number + "-X", gender.name(), gender));
            directory.addPerson(dude);
            number++;
        }

        dialog.showCommDirectoryDialog(directory);
    }

    //portrait menu
    protected void PersonaChangeConfirm() {

        text.addPara(StringHelper.getString("vic_PersonaChange", "Confirm"));

        options.clearOptions();

        options.addOption("Confirm", result);

        options.addOption("Never mind", GO_BACK);
    }

    //result screen
    protected void PersonaChangeResult() {

        text.addPara(StringHelper.getString("vic_PersonaChange", "Result"));

        playerCargo.getCredits().subtract(10000);
        AddRemoveCommodity.addCreditsLossText(10000, text);

        Global.getSector().getCharacterData().setPortraitName(entity.getActivePerson().getPortraitSprite());
        //Global.getSector().getCharacterData().setName(entity.getActivePerson().getName().getFirst(), entity.getActivePerson().getGender());

        options.clearOptions();

        options.addOption("Continue", GO_BACK);
    }

}
