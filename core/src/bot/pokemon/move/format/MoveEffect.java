package bot.pokemon.move.format;

import bot.pokemon.battle.MoveContext;

public abstract class MoveEffect {
	
	// chance of effect occurring given the move hit; 0 means it's guaranteed
	// private int effectChance;
	
	// I think this is only used for very few moves, and for those it can be implemented in other ways.
	// private RangeValue turnDelay; // number of turns until this effect starts occurring
	
	public MoveEffect() {}
	
	public abstract void apply(MoveContext context);
	
	/*protected int getEffectChance() {
		return effectChance;
	}*/
}
