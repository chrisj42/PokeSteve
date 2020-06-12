package bot.pokemon.move;

import bot.pokemon.DamageRelation;
import bot.pokemon.DamageCategory;
import bot.pokemon.Type;
import bot.pokemon.battle.MoveContext;
import bot.pokemon.move.MultiHitProperty.MultiHitBuilder;
import bot.util.Utils;

public class DamageProperty {
	
	public static final DamageProperty NO_DAMAGE = new DamageBuilder(null, null).create();
	
	private final DamageCategory damageType;
	private final DamageCalculator damageBehavior;
	private final MultiHitProperty multiHitProperty;
	private final int drainPercent;
	private final boolean ignoreTypeBonuses;
	
	private DamageProperty(DamageCategory damageType, DamageCalculator damageBehavior, MultiHitProperty multiHitProperty, int drainPercent, boolean ignoreTypeBonuses) {
		this.damageType = damageType;
		this.damageBehavior = damageBehavior;
		this.multiHitProperty = multiHitProperty;
		this.drainPercent = drainPercent;
		this.ignoreTypeBonuses = ignoreTypeBonuses;
	}
	
	public EffectResult doDamage(MoveContext context) {
		if(damageType == null)
			return EffectResult.NA;
		/*if(!context.hadEffect()) {
			context.setHadEffect();
			context.line("But it failed!");
			return;
		}*/
		
		final Move move = context.userMove;
		final DamageRelation relation1 = Type.getDamageRelation(move.type, context.enemySpecies.primaryType);
		final DamageRelation relation2 = Type.getDamageRelation(move.type, context.enemySpecies.secondaryType);
		if(relation1 == DamageRelation.NoEffect || relation2 == DamageRelation.NoEffect) {
			/*if(!context.hadEffect()) {
				context.setHadEffect();
				context.withUser("is unaffected...");
			}*/
			return EffectResult.NO_OUTPUT;
		}
		
		final boolean singleHit = multiHitProperty == null;
		final int hits = singleHit ? 1 : multiHitProperty.getHitCount();
		int damage = 0;
		for(int i = 0; i < hits; i++) {
			if(hits > 1)
				context.line("hitting ").append(i+1).append(" time").append(i+1==1?"":"s").append("...");
			damage += damageBehavior.getDamage(context, damageType);
		}
		
		if(!ignoreTypeBonuses) {
			// type effectiveness
			damage = DamageRelation.multiplyDamage(damage, relation1, relation2);
			// message
			int effectiveness = 0;
			effectiveness += relation1.ordinal() - 2;
			effectiveness += relation2.ordinal() - 2;
			String message = getEffectivenessMessage(effectiveness);
			if(message != null)
				context.line(message);
			
			// same type attack bonus
			if(move.type != null && (move.type == context.userSpecies.primaryType
				|| move.type == context.userSpecies.secondaryType))
				damage = damage * 3 / 2;
		}
		
		context.enemy.alterHealth(-damage); // intentionally not using the returned value
		context.withEnemy("took ").append(damage).append(" damage!");
		// context.setHadEffect();
		// if(!singleHit)
		// 	context.line("Hit ").append(hits).append(hits == 1 ? " time!":" times!");
		
		if(drainPercent > 0) {
			int gain = context.user.alterHealth(damage * drainPercent / 100);
			context.withUser("regained ").append(gain).append(" health!");
		}
		
		return EffectResult.RECORDED;
	}
	
	private static String getEffectivenessMessage(int effectiveness) {
		effectiveness = Utils.clamp(effectiveness, -2, 2);
		switch(effectiveness) {
			case -2: return "It barely had any effect...";
			case -1: return "It's not very effective...";
			case 1: return "It's super effective!";
			case 2: return "It's extremely effective!";
			default: return null;
		}
	}
	
	public static class DamageBuilder {
		
		private DamageCategory damageType;
		private DamageCalculator damageBehavior;
		private MultiHitProperty multiHitProperty;
		private int drainPercent;
		private boolean ignoreTypeBonuses;
		
		DamageBuilder(DamageCategory damageType, DamageCalculator damageBehavior) {
			this.damageType = damageType;
			this.damageBehavior = damageBehavior;
			ignoreTypeBonuses = false;
		}
		
		MultiHitBuilder multiHit() { return new MultiHitBuilder(this); }
		DamageBuilder multiHit(MultiHitProperty hitProp) {
			this.multiHitProperty = hitProp;
			return this;
		}
		
		DamageBuilder drain(int percentOfDealt) {
			drainPercent = percentOfDealt;
			return this;
		}
		
		DamageBuilder ignoreTypeBonuses() {
			ignoreTypeBonuses = true;
			return this;
		}
		
		DamageProperty create() {
			return new DamageProperty(damageType, damageBehavior, multiHitProperty, drainPercent, ignoreTypeBonuses);
		}
	}
}
