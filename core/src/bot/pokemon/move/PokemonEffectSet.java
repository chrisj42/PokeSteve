package bot.pokemon.move;

import java.util.List;

import bot.pokemon.battle.Flag;
import bot.pokemon.battle.MoveContext;
import bot.pokemon.battle.PlayerContext;
import bot.pokemon.move.PersistentEffect.TimedPersistentEffect;
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
