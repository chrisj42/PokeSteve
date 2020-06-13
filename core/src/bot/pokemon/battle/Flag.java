package bot.pokemon.battle;

import bot.pokemon.move.TrapProperty.TrapEffect;

public interface Flag {
	
	// pokemon battle flags to track various effects
	
	class BoolFlag implements Flag {}
	class ValueFlag<T> implements Flag {}
	
	BoolFlag FLINCH = new BoolFlag();
	
	// used for forced moves and forced lack of moves, if the index is invalid.
	ValueFlag<Integer> FORCED_MOVE = new ValueFlag<>();
	
	ValueFlag<TrapEffect> TRAP = new ValueFlag<>();
	
}
