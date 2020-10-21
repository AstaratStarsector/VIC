package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class vic_rubiconOnHit implements OnHitEffectPlugin {

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, CombatEngineAPI engine) {

		for (int i = 0; i < 5; i++) {


		float todaysRandom = MathUtils.getRandomNumberInRange(-45, 45);
		float todaysRandom2 = MathUtils.getRandomNumberInRange(0.5f, 1.5f);
		Vector2f Dir = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing() + todaysRandom);
		Vector2f SpawnPoint = new Vector2f(point.x + Dir.x * -300 * todaysRandom2, point.y + Dir.y * -300 * todaysRandom2);

		engine.spawnProjectile(projectile.getSource(),
				projectile.getWeapon(),
				"arbalest",
				SpawnPoint,
				projectile.getFacing() + (todaysRandom * MathUtils.getRandomNumberInRange(0.5f, 1.5f)),
				new Vector2f());

			MagicRender.battlespace(
					Global.getSettings().getSprite("fx","vic_rubicon_river"),
					SpawnPoint,
					new Vector2f(),
					new Vector2f(60,60),
					new Vector2f(30,30),
					//angle,
					projectile.getFacing() + (todaysRandom * MathUtils.getRandomNumberInRange(0.5f, 1.5f)),
					0,
					new Color(255,255,255),
					true,
					0.2f,
					0.0f,
					0.2f
			);

		}




	}
}
