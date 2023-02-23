package data.scripts.utilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

public class StringHelper {

    public static String getString(String category, String id, boolean ucFirst) {
        String str = "";
        try {
            str = Global.getSettings().getString(category, id);
        } catch (Exception ex) {
            // could be a string not found
            //str = ex.toString();  // looks really silly
            Global.getLogger(StringHelper.class).warn(ex);
            return "[INVALID]" + id;
        }
        if (ucFirst) str = Misc.ucFirst(str);
        return str;
    }

    public static String getString(String category, String id) {
        return getString(category, id, false);
    }

}
