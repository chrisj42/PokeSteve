package bot.pokemon.move;

public enum EffectResult {
	// because of the +2 at the end, a move that isn't completely cancelled should always do some damage, so a "NO_DAMAGE" constant is unnecassary.
	
	NA, // this move doesn't / didn't even attempt to cause an effect
	FAILURE, // target is immune or the effect failed to occur
	AFFECTED; // effect occurred i.e. damage was taken
	
	public EffectResult combine(EffectResult other) {
		if(this == AFFECTED || other == AFFECTED)
			return AFFECTED;
		if(this == FAILURE || other == FAILURE)
			return FAILURE;
		return NA;
	}
}
