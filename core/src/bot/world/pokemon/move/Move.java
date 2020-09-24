package bot.world.pokemon.move;

import java.util.function.Consumer;
import java.util.function.Function;

import bot.world.pokemon.DamageCategory;
import bot.world.pokemon.Stat;
import bot.world.pokemon.Stat.StageEquation;
import bot.world.pokemon.Type;
import bot.world.pokemon.battle.BattlePokemon;
import bot.world.pokemon.battle.Flag;
import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.move.DamageCalculator.PercentageDamage;
import bot.world.pokemon.move.DamageProperty.DamageBuilder;
import bot.world.pokemon.move.EffectGroup.EffectGroupBuilder;
import bot.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Move implements Comparable<Move> {
	
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
	private final Consumer<MoveContext> onMoveMiss;
	public final EffectGroup primary;
	public final EffectGroup secondary;
	public final int secondaryChance;
	
	public Move(String name, int id, MoveDescription description, Type type, int pp, int priority, ChargeState chargeState, boolean doesRecharge, int accuracy, AccuracyProperty accuracyProp, DamageProperty damageEffect, Function<MoveContext, Boolean> moveCondition, Consumer<MoveContext> onMoveMiss, EffectGroup primary, EffectGroup secondary, int secondaryChance) {
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
		this.onMoveMiss = onMoveMiss;
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
	
	// returns title of embed field
	public String doMove(MoveContext context) {
		if(context.user.hasFlag(Flag.FLINCH)) {
			return context.userPokemon.getName() + " flinched!";
		}
		
		context.user.subtractPp(context.userMoveIdx);
		final String titleText = context.userPokemon.getName() + " used " + name+'!';
		
		if(moveCondition != null && !moveCondition.apply(context)) {
			context.line("But it failed!");
			return titleText;
		}
		
		if(chargeState != null) {
			// it's taken as a given that this is the move that's charging
			if(context.user.hasFlag(Flag.FORCED_MOVE))
				context.user.clearFlag(Flag.FORCED_MOVE);
			else {
				context.user.setFlag(Flag.FORCED_MOVE, context.userMoveIdx);
				context.withUser(chargeState.prepMessage);
				return titleText;
			}
		}
		
		if(context.enemy.hasFlag(Flag.FORCED_MOVE)) {
			ChargeState state = context.enemyPokemon.getMove(context.enemy.getFlag(Flag.FORCED_MOVE)).chargeState;
			if(!state.affectedBy(this)) {
				context.line("But ").append(context.enemyPlayer).append(" wasn't there!");
				return titleText;
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
		if(!hit) {
			context.line("It missed!");
			if(onMoveMiss != null)
				onMoveMiss.accept(context);
		}
		else {
			if(doesRecharge) {
				// user is going to have to recharge next turn
				context.user.setFlag(Flag.FORCED_MOVE, -1);
				context.user.setFlag(Flag.REST_MESSAGE, "You are recharging from a previous move."); // stores
			}
			
			EffectResult result = context.userMove.damageEffect.doDamage(context);
			if(result == EffectResult.NO_OUTPUT) {
				context.line("It had no effect...");
				return titleText;
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
			
			if(result == EffectResult.NO_OUTPUT)
				context.line("But it failed!");
			else if(result == EffectResult.NA)
				context.line("But nothing happened...");
		}
		
		return titleText;
	}
	
	@Override
	public int compareTo(@NotNull Move o) {
		return Integer.compare(id, o.id);
	}
}
