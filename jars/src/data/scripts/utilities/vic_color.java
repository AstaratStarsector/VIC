package data.scripts.utilities;

import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class vic_color {

    public static Color randomizeColor(Color color, float magnitude){
        float colorMax = 1 + magnitude;
        float colorMin = 1 - magnitude;
        int red = MathUtils.clamp(Math.round(MathUtils.getRandomNumberInRange(color.getRed() * colorMin, color.getRed() * colorMax)),0, 255);
        int green = MathUtils.clamp(Math.round(MathUtils.getRandomNumberInRange(color.getGreen() * colorMin, color.getGreen() * colorMax)),0, 255);
        int blue = MathUtils.clamp(Math.round(MathUtils.getRandomNumberInRange(color.getBlue() * colorMin, color.getBlue() * colorMax)),0, 255);

        return new Color(red, green, blue);
    }
}
