package bot.pokemon.move;

import bot.pokemon.Stat;
import bot.pokemon.Stat.StageEquation;
import bot.pokemon.Type;
import bot.pokemon.battle.BattlePokemon;
import bot.pokemon.battle.MoveContext;
import bot.pokemon.move.EffectGroup.EffectGroupBuilder;
import bot.util.Utils;

import org.jetbrains.annotations.Nullable;

public class Move {
	
	public final String name;
	public final int id;
	public final MoveDescription description;
	public final Type type;
	public final int pp;
	public final int priority;
	private final int accuracy;
	private final AccuracyProperty accuracyProp;
	public final DamageProperty damageEffect;
	public final EffectGroup primary;
	public final EffectGroup secondary;
	public final int secondaryChance;
	
	public Move(String name, int id, MoveDescription description, Type type, int pp, int priority, int accuracy, AccuracyProperty accuracyProp, DamageProperty damageEffect, EffectGroup primary, EffectGroup secondary, int secondaryChance) {
		this.name = name;
		this.id = id;
		this.description = description;
		this.type = type;
		this.pp = pp;
		this.priority = priority;
		this.accuracy = accuracy;
		this.accuracyProp = accuracyProp;
		this.damageEffect = damageEffect;
		this.primary = primary;
		this.secondary = secondary;
		this.secondaryChance = secondaryChance;
	}
	
	@Override
	public String toString() { return name; }
	public String getName() { return name; }
	public Type getType() { return type; }
	
	public int getAccuracy(MoveContext context) {
		if(accuracyProp == null)
			return accuracy;
		return accuracyProp.getAccuracy(context);
	}
	
	public void doMove(MoveContext context) {
		context.user.subtractPp(context.userMoveIdx);
		context.withUser(" used ").append(name).append('!');
		
		// calc if hit
		int accuracy = getAccuracy(context);
		if(accuracyProp == null || !accuracyProp.ignoresStages()) {
			int accuracyStage = Utils.clamp(context.user.getStage(Stat.Accuracy) - context.enemy.getStage(Stat.Evasion), BattlePokemon.MIN_STAGE, BattlePokemon.MAX_STAGE);
			accuracy = StageEquation.Accuracy.modifyStat(accuracy, accuracyStage);
		}
		boolean hit = Utils.randInt(0, 99) < accuracy;
		if(!hit)
			context.line("It missed!");
		else {
			EffectResult result = context.userMove.damageEffect.doDamage(context);
			if(result == EffectResult.FAILURE) {
				context.line("It had no effect...");
				return;
			}
			
			final boolean doSecondary = secondaryChance == 0 || Utils.randInt(0, 99) < secondaryChance;
			if(primary.enemy != null)
				result = result.combine(primary.enemy.doEffects(context, true));
			if(doSecondary && secondary.enemy != null)
				result = result.combine(secondary.enemy.doEffects(context, true));
			if(primary.self != null)
				result = result.combine(primary.self.doEffects(context, false));
			if(doSecondary && secondary.self != null)
				result = result.combine(secondary.self.doEffects(context, false));
			if(primary.field != null)
				result = result.combine(primary.field.doEffects(context));
			if(doSecondary && secondary.field != null)
				result = result.combine(secondary.field.doEffects(context));
			
			if(result == EffectResult.FAILURE)
				context.line("But it failed!");
			else if(result == EffectResult.NA)
				context.line("But nothing happened...");
		}
	}
	
	static class MoveBuilder {
		
		private final String name;
		// private final Type type;
		// private final int pp;
		private int accuracy;
		private AccuracyProperty accuracyProp;
		private DamageProperty damageEffect;
		private EffectGroup primary;
		private EffectGroup secondary;
		private int secondaryChance;
		
		// MoveBuilder(Type type, int pp) {  this(null, type, pp); }
		// MoveBuilder(Type type, int pp, int accuracy) {  this(null, type, pp, accuracy); }
		// MoveBuilder(@Nullable String name, Type type, int pp) { this(name, type, pp, 0); }
		// MoveBuilder(@Nullable String name, Type type, int pp, int accuracy) {
		MoveBuilder() {  this(null); }
		MoveBuilder(int accuracy) {  this(null, accuracy); }
		MoveBuilder(@Nullable String name) { this(name, 0); }
		MoveBuilder(@Nullable String name, int accuracy) {
			this.name = name;
			// this.type = type;
			// this.pp = pp;
			this.accuracy = accuracy;
			
		}
		
		Move create(Moves moveEnum) {
			String name = this.name == null ? moveEnum.name().replaceAll("_", " ").trim() : this.name;
			return new Move(name, moveEnum.ordinal(), moveEnum.description, moveEnum.type, moveEnum.pp, moveEnum.priority, accuracy, accuracyProp, damageEffect, primary, secondary, secondaryChance);
		}
		
		/*MoveBuilder acc(int accuracy) {
			this.accuracy = accuracy;
			return this;
		}*/
		MoveBuilder acc(AccuracyProperty prop) {
			this.accuracyProp = prop;
			return this;
		}
		
		MoveBuilder damage(DamageProperty damageEffect) {
			this.damageEffect = damageEffect;
			return this;
		}
		
		EffectGroupBuilder primary() {
			return new EffectGroupBuilder(this, true);
		}
		EffectGroupBuilder secondary(int chance) {
			secondaryChance = chance;
			return new EffectGroupBuilder(this, false);
		}
		
		MoveBuilder primary(EffectGroup effects) {
			this.primary = effects;
			return this;
		}
		MoveBuilder secondary(EffectGroup effects) {
			this.secondary = effects;
			return this;
		}
		
		MoveBuilder effectChance(int chance) {
			secondaryChance = chance;
			return this;
		}
	}
}
