package bot.pokemon;

public enum Stat {
	
	// these are the stats that are persistent for pokemon instances.
	
	Health,
	Attack,
	Defense,
	SpAttack,
	SpDefense,
	Speed;
	
	public static final Stat[] values = Stat.values();
}
