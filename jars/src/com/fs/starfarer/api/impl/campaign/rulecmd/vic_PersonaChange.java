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
import com.fs.starfarer.api.impl.campaign.ui.vic_portraitSelectUiPlugin;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.campaign.CommDirectory;
import com.fs.starfarer.rpg.Person;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;

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

    public static float
            respecOfficerXP = 0.9f;

    public static int
            respecPlayerCost = 50000,
            respecOfficerCost = 100000;


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
            temp.portrait = null;
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
                showPortraitSelector(FullName.Gender.FEMALE);
                break;
            case "vic_PersonaChangeMale":
                showPortraitSelector(FullName.Gender.MALE);
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
        dialog.getVisualPanel().hideSecondPerson();

        for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
            officer.getPerson().removeTag("vic_personToChangeOfficer");
        }

        for (AdminData admin : Global.getSector().getCharacterData().getAdmins()) {
            admin.getPerson().removeTag("vic_personToChangeAdmin");
        }
        ((RuleBasedDialog)dialog.getPlugin()).updateMemory();

        options.clearOptions();
        options.addOption("Consider changing something about yourself", changeSelf);
        options.addOption("Consider changing your officer", changeOfficer);
        options.addOption("Consider changing your administrator", changeAdmin);


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
                if (Misc.isUnremovable(officer.getPerson())) continue;
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
        if (entity.getActivePerson().hasTag("vic_personToChangeAdmin") || entity.getActivePerson().hasTag("vic_personToChangeOfficer")){
            temp.personaToChange = entity.getActivePerson();
            temp.isPlayer = false;
            PersonaChangeMenu();
        }
    }

    //generate menu
    protected void PersonaChangeMenu() {

        dialog.getVisualPanel().hideSecondPerson();
        options.clearOptions();

        options.addOption("Open \"Male\" section of the appearances list", male);
        options.addOption("Open \"Female\" section of the appearances list", female);
        if (currState == mainMenuState.InToOfficer)
            options.addOption("Revitalize the current body (Respec the Skill Points) ", respec);

        if (playerCargo.getCredits().get() < 10000) {
            options.setEnabled(male, false);
            options.setTooltip(male, "Not enough credits.");
            options.setEnabled(female, false);
            options.setTooltip(female, "Not enough credits.");
        }

        switch (currState) {
            case InToYou:
                if (playerCargo.getCredits().get() < respecPlayerCost) {
                    options.setEnabled(respec, false);
                    options.setTooltip(respec, "Not enough credits.");
                }
                break;
            case InToOfficer:
                if (playerCargo.getCredits().get() < respecOfficerCost) {
                    options.setEnabled(respec, false);
                    options.setTooltip(respec, "Not enough credits.");
                }
                break;
        }


        options.addOption("Re-think your choice", backToChoose);
        options.setShortcut(backToChoose,1,false,false,false,false);

    }

    //comms
    protected void showPortraitSelector(FullName.Gender gender) {
        dialog.showCustomDialog(800, 500, new vic_portraitSelectUI(dialog, gender));
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

        Color h = Misc.getHighlightColor();
        Color b = Misc.getNegativeHighlightColor();
        switch (currState) {
            case InToYou:
                text.addPara("The manager nods affirmatively and after giving you some more information about internal organic reconfiguration, informs you that the procedure will cost %s.",
                        h, respecPlayerCost + " credits");
                break;
            case InToOfficer:
                text.addPara("The manager nods affirmatively and after giving you some more information about internal organic reconfiguration, informs you that the procedure will cost %s. Additionally, both you and your subordinate are warned about possible side effects," +
                                " the most significant of which is partial memory loss (which will result in a loss of %s of their current experience).",
                        h, respecOfficerCost + " credits", Math.round(100 - respecOfficerXP * 100) + "%");
                break;
        }

        options.addOption("Confirm your choice", respecResult);

        options.addOption("Re-think your choice", backToChoose);
        options.setShortcut(backToChoose,1,false,false,false,false);
    }

    protected void PersonaChangeRespecResult(){
        switch (currState){
            case InToYou:
                playerCargo.getCredits().subtract(respecPlayerCost);
                AddRemoveCommodity.addCreditsLossText(respecPlayerCost, text);
                respecPlayer();
                break;
            case InToOfficer:
                respecOfficer(playerFleet.getFleetData().getOfficerData(temp.personaToChange), playerFleet);
                playerCargo.getCredits().subtract(respecOfficerCost);
                AddRemoveCommodity.addCreditsLossText(respecOfficerCost, text);
                break;
        }



        if (!temp.isPlayer){
            for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
                officer.getPerson().removeTag("vic_personToChangeOfficer");
            }
            for (AdminData admin : Global.getSector().getCharacterData().getAdmins()) {
                admin.getPerson().removeTag("vic_personToChangeAdmin");
                admin.getPerson().getMemory().advance(1);
            }
        }
        ((RuleBasedDialog)dialog.getPlugin()).updateMemory();

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

        dialog.getVisualPanel().hideSecondPerson();
        playerCargo.getCredits().subtract(10000);
        AddRemoveCommodity.addCreditsLossText(10000, text);


        //text.addPara("change result");

        ((RuleBasedDialog)dialog.getPlugin()).notifyActivePersonChanged();

        if (temp.isPlayer){
            Global.getSector().getCharacterData().setPortraitName(temp.portrait);
        } else {
            for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
                officer.getPerson().removeTag("vic_personToChangeOfficer");
            }
            for (AdminData admin : Global.getSector().getCharacterData().getAdmins()) {

                for (String tag : admin.getPerson().getTags()){
                    text.addPara(tag);
                }
                admin.getPerson().removeTag("vic_personToChangeAdmin");
                text.addPara(admin.getPerson().getName().getFullName());
                for (String tag : admin.getPerson().getTags()){
                    text.addPara(tag);
                }
            }
            temp.personaToChange.setPortraitSprite(temp.portrait);
            temp.personaToChange.setGender(temp.gender);
        }
        ((RuleBasedDialog)dialog.getPlugin()).updateMemory();

        dialog.getInteractionTarget().getMarket().getMemoryWithoutUpdate().advance(1f);
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

    private void respecPlayer(){
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
                player.addPoints(1);
                skillRefunded += 1;
            }
        }

        player.refreshCharacterStatsEffects();
    }

    private static void respecOfficer(OfficerDataAPI toRespec, CampaignFleetAPI sourceFleet){

        // Technically it should be called cloneOfficer(), but whatever...
        final PersonAPI
                oldPerson = toRespec.getPerson(),
                newPerson = OfficerManagerEvent.createOfficer(oldPerson.getFaction(), 1, OfficerManagerEvent.SkillPickPreference.ANY,
                        false, sourceFleet, false, false, -1, MathUtils.getRandom());

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


        /*
        // Show skills that were reset
        final List<MutableCharacterStatsAPI.SkillLevelAPI> skills = oldPerson.getStats().getSkillsCopy();
        Collections.sort(skills, new SkillLevelComparator());

         */


        // Set the officer's person to the new copy and give it the proper amount of experience
        toRespec.setPerson(newPerson);
        toRespec.getPerson().getStats().setSkillLevel(toRespec.getPerson().getStats().getSkillsCopy().get(0).getSkill().getId(), 1);
        if (ship != null) ship.setCaptain(newPerson);
        toRespec.addXP((long) (oldPerson.getStats().getXP() * respecOfficerXP));
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
        public String portrait;
        public FullName.Gender gender;
    }

    public class vic_portraitSelectUI implements CustomDialogDelegate {

        public TooltipMakerAPI UI;
        public PositionAPI highlight;
        public InteractionDialogAPI dialog;
        public FullName.Gender gender;

        public vic_portraitSelectUI(InteractionDialogAPI dialog, FullName.Gender gender){
            this.dialog = dialog;
            this.gender = gender;
        }


        HashMap<ButtonAPI, String> buttons = new HashMap<>();
        //List<ButtonAPI> buttons = new ArrayList<>();

        @Override
        public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
            float width = 800;
            float height = 500;

            UI = panel.createUIElement(width,height, true);

            float imageSize = 128;
            float imagePad = 4;
            int column = 0;
            int row = 0;

            UI.addSpacer(imagePad);
            UI.addImage("graphics/hud/minimap_bg2.png", 0,0,0);
            highlight = UI.getPrev().getPosition();
            highlight.setSize(imageSize + imagePad,imageSize+ imagePad);
            highlight.inTL(-333,-333);

            for (String s : Global.getSector().getPlayerFaction().getPortraits(gender).getItems()) {

                if (column == 6){
                    row++;
                    column = 0;
                    UI.addSpacer(imagePad);
                }
                float size = 0;
                if (column == 0) size = imageSize;


                ButtonAPI button = UI.addAreaCheckbox("",null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),size,size,0);

                button.getPosition().setSize(imageSize,imageSize);
                button.getPosition().inTL((imageSize + imagePad) * column + 2, (imageSize + imagePad) * row + 2);
                button.getPosition().setLocation((imageSize + imagePad) * column + 2, (imageSize + imagePad) * row + 2);

                UI.addImage(s,0,0,0);
                UI.getPrev().getPosition().setSize(imageSize,imageSize);
                UI.getPrev().getPosition().inTL((imageSize + imagePad) * column + 2, (imageSize + imagePad) * row + 2);
                UI.getPrev().getPosition().setLocation((imageSize + imagePad) * column + 2, (imageSize + imagePad) * row + 2);


                buttons.put(button,s);
                column++;
            }
            UI.addSpacer(imagePad);
            panel.addUIElement(UI).inTL(0f, 0f);
        }



        @Override
        public boolean hasCancelButton() {
            return true;
        }

        @Override
        public String getConfirmText() {
            return "Confirm";
        }

        @Override
        public String getCancelText() {
            return "Close";
        }

        @Override

        public void customDialogConfirm() {
            String sprite = null;
            for (Map.Entry<ButtonAPI, String> pair : buttons.entrySet()) {
                ButtonAPI button = pair.getKey();
                if (button.isChecked()) {
                    sprite = pair.getValue();
                    break;
                }
            }
            if (sprite == null) return;
            PersonAPI person =  new Person();
            person.setPortraitSprite(sprite);
            //dialog.getVisualPanel().showPersonInfo(person,true,false);
            dialog.getVisualPanel().hideSecondPerson();
            dialog.getVisualPanel().showSecondPerson(person);
            temp.portrait = sprite;
            temp.gender = gender;
            PersonaChangeConfirm();
        }

        @Override
        public void customDialogCancel() {

        }

        @Override
        public CustomUIPanelPlugin getCustomPanelPlugin() {
            return new vic_portraitSelectUiPlugin(buttons, this);
        }
    }
}
