package bot.pokemon.move;

import bot.pokemon.battle.MoveContext;
import bot.pokemon.move.PokemonEffectSet.PokemonEffect;

public class StatusProperty implements PokemonEffect {
	
	public static final StatusProperty NO_EFFECT = new StatusProperty();
	
	@Override
	public EffectResult doEffect(MoveContext context, boolean onEnemy) {
		return EffectResult.NA;
	}
	
}
