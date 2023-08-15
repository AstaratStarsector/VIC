package com.fs.starfarer.api.impl.campaign.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.vic_PersonaChange;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class vic_portraitSelectUiPlugin implements CustomUIPanelPlugin {

    vic_PersonaChange.vic_portraitSelectUI delegate;
    HashMap<ButtonAPI, String> buttons;
    ButtonAPI lastActive = null;

    public vic_portraitSelectUiPlugin(HashMap<ButtonAPI, String> buttons, vic_PersonaChange.vic_portraitSelectUI delegate){
        this.buttons = buttons;
        this.delegate = delegate;
    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
        /*
        for (Map.Entry<ButtonAPI, String> pair : buttons.entrySet()){
            SpriteAPI image = Global.getSettings().getSprite(pair.getValue());
            image.setColor(new Color(125,125,125,255));
            if (pair.getKey().isHighlighted()) image.setColor(new Color(200,200,200,255));
            //if (pair.getKey().isChecked()) image.setColor(new Color(255,255,255,255));
            image.renderAtCenter(pair.getKey().getPosition().getX() + 64,pair.getKey().getPosition().getY() + 64);
        }

         */
    }

    @Override
    public void advance(float amount) {
        for (Map.Entry<ButtonAPI, String> pair : buttons.entrySet()){
            ButtonAPI button = pair.getKey();
            if (button.isChecked()){
                if (!button.equals(lastActive)){
                    if (lastActive != null) lastActive.setChecked(false);
                    lastActive = button;
                    break;
                }
            }
        }

        float imageSize = 128;
        float imagePad = 4;
        int column = 0;
        int row = 0;

        Global.getLogger(vic_portraitSelectUiPlugin.class).info("highlighted");
        if (delegate.highlight == null) return;
        delegate.highlight.inTL(-333,-333);
        for (Map.Entry<ButtonAPI, String> pair : buttons.entrySet()){
            if (column == 6){
                row++;
                column = 0;
            }
            if (pair.getKey().isChecked()){
                delegate.highlight.belowMid(pair.getKey(),-130);
                //delegate.highlight.inTL(pair.getKey().getPosition()., pair.getKey().getPosition().getY());
                return;
            }
            column++;
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
