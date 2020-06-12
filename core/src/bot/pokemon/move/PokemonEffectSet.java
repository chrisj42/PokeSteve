package bot.pokemon.move;

import java.util.List;

import bot.pokemon.battle.MoveContext;

public class PokemonEffectSet {
	
	public static final PokemonEffectSet NO_EFFECT = new PokemonEffectSet();
	private static final PokemonEffect[] EMPTY = new PokemonEffect[0];
	
	public interface PokemonEffect {
		EffectResult doEffect(MoveContext context, boolean onEnemy);
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
