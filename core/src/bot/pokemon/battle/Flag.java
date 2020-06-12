package bot.pokemon.battle;

import bot.pokemon.move.PokemonTrapEffect.TrapEffect;

public interface Flag {
	
	// pokemon battle flags to track various effects
	
	class BoolFlag implements Flag {}
	class ValueFlag<T> implements Flag {}
	
	BoolFlag RECHARGING = new BoolFlag();
	
	ValueFlag<Integer> CHARGING_MOVE = new ValueFlag<>();
	
	ValueFlag<TrapEffect> TRAP = new ValueFlag<>();
}
