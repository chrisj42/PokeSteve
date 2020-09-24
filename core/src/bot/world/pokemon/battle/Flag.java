package bot.world.pokemon.battle;

import bot.world.pokemon.move.TrapEffect.PersistentTrapEffect;

public interface Flag {
	
	// pokemon battle flags to track various effects
	
	class BoolFlag implements Flag {}
	class ValueFlag<T> implements Flag {}
	
	BoolFlag FLINCH = new BoolFlag();
	
	// used for forced moves and forced lack of moves, if the index is invalid.
	ValueFlag<Integer> FORCED_MOVE = new ValueFlag<>();
	
	ValueFlag<Integer> DISABLED_MOVE = new ValueFlag<>();
	
	// denotes that no move will be used the following turn, and the value is the message for the player saying why they can't select a move
	// so far, used for recharging
	ValueFlag<String> REST_MESSAGE = new ValueFlag<>();
	
	ValueFlag<PersistentTrapEffect> TRAP = new ValueFlag<>();
	
}
