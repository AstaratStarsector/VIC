package com.fs.starfarer.api.impl.campaign.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.rpg.Person;

import java.util.HashMap;
import java.util.Map;

public class vic_portraitSelectUI implements CustomDialogDelegate {

    TooltipMakerAPI UI;
    PositionAPI highlight;
    public InteractionDialogAPI dialog;

    public vic_portraitSelectUI(InteractionDialogAPI dialog){
        this.dialog = dialog;
    }


    HashMap<ButtonAPI, String> buttons = new HashMap<>();
    //List<ButtonAPI> buttons = new ArrayList<>();


    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {


        float pad = 3f;
        float spad = 5f;
        float opad = 10f;

        float width = 800;
        float height = 500;

        UI = panel.createUIElement(width,height, true);



        float spacerHeight = 5f;
        final float backgroundBoxWidth = width - 10f;
        boolean doOnce = true;

        float imageSize = 128;
        float imagePad = 4;
        int column = 0;
        int row = 0;

        UI.addSpacer(imagePad);
        UI.addImage("graphics/hud/minimap_bg2.png", 0,0,0);
        highlight = UI.getPrev().getPosition();
        highlight.setSize(imageSize + imagePad,imageSize+ imagePad);
        highlight.inTL(-333,-333);

        for (String s : Global.getSector().getPlayerFaction().getPortraits(FullName.Gender.FEMALE).getItems()) {

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
            //UI.addImage(s, 128, 128, pad);
            //TooltipMakerAPI image = UI.beginImageWithText(s, 128);
            //UI.addImageWithText(5);
            //UI.getPrev().getPosition().setYAlignOffset(128 * row);
            //UI.getPrev().getPosition().setXAlignOffset(128 * column);
            //UI.getPrev().getPosition().setSize(0,0);
            column++;
        }
        UI.addSpacer(imagePad);


        //UI.bringComponentToTop(UI.getPrev());
        //UI.addImage("graphics/portraits/portrait_hegemony01.png", 128,0);
        /*
        for (ButtonAPI button : buttons){
            if (column == 5){
                row++;
                column = 0;
            }
            button.getPosition().setLocation(128 * column, 128 * row);
            column++;
        }
        */
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
        dialog.getVisualPanel().showThirdPerson(person);
    }

    @Override
    public void customDialogCancel() {

    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return null;
    }
}
