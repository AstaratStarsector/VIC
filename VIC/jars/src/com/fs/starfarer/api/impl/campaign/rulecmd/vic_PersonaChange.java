package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.campaign.CommDirectory;
import com.fs.starfarer.launcher.ModManager;
import com.fs.starfarer.rpg.Person;
import data.scripts.utilities.StringHelper;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.awt.*;
import java.util.*;
import java.util.List;

public class vic_PersonaChange extends BaseCommandPlugin {

    public static Logger log = Global.getLogger(vic_PersonaChange.class);
    protected static vic_personaChangeData temp = new vic_personaChangeData();
    public String
            male = "vic_PersonaChangeMale",
            female = "vic_PersonaChangeFemale",
            respec = "vic_PersonaChangeRespec",
            result = "vic_PersonaChangeResult",
            respecResult = "vic_PersonaChangeRespecResult",

            GO_BACK = "vic_PersonaChangeEnd",
            exitBenInYou = "vic_PersonaChangeEndYou",
            exitBenInOfficer = "vic_PersonaChangeEndOfficer",
            exitBenInAdmin = "vic_PersonaChangeEndAdmin",

            backToChoose = "vic_PerconaChangeChoseBack",

            exitResult = "vic_PersonaChangeEndNew",
            exitResultOfficer = "vic_PersonaChangeEndNewOfficer",
            exitResultAdmin = "vic_PersonaChangeEndNewAdmin",

            changeSelf = "vic_PersonaChangeYou",
            changeOfficer = "vic_PersonaChangeOfficer",
            changeAdmin = "vic_PersonaChangeAdmin";

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

    protected static void resetTmp() {
        if (temp != null) {
            temp.isPlayer = true;
            temp.personaToChange = null;
        }

    }



    protected enum mainMenuState {
        justEntered,
        InToYou,
        InToOfficer,
        InToAdmin
    }

