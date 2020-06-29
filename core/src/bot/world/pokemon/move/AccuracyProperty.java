package bot.world.pokemon.move;

import bot.world.pokemon.battle.MoveContext;

public interface AccuracyProperty {
	
	int getAccuracy(MoveContext context);
	
	// does the accuracy calculation ignore accuracy and evasion stage modifiers?
	default boolean ignoresStages() {
		// defaults to true because most custom accuracy algos ignore it
		return true;
	}
	
}
