package data.scripts.utilities;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONException;
import org.magiclib.util.MagicSettings;

import java.io.IOException;

public class vic_getSettings {

    static final String modID = "vic";

    public static boolean getBoolean(String key) throws JSONException, IOException {
        boolean value = false;
        try {
            if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
                value = Boolean.TRUE.equals(LunaSettings.getBoolean(modID, key));
            } else {
                value = MagicSettings.getBoolean(modID, key);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return value;
    }

    public static Integer getInt(String key) throws JSONException, IOException {
        Integer value = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            value = LunaSettings.getInt(modID, key);
        } else {
            value = MagicSettings.getInteger(modID, key);
        }
        return value;
    }

    public static String getString(String key) throws JSONException, IOException {
        String value = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            value = LunaSettings.getString(modID, key);
        } else {
            value = MagicSettings.getString(modID, key);
        }
        return value;
    }
}