    protected static mainMenuState currState;


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
            case "vic_PerconaChangeChose":
                currState = mainMenuState.justEntered;
                PerconaChangeChose();
                break;
            case "vic_PerconaChangeChoseBack":
                PerconaChangeChose();
                break;
            case "vic_PersonaChangeYou":
                currState = mainMenuState.InToYou;
                temp.isPlayer = true;
                PersonaChangeMenu();
                break;
            case "vic_PersonaChangeOfficer":
                currState = mainMenuState.InToOfficer;
                PerconaChangeChosePersona(true);
                break;
            case "vic_PersonaChangeAdmin":
                currState = mainMenuState.InToAdmin;
                PerconaChangeChosePersona(false);
                break;
            case "vic_PerconaChangeNotPLayer":
                PerconaChangeNotPLayer();
                break;
            case "vic_PersonaChangeMenu":
                PersonaChangeMenu();
                break;
            case "vic_PersonaChangeFemale":
                CommsSummon(FullName.Gender.FEMALE);
                break;
            case "vic_PersonaChangeMale":
                CommsSummon(FullName.Gender.MALE);
                break;
            case "vic_PersonaChangeRespec":
                PersonaChangeRespecConfirm();
                break;
            case "vic_PersonaChangeRespecResult":
                PersonaChangeRespecResult();
                break;
            case "vic_PersonaChangeConfirm":
                PersonaChangeConfirm();
                break;
            case "vic_PersonaChangeResult":
                PersonaChangeResult();
                break;
        }

        return true;
    }



    protected void PerconaChangeChose() {
        resetTmp();

        for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
            officer.getPerson().removeTag("vic_personToChange");
        }

        for (AdminData admin : Global.getSector().getCharacterData().getAdmins()) {
            admin.getPerson().removeTag("vic_personToChange");
        }

        options.clearOptions();
        options.addOption("Change your own appearance", changeSelf);
        options.addOption("Change the officer's appearance", changeOfficer);
        options.addOption("Change the administrator's appearance", changeAdmin);


        switch (currState){
            case justEntered:
                options.addOption("Leave the Centre", GO_BACK);
                options.setShortcut(GO_BACK,1,false,false,false,false);
                break;
            case InToYou:
                options.addOption("Leave the Centre", exitBenInYou );
                options.setShortcut(exitBenInYou,1,false,false,false,false);
                break;
            case InToOfficer:
                options.addOption("Leave the Centre", exitBenInOfficer);
                options.setShortcut(exitBenInOfficer,1,false,false,false,false);
                break;
            case InToAdmin:
                options.addOption("Leave the Centre", exitBenInAdmin);
                options.setShortcut(exitBenInAdmin,1,false,false,false,false);
                break;
        }

        if (Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy().isEmpty()) {
            options.setEnabled(changeOfficer, false);
            options.setTooltip(changeOfficer, "You don't have any officers.");
        }
        if (Global.getSector().getCharacterData().getAdmins().isEmpty()) {
            options.setEnabled(changeAdmin, false);
            options.setTooltip(changeAdmin, "You don't have any administrators.");
        }
    }

    protected void PerconaChangeChosePersona(boolean isItOfficer) {
        CommDirectory directory = new CommDirectory();
        if (isItOfficer) {
            for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
                officer.getPerson().addTag("vic_personToChangeOfficer");
                directory.addPerson(officer.getPerson());
            }
        } else {
            for (AdminData admin : Global.getSector().getCharacterData().getAdmins()) {
                admin.getPerson().addTag("vic_personToChangeAdmin");
                directory.addPerson(admin.getPerson());
            }
        }
        dialog.showCommDirectoryDialog(directory);
    }

    protected void PerconaChangeNotPLayer() {
        temp.personaToChange = entity.getActivePerson();
        temp.isPlayer = false;
        PersonaChangeMenu();
    }

    //generate menu
    protected void PersonaChangeMenu() {

        options.clearOptions();

        options.addOption("Open \"Male\" section", male);
        options.addOption("Open \"Female\" section", female);
        if (currState != mainMenuState.InToAdmin)
            options.addOption("Respec skills", respec);

        if (playerCargo.getCredits().get() < 10000) {
            options.setEnabled(male, false);
            options.setTooltip(male, "Not enough credits.");
            options.setEnabled(female, false);
            options.setTooltip(female, "Not enough credits.");
            options.setEnabled(respec, false);
            options.setTooltip(respec, "Not enough credits.");
        }


        options.addOption("Re-think your choice", backToChoose);
        options.setShortcut(backToChoose,1,false,false,false,false);

    }

    //comms
    protected void CommsSummon(FullName.Gender gender) {

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

        options.clearOptions();

        options.addOption("Confirm your choice", result);

        options.addOption("Return to the \"Male\" section", male);
        options.addOption("Return to the \"Female\" section", female);

        options.addOption("Re-think your choice", backToChoose);
        options.setShortcut(backToChoose,1,false,false,false,false);
    }

    protected void PersonaChangeRespecConfirm(){
        options.clearOptions();

        options.addOption("Confirm your choice", respecResult);

        options.addOption("Re-think your choice", backToChoose);
        options.setShortcut(backToChoose,1,false,false,false,false);
    }

    protected void PersonaChangeRespecResult(){
        switch (currState){
            case InToYou:
                respecPlayer();
                break;
            case InToOfficer:
                respecOfficer(playerFleet.getFleetData().getOfficerData(temp.personaToChange), playerFleet);
                break;
        }

        playerCargo.getCredits().subtract(10000);
        AddRemoveCommodity.addCreditsLossText(10000, text);

        if (!temp.isPlayer){
            for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
                officer.getPerson().removeTag("vic_personToChange");
            }
            for (AdminData admin : Global.getSector().getCharacterData().getAdmins()) {
                admin.getPerson().removeTag("vic_personToChange");
            }
        }

        options.clearOptions();
        switch (currState){
            case InToYou:
                options.addOption("Leave the Centre", exitResult);
                options.setShortcut(exitResult,1,false,false,false,false);
                break;
            case InToOfficer:
                options.addOption("Leave the Centre", exitResultOfficer);
                options.setShortcut(exitResultOfficer,1,false,false,false,false);
                break;
            case InToAdmin:
                options.addOption("Leave the Centre", exitResultAdmin);
                options.setShortcut(exitResultAdmin,1,false,false,false,false);
                break;
        }
    }



    //result screen
    protected void PersonaChangeResult() {

        playerCargo.getCredits().subtract(10000);
        AddRemoveCommodity.addCreditsLossText(10000, text);

        if (temp.isPlayer)
            Global.getSector().getCharacterData().setPortraitName(entity.getActivePerson().getPortraitSprite());
        else {
            temp.personaToChange.setPortraitSprite(entity.getActivePerson().getPortraitSprite());
            for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
                officer.getPerson().removeTag("vic_personToChange");
            }
            for (AdminData admin : Global.getSector().getCharacterData().getAdmins()) {
                admin.getPerson().removeTag("vic_personToChange");
            }
        }
        //Global.getSector().getCharacterData().setName(entity.getActivePerson().getName().getFirst(), entity.getActivePerson().getGender());

        options.clearOptions();

        options.addOption("Return to the \"Male\" section of the list", male);
        options.addOption("Return to the \"Female\" section of the list", female);

        switch (currState){
            case InToYou:
                options.addOption("Leave the Centre", exitResult);
                options.setShortcut(exitResult,1,false,false,false,false);
                break;
            case InToOfficer:
                options.addOption("Leave the Centre", exitResultOfficer);
                options.setShortcut(exitResultOfficer,1,false,false,false,false);
                break;
            case InToAdmin:
                options.addOption("Leave the Centre", exitResultAdmin);
                options.setShortcut(exitResultAdmin,1,false,false,false,false);
                break;
        }
    }

    private static void respecPlayer(){
        final MutableCharacterStatsAPI player = Global.getSector().getPlayerPerson().getStats();
        int aptRefunded = 0;
        for (final String aptitude : Global.getSettings().getAptitudeIds())
        {
            final int total = (int) player.getAptitudeLevel(aptitude);
            if (total > 0)
            {
                player.setAptitudeLevel(aptitude, 0f);
                player.addPoints(total);
                aptRefunded += total;
            }
        }

        // Refund skills
        int skillRefunded = 0;
        for (final String skill : Global.getSettings().getSortedSkillIds())
        {
            // Ignore aptitudes (included in list because officers treat them as skills)
            if (Global.getSettings().getSkillSpec(skill).isAptitudeEffect())
            {
                continue;
            }

            final int total = (int) player.getSkillLevel(skill);
            if (total > 0)
            {
                player.setSkillLevel(skill, 0f);
                player.addPoints(total);
                skillRefunded += total;
            }
        }

        player.refreshCharacterStatsEffects();
    }

    private static void respecOfficer(OfficerDataAPI toRespec, CampaignFleetAPI sourceFleet)
    {
        // Technically it should be called cloneOfficer(), but whatever...
        final PersonAPI oldPerson = toRespec.getPerson(),
                newPerson = OfficerManagerEvent.createOfficer(oldPerson.getFaction(), 1, false);
        final FleetMemberAPI ship = sourceFleet.getFleetData().getMemberWithCaptain(oldPerson);

        // Copy the old person's memory to the new person
        final MemoryAPI oldMemory = oldPerson.getMemory(), newMemory = newPerson.getMemory();
        newMemory.clear();
        for (String key : oldMemory.getKeys())
        {
            if (oldMemory.getExpire(key) != 0f)
            {
                newMemory.set(key, oldMemory.get(key), oldMemory.getExpire(key));
            }
            else
            {
                newMemory.set(key, oldMemory.get(key));
            }
        }

        // Copy required status of any memory keys
        for (String key : oldMemory.getKeys())
        {
            final Set<String> required = oldMemory.getRequired(key);
            if (!required.isEmpty())
            {
                for (String rKey : required) newMemory.addRequired(key, rKey);
            }
        }

        // Copy traits from old person
        newPerson.setAICoreId(oldPerson.getAICoreId());
        newPerson.setContactWeight(oldPerson.getContactWeight());
        newPerson.setFaction(oldPerson.getFaction().getId());
        newPerson.setName(oldPerson.getName());
        newPerson.setPersonality(oldPerson.getPersonalityAPI().getId());
        newPerson.setPortraitSprite(oldPerson.getPortraitSprite());
        newPerson.setPostId(oldPerson.getPostId());
        newPerson.setRankId(oldPerson.getRankId());
        newPerson.getRelToPlayer().setRel(oldPerson.getRelToPlayer().getRel());

        // Copy any tags from the old person
        newPerson.getTags().clear();
        for (String tag : oldPerson.getTags()) newPerson.addTag(tag);


        // Show skills that were reset
        final List<MutableCharacterStatsAPI.SkillLevelAPI> skills = oldPerson.getStats().getSkillsCopy();
        Collections.sort(skills, new SkillLevelComparator());

        // Notify player of respecced skills
        int totalRefunded = 0;
        /*
        for (MutableCharacterStatsAPI.SkillLevelAPI skill : skills)
        {
            final int refunded = (int) skill.getLevel();
            if (refunded > 0)
            {
                Console.showMessage(" - removed " + refunded + " points from " + (skill.getSkill().isAptitudeEffect()
                        ? "aptitude " : "skill ") + skill.getSkill().getId());
                totalRefunded += refunded;
            }
        }

         */


        // Set the officer's person to the new copy and give it the proper amount of experience
        toRespec.setPerson(newPerson);
        if (ship != null) ship.setCaptain(newPerson);
        toRespec.addXP(oldPerson.getStats().getXP());
        newPerson.getStats().refreshCharacterStatsEffects();
    }

    private static class SkillLevelComparator implements Comparator<MutableCharacterStatsAPI.SkillLevelAPI>
    {
        @Override
        public int compare(MutableCharacterStatsAPI.SkillLevelAPI o1, MutableCharacterStatsAPI.SkillLevelAPI o2)
        {
            final SkillSpecAPI skill1 = o1.getSkill(), skill2 = o2.getSkill();
            if (skill1.isAptitudeEffect() && !skill2.isAptitudeEffect())
            {
                return -1;
            }
            else if (skill2.isAptitudeEffect() && !skill1.isAptitudeEffect())
            {
                return 1;
            }
            else
            {
                return skill1.getId().compareTo(skill2.getId());
            }
        }
    }

    protected static class vic_personaChangeData {
        public boolean isPlayer;
        public PersonAPI personaToChange;
    }
}
