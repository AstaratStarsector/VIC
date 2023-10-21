package data.scripts.utilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;
import org.magiclib.util.MagicSettings;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class vic_trailSpawner {

    static Logger log = Global.getLogger(vic_trailSpawner.class);

    public static final Map<String, List<vic_trailData>> VIC_TRAILS_LIST = new HashMap();

    public static void createTrailSegment(vic_trailData trailData, Vector2f spawnPosition, float trailID, Vector2f vel, Vector2f sidewayVel, CombatEntityAPI linkedEntity, float angle) {


        SpriteAPI spriteToUse = Global.getSettings().getSprite("fx", trailData.sprite);


        //Sideway offset velocity, for projectiles that use it
        /*
        Vector2f projBodyVel = new Vector2f(projVel);
        projBodyVel = VectorUtils.rotate(projBodyVel, -proj.getFacing());
        Vector2f projLateralBodyVel = new Vector2f(0f, projBodyVel.getY());
        Vector2f sidewayVel = new Vector2f(projLateralBodyVel);
        sidewayVel = (Vector2f) VectorUtils.rotate(sidewayVel, proj.getFacing()).scale(trailData.drift);

         */

        //random dispersion of the segments if necessary
        float rotationIn = trailData.rotationIn;
        float rotationOut = trailData.rotationOut;

        float velIn = trailData.velocityIn;
        float velOut = trailData.velocityOut;

        if (trailData.randomRotation) {
            float rand = MathUtils.getRandomNumberInRange(-1f, 1f);
            rotationIn = rotationIn * rand;
            rotationOut = rotationOut * rand;
        }

        if (trailData.dispersion > 0) {
            Vector2f.add(
                    sidewayVel,
                    MathUtils.getRandomPointInCircle(null, trailData.dispersion),
                    sidewayVel);
        }

        if (trailData.randomVelocity > 0) {
            float rand = MathUtils.getRandomNumberInRange(-1f, 1f);
            velIn *= 1 + trailData.randomVelocity * rand;
            velOut *= 1 + trailData.randomVelocity * rand;
        }

        //Opacity adjustment for fade-out, if the projectile uses it
        /*
        float opacityMult = 1f;
        if (trailData.fadeOnFadeOut && proj.isFading()) {
            opacityMult = Math.max(0, Math.min(1, proj.getDamageAmount() / proj.getBaseDamageAmount()));
        }*/

        //Then, actually spawn a trail
        MagicTrailPlugin.addTrailMemberAdvanced(
                linkedEntity,
                trailID,
                spriteToUse,
                spawnPosition,
                velIn,
                velOut,
                angle,
                rotationIn,
                rotationOut,
                trailData.sizeIn,
                trailData.sizeOut,
                trailData.colorIn,
                trailData.colorOut,
                trailData.opacity,
                trailData.fadeIn,
                trailData.duration,
                trailData.fadeOut,
                GL_SRC_ALPHA,
                trailData.blendOut,
                trailData.textLength,
                trailData.textScroll,
                trailData.textOffset,
                sidewayVel,
                null,
                trailData.layer,
                trailData.frameOffsetMult
        );
    }

    public static void getTrailData() {
        //clear up the trash
        VIC_TRAILS_LIST.clear();

        //merge all the trail
        JSONArray trailData = new JSONArray();
        try {
            trailData = Global.getSettings().loadCSV("data/trails/trail_data.csv", "vic");
        } catch (IOException | JSONException | RuntimeException ex) {
            log.error("fail to load trails");
        }

        for (int i = 0; i < trailData.length(); i++) {
            try {
                JSONObject row = trailData.getJSONObject(i);

                //check the blending first
                int blend = GL_ONE_MINUS_SRC_ALPHA;
                if (row.getBoolean("additive")) {
                    blend = GL_ONE;
                }

                //get the concerned projectile
                String thisProj = row.getString("projectile");

                //setup layer override
                CombatEngineLayers layer = CombatEngineLayers.BELOW_INDICATORS_LAYER;
                try {
                    if (row.getBoolean("renderBelowExplosions")) {
                        layer = CombatEngineLayers.ABOVE_SHIPS_LAYER;
                    }
                } catch (JSONException ex) {
//                            LOG.error("missing layer override for " + thisProj);
                }

                float frameOffsetMult = 1f;
                try {
                    frameOffsetMult = (float) row.getDouble("frameOffsetMult");
                } catch (JSONException ex) {
//                            LOG.error("missing frame offset mult override for " + thisProj);
                }

                float textureOffset = 0;
                try {
                    if (row.getBoolean("randomTextureOffset")) {
                        textureOffset = -1;
                    }
                } catch (JSONException ignored) {
//                            LOG.error("missing random texture offset boolean for " + thisProj);
                }
                vic_trailData temp = new vic_trailData(
                        row.getString("sprite"),
                        (float) row.getDouble("minLength"),
                        (float) row.getDouble("fadeIn"),
                        (float) row.getDouble("duration"),
                        (float) row.getDouble("fadeOut"),
                        (float) row.getDouble("sizeIn"),
                        (float) row.getDouble("sizeOut"),
                        MagicSettings.toColor3(row.getString("colorIn")),
                        MagicSettings.toColor3(row.getString("colorOut")),
                        (float) row.getDouble("opacity"),
                        blend,
                        (float) row.getDouble("textLength"),
                        (float) row.getDouble("textScroll"),
                        textureOffset,
                        (float) row.getDouble("distance"),
                        (float) row.getDouble("drift"),
                        row.getBoolean("fadeOnFadeOut"),
                        row.getBoolean("angleAdjustment"),
                        (float) row.getDouble("dispersion"),
                        (float) row.getDouble("velocityIn"),
                        (float) row.getDouble("velocityOut"),
                        (float) row.getDouble("randomVelocity"),
                        (float) row.getDouble("angle"),
                        (float) row.getDouble("rotationIn"),
                        (float) row.getDouble("rotationOut"),
                        row.getBoolean("randomRotation"),
                        layer,
                        frameOffsetMult
                );

                //check if there are any trail already assigned to that projectile
                if (VIC_TRAILS_LIST.containsKey(thisProj)) {
                    //add the new trail to the existing proj
                    VIC_TRAILS_LIST.get(thisProj).add(temp);
                    //log.info("added trail to " + thisProj);
                } else {
                    //add a new entry with that first trail
                    List<vic_trailData> list = new ArrayList<>();
                    list.add(temp);
                    VIC_TRAILS_LIST.put(
                            thisProj,
                            list
                    );
                    //log.info("made list for " + thisProj);
                }
            } catch (JSONException ex) {
                log.error("Invalid line, skipping");
            }
        }
    }


    public static class vic_trailData {
        final String sprite;
        final float minLength;
        final float fadeIn;
        final float duration;
        final float fadeOut;
        final float sizeIn;
        final float sizeOut;
        final Color colorIn;
        final Color colorOut;
        final float opacity;
        final int blendOut;
        final float textLength;
        final float textScroll;
        final float textOffset;
        final float distance;
        final float drift;
        final boolean fadeOnFadeOut;
        final boolean angleAdjustment;
        final float dispersion;
        final float velocityIn;
        final float velocityOut;
        final float randomVelocity;
        final float angle;
        final float rotationIn;
        final float rotationOut;
        final boolean randomRotation;
        final CombatEngineLayers layer;
        final float frameOffsetMult;

        public vic_trailData(
                String sprite,
                float minLength,
                float fadeIn,
                float duration,
                float fadeOut,
                float sizeIn,
                float sizeOut,
                Color colorIn,
                Color colorOut,
                float opacity,
                int blendOut,
                float textLength,
                float textScroll,
                float textOffset,
                float distance,
                float drift,
                boolean fadeOnFadeOut,
                boolean angleAdjustment,
                float dispersion,
                float velocityIn,
                float velocityOut,
                float randomVelocity,
                float angle,
                float rotationIn,
                float rotationOut,
                boolean randomRotation,
                CombatEngineLayers layer,
                float frameOffsetMult
        ) {
            this.sprite = sprite;
            this.minLength = minLength;
            this.fadeIn = fadeIn;
            this.duration = duration;
            this.fadeOut = fadeOut;
            this.sizeIn = sizeIn;
            this.sizeOut = sizeOut;
            this.colorIn = colorIn;
            this.colorOut = colorOut;
            this.opacity = opacity;
            this.blendOut = blendOut;
            this.textLength = textLength;
            this.textScroll = textScroll;
            this.textOffset = textOffset;
            this.distance = distance;
            this.drift = drift;
            this.fadeOnFadeOut = fadeOnFadeOut;
            this.angleAdjustment = angleAdjustment;
            this.dispersion = dispersion;
            this.velocityIn = velocityIn;
            this.velocityOut = velocityOut;
            this.randomVelocity = randomVelocity;
            this.angle = angle;
            this.rotationIn = rotationIn;
            this.rotationOut = rotationOut;
            this.randomRotation = randomRotation;
            this.layer = layer;
            this.frameOffsetMult = frameOffsetMult;
        }
    }
}
