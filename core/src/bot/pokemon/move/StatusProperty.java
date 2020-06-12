package bot.pokemon.move;

import bot.pokemon.battle.MoveContext;
import bot.pokemon.battle.status.StatusEffects;
import bot.pokemon.move.PokemonEffectSet.PokemonEffect;

public class StatusProperty implements PokemonEffect {
	
	public static final StatusProperty NO_EFFECT = new StatusProperty(null);
	
	private final StatusEffects status;
	
	public StatusProperty(StatusEffects status) {
		this.status = status;
	}
	
	@Override
	public EffectResult doEffect(MoveContext context, boolean onEnemy) {
		return EffectResult.NA;
	}
	
}
