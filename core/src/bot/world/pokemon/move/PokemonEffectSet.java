package bot.world.pokemon.move;

import java.util.List;

import bot.world.pokemon.battle.Flag;
import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.battle.PlayerContext;
import bot.world.pokemon.move.PersistentEffect.TimedPersistentEffect;
import bot.util.Utils;

public class PokemonEffectSet {
	
	public static final PokemonEffectSet NO_EFFECT = new PokemonEffectSet();
	private static final PokemonEffect[] EMPTY = new PokemonEffect[0];
	
	public interface PokemonEffect {
		EffectResult doEffect(MoveContext context, boolean onEnemy);
		
		PokemonEffect FLINCH = (context, onEnemy) -> {
			if(context.isFirst) context.enemy.setFlag(Flag.FLINCH);
			return EffectResult.NO_OUTPUT;
		};
		
		PokemonEffect THRASH = (context, onEnemy) -> {
			if(context.user.hasFlag(Flag.FORCED_MOVE))
				context.withUser("is thrashing about!");
			else {
				context.withUser("began thrashing about!");
				context.user.setFlag(Flag.FORCED_MOVE, context.userMoveIdx);
				context.user.addEffect(new TimedPersistentEffect(Utils.randInt(2, 3)) {
					@Override
					protected void onEffectEnd(PlayerContext context) {
						context.withUser("stopped thrashing about.");
						context.user.clearFlag(Flag.FORCED_MOVE);
						// TODO cause confusion
					}
				});
			}
			return EffectResult.RECORDED;
		};
		
		PokemonEffect DISABLE = (context, onEnemy) -> {
			if(context.enemy.hasFlag(Flag.DISABLED_MOVE))
				return EffectResult.NO_OUTPUT; // "but it failed"
			int lastMove = context.enemyPlayer.getLastMoveIdx();
			if(lastMove < 0)
				return EffectResult.NO_OUTPUT;
			
			Move move = context.enemyPokemon.getMove(lastMove);
			context.line(context.enemyPlayer).append("'s ").append(move).append(" was disabled!");
			context.enemy.setFlag(Flag.DISABLED_MOVE, lastMove);
			context.enemy.addEffect(new TimedPersistentEffect(4) {
				@Override
				protected void onEffectEnd(PlayerContext context) {
					context.line(context.userPlayer).append("'s ").append(move).append(" is no longer disabled!");
					context.user.clearFlag(Flag.DISABLED_MOVE);
				}
			});
			return EffectResult.RECORDED;
		};
	}
	
	private final PokemonEffect[] effects;
	
	PokemonEffectSet(PokemonEffect... effects) {
		this.effects = effects;
	}
	PokemonEffectSet(List<PokemonEffect> effects) {
		this(effects.toArray(EMPTY));
	}
	
	public EffectResult doEffects(MoveContext context, boolean onEnemy) {
		EffectResult result = EffectResult.NA;
		for(PokemonEffect effect: effects)
			result = result.combine(effect.doEffect(context, onEnemy));
		return result;
	}
}
