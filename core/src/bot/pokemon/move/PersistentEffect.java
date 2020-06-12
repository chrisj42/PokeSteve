package bot.pokemon.move;

import bot.pokemon.battle.MoveContext;

public abstract class PersistentEffect {
	
	// these effects are applied to pokemon and affect them across multiple turns
	
	private int turnDuration; // number of turns where this effect persists
	
	public PersistentEffect(int turnDuration) {
		this.turnDuration = turnDuration;
	}
	
	// apply the effect on a subsequent turn.
	// returns true to continue effect, false to end the effect.
	public abstract boolean apply(int turn, MoveContext context);
}
