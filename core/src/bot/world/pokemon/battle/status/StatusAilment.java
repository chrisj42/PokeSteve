package bot.world.pokemon.battle.status;

public enum StatusAilment {
	Paralysis("was paralyzed!", "is paralyzed! It can't move!", "is no longer paralyzed."),
	Sleep("fell asleep!", "is fast asleep.", "woke up!"),
	Freeze("has been frozen!", "is frozen solid!", "thawed out."),
	Burn("was burned!", "is hurt by its burn!", "is no longer on fire :D"),
	Poison("was poisoned!", "is hurt by the poison!", "is no longer poisoned."),
	Confusion("became confused!", "is confused!", "snapped out of its confusion."),
	// most effects past here will be relocated / turned into flags
	Infatuation, Trap, Nightmare, Torment, Disable, Yawn,
	HealBlock, NoTypeImmunity, LeechSeed, Embargo, PerishSong,
	Ingrain, Silence;
	
	public static final StatusAilment[] values = StatusAilment.values();
	
	public final String statusAttain, statusAffect, statusRemove;
	
	// this constructor will be phased out
	StatusAilment() { this(null, null, null); }
	
	StatusAilment(String statusAttain, String statusAffect, String statusRemove) {
		this.statusAttain = statusAttain;
		this.statusAffect = statusAffect;
		this.statusRemove = statusRemove;
	}
}
