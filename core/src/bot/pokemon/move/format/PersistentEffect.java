package bot.pokemon.move.format;

public abstract class PersistentEffect extends MoveEffect {
	
	// this is a separate class because these effects need a way to flag that the effect is still present over multiple turns
	
	private MoveEffect effect;
	private PokeUtil.RangeValue turnDuration; // number of turns where this effect persists
	
	public PersistentEffect() {}
	
	// apply the effect on a subsequent turn.
	// returns true to continue effect, false to end the effect.
	public abstract boolean apply(int turn);
	
}
