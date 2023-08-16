package data.scripts.weapons.EveryFrameEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;

import java.util.List;

public class vic_inFLightSound implements OnFireEffectPlugin {

    @Override
    public void onFire(final DamagingProjectileAPI projectile, WeaponAPI weapon, final CombatEngineAPI engine) {
        engine.addPlugin(new EveryFrameCombatPlugin() {
            @Override
            public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

            }

            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                Global.getSoundPlayer().playLoop("soindId", projectile, 1,1,projectile.getLocation(),projectile.getVelocity(),0.2f,0.2f);
                if (!engine.isEntityInPlay(projectile)) engine.removePlugin(this);
            }

            @Override
            public void renderInWorldCoords(ViewportAPI viewport) {

            }

            @Override
            public void renderInUICoords(ViewportAPI viewport) {

            }

            @Override
            public void init(CombatEngineAPI engine) {

            }
        });
    }
}
