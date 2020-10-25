package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.launcher.ModManager;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lwjgl.util.vector.Vector2f;

public class vic_psotnikOnHit implements OnHitEffectPlugin {


	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
		if (!shieldHit && target instanceof ShipAPI) {
			if (Math.random() > 0.75f) {
				engine.spawnEmpArc(projectile.getSource(),
						point,
						target,
						target,
						DamageType.FRAGMENTATION,
						0,
						80,
						3000,
						"tachyon_lance_emp_impact",
						4,
						Color.WHITE,
						Color.CYAN);
			}
		}
	}
}
