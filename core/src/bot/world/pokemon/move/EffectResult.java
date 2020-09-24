package bot.world.pokemon.move;

public enum EffectResult {
	// this is used for output purposes; does more need to be said?
	
	NA, // nothing happened; if we end on this, then no effect occurred or will ever occur on repeated attempts. No notification should be given.
	FAILURE, // stuff may or may not have happened, but no output was given. Most of the time nothing will have happened. Something should be printed to notify the users.
	RECORDED; // output was given signifying what happened, or the lack of occurrence
	
	public EffectResult combine(EffectResult other) {
		if(this == RECORDED || other == RECORDED)
			return RECORDED;
		if(this == FAILURE || other == FAILURE)
			return FAILURE;
		return NA;
	}
}
