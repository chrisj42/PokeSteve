package bot.pokemon.move;

import bot.pokemon.Stat;
import bot.pokemon.Stat.StageEquation;
import bot.pokemon.battle.BattlePokemon;
import bot.pokemon.battle.MoveContext;
import bot.util.Utils;

public interface AccuracyProperty {
	
	int getAccuracy(MoveContext context);
	
	// does the accuracy calculation ignore accuracy and evasion stage modifiers?
	default boolean ignoresStages() {
		// defaults to true because most custom accuracy algos ignore it
		return true;
	}
	
}
