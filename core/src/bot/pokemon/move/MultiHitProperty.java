package bot.pokemon.move;

import java.util.LinkedList;

import bot.pokemon.move.DamageProperty.DamageBuilder;
import bot.util.Utils;

public class MultiHitProperty {
	
	// public static final MultiHitProperty SINGLE = new MultiHitProperty(1);
	public static final MultiHitProperty SCALED_2_5 = new MultiHitProperty(
		2, 2, 3, 3, 4, 5
	);
	
	private final int[] hitCounts;
	
	MultiHitProperty(int... hitCounts) {
		this.hitCounts = hitCounts;
	}
	
	public int getHitCount() {
		return hitCounts[Utils.randInt(0, hitCounts.length - 1)];
	}
	
	static class MultiHitBuilder {
		
		private DamageBuilder damageBuilder;
		private LinkedList<HitChance> hitChances;
		
		MultiHitBuilder(DamageBuilder damageBuilder) {
			this.damageBuilder = damageBuilder;
			hitChances = new LinkedList<>();
		}
		
		MultiHitBuilder chance(int chance, int... hitCounts) {
			for(int count: hitCounts)
				hitChances.add(new HitChance(count, chance));
			return this;
		}
		
		DamageBuilder add() {
			int chanceCount = 0;
			for(HitChance hitChance: hitChances)
				chanceCount += hitChance.chance;
			
			int[] hitAr = new int[chanceCount];
			int i = 0;
			for(HitChance hitChance: hitChances) {
				for(int j = 0; j < hitChance.chance; j++)
					hitAr[i++] = hitChance.hitCount;
			}
			
			return damageBuilder.multiHit(new MultiHitProperty(hitAr));
		}
		
		private static class HitChance {
			private final int hitCount;
			private final int chance;
			
			private HitChance(int hitCount, int chance) {
				this.hitCount = hitCount;
				this.chance = chance;
			}
		}
	}
}
