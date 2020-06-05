package bot.pokemon;

public enum StageShiftStat {
	
	// these are the stats that can be altered in stages during battle
	
	Attack,
	Defense,
	SpAttack,
	SpDefense,
	Speed,
	Accuracy,
	Evasion;
	
	public static final StageShiftStat[] values = StageShiftStat.values();
}
