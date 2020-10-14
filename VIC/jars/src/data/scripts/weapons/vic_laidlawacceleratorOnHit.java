package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.lwjgl.util.vector.Vector2f;

public class vic_laidlawacceleratorOnHit implements OnHitEffectPlugin {

	private DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.05f,
			25,
			12.5f,
			150,
			75,
			CollisionClass.PROJECTILE_FF,
			CollisionClass.PROJECTILE_FIGHTER,
			3,
			3,
			0.5f,
			10,
			new Color(255, 214, 33, 255),
			new Color(255, 150, 35, 255)
	);

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
		if (!shieldHit && target instanceof ShipAPI) {
			explosion.setDamageType(DamageType.FRAGMENTATION);
			explosion.setShowGraphic(false);
			engine.spawnDamagingExplosion(explosion,projectile.getSource(),point);
		}
	}
}
