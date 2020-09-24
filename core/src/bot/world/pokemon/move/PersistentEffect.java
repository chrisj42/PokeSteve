package bot.world.pokemon.move;

import bot.world.pokemon.battle.PlayerContext;

public interface PersistentEffect {
	
	// these effects are applied to pokemon and affect them across multiple turns
	
	// apply the effect on a subsequent turn.
	// returns true to continue effect, false to end the effect.
	boolean apply(PlayerContext context);
	
	default void onEffectEnd(PlayerContext context) {}
	
	abstract class TimedPersistentEffect implements PersistentEffect {
		
		private int turnDuration; // number of turns where this effect persists
		
		public TimedPersistentEffect(int turnDuration) {
			this.turnDuration = turnDuration;
		}
		
		@Override
		public boolean apply(PlayerContext context) {
			turnDuration--;
			return turnDuration > 0;
		}
		
		@Override
		public abstract void onEffectEnd(PlayerContext context);
	}
}
