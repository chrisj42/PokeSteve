package bot.pokemon.move;

import java.util.function.Function;

import bot.pokemon.Stat;
import bot.pokemon.Stat.StageEquation;
import bot.pokemon.Type;
import bot.pokemon.battle.BattlePokemon;
import bot.pokemon.battle.Flag;
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
	public final ChargeState chargeState;
	private final boolean doesRecharge;
	private final int accuracy;
	private final AccuracyProperty accuracyProp;
	public final DamageProperty damageEffect;
	private final Function<MoveContext, Boolean> moveCondition;
	public final EffectGroup primary;
	public final EffectGroup secondary;
	public final int secondaryChance;
	
	public Move(String name, int id, MoveDescription description, Type type, int pp, int priority, ChargeState chargeState, boolean doesRecharge, int accuracy, AccuracyProperty accuracyProp, DamageProperty damageEffect, Function<MoveContext, Boolean> moveCondition, EffectGroup primary, EffectGroup secondary, int secondaryChance) {
		this.name = name;
		this.id = id;
		this.description = description;
		this.type = type;
		this.pp = pp;
		this.priority = priority;
		this.chargeState = chargeState;
		this.doesRecharge = doesRecharge;
		this.accuracy = accuracy;
		this.accuracyProp = accuracyProp;
		this.damageEffect = damageEffect;
		this.moveCondition = moveCondition;
		this.primary = primary == null ? EffectGroup.NO_EFFECTS : primary;
		this.secondary = secondary == null ? EffectGroup.NO_EFFECTS : secondary;
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
		context.withUser("used ").append(name).append('!');
		
		if(moveCondition != null && !moveCondition.apply(context)) {
			context.line("But it failed!");
			return;
		}
		
		if(chargeState != null) {
			// it's taken as a given that this is the move that's charging
			if(context.user.hasFlag(Flag.CHARGING_MOVE))
				context.user.clearFlag(Flag.CHARGING_MOVE);
			else {
				context.user.setFlag(Flag.CHARGING_MOVE, context.userMoveIdx);
				context.withUser(chargeState.prepMessage);
				return;
			}
		}
		
		if(context.enemy.hasFlag(Flag.CHARGING_MOVE)) {
			ChargeState state = context.enemyPokemon.moveset[context.enemy.getFlag(Flag.CHARGING_MOVE)].chargeState;
			if(!state.affectedBy(this)) {
				context.line("But ").append(context.enemyPlayer).append(" wasn't there!");
				return;
			}
		}
		
		// calc if hit
		int accuracy = getAccuracy(context);
		if(accuracyProp == null || !accuracyProp.ignoresStages()) {
			int accuracyStage = Utils.clamp(context.user.getStage(Stat.Accuracy) - context.enemy.getStage(Stat.Evasion), BattlePokemon.MIN_STAGE, BattlePokemon.MAX_STAGE);
			accuracy = StageEquation.Accuracy.modifyStat(accuracy, accuracyStage);
		}
		// System.out.println("accuracy for move "+this+": "+accuracy);
		boolean hit = Utils.chance(accuracy);
		if(!hit)
			context.line("It missed!");
		else {
			if(doesRecharge)
				// user is going to have to recharge next turn
				context.user.setFlag(Flag.RECHARGING);
			
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
		private ChargeState doesCharge;
		private boolean doesRecharge;
		private AccuracyProperty accuracyProp;
		private DamageProperty damageEffect;
		private EffectGroup primary;
		private EffectGroup secondary;
		private int secondaryChance;
		private Function<MoveContext, Boolean> moveCondition;
		
		// MoveBuilder(Type type, int pp) {  this(null, type, pp); }
		// MoveBuilder(Type type, int pp, int accuracy) {  this(null, type, pp, accuracy); }
		// MoveBuilder(@Nullable String name, Type type, int pp) { this(name, type, pp, 0); }
		// MoveBuilder(@Nullable String name, Type type, int pp, int accuracy) {
		MoveBuilder() {  this(null); }
		// MoveBuilder(int accuracy) {  this(null, accuracy); }
		// MoveBuilder(@Nullable String name) { this(name, 0); }
		MoveBuilder(@Nullable String name) {
			this.name = name;
			// doesCharge = false;
			doesRecharge = false;
			secondaryChance = -1;
		}
		
		Move create(Moves moveEnum) {
			String name = this.name == null ? moveEnum.name().replaceAll("_", " ").trim() : this.name;
			return new Move(name, moveEnum.ordinal()+1, moveEnum.description, moveEnum.type, moveEnum.pp, moveEnum.priority, doesCharge, doesRecharge, moveEnum.accuracy, accuracyProp, damageEffect == null ? moveEnum.classicDamage : 	damageEffect, moveCondition, primary, secondary, secondaryChance < 0 ? moveEnum.secondaryChance : secondaryChance);
		}
		
		MoveBuilder acc(AccuracyProperty prop) {
			this.accuracyProp = prop;
			return this;
		}
		
		MoveBuilder damage(DamageProperty damageEffect) {
			this.damageEffect = damageEffect;
			return this;
		}
		
		MoveBuilder condition(Function<MoveContext, Boolean> condition) {
			this.moveCondition = condition;
			return this;
		}
		
		MoveBuilder charge(ChargeState chargeState) {
			doesCharge = chargeState;
			return this;
		}
		
		MoveBuilder recharge() {
			doesRecharge = true;
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
