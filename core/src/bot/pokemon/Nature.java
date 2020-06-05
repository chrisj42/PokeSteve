package bot.pokemon;

public enum Nature {
	
	Hardy(),
	Bold(Stat.Defense, Stat.Attack),
	Modest(Stat.SpAttack, Stat.Attack),
	Calm(Stat.SpDefense, Stat.Attack),
	Timid(Stat.Speed, Stat.Attack),
	Lonely(Stat.Attack, Stat.Defense),
	Docile(),
	Mild(Stat.SpAttack, Stat.Defense),
	Gentle(Stat.SpDefense, Stat.Defense),
	Hasty(Stat.Speed, Stat.Defense),
	Adamant(Stat.Attack, Stat.SpAttack),
	Impish(Stat.Defense, Stat.SpAttack),
	Bashful(),
	Careful(Stat.SpDefense, Stat.SpAttack),
	Rash(Stat.SpAttack, Stat.SpDefense),
	Jolly(Stat.Speed, Stat.SpAttack),
	Naughty(Stat.Attack, Stat.SpDefense),
	Lax(Stat.Defense, Stat.SpDefense),
	Quirky(),
	Naive(Stat.Speed, Stat.SpDefense),
	Brave(Stat.Attack, Stat.Speed),
	Relaxed(Stat.Defense, Stat.Speed),
	Quiet(Stat.SpAttack, Stat.Speed),
	Sassy(Stat.SpDefense, Stat.Speed),
	Serious();
	
	public static final Nature[] values = Nature.values();
	
	private final Stat incStat, decStat;
	
	Nature() { this(null, null); }
	Nature(Stat incStat, Stat decStat) {
		this.incStat = incStat;
		this.decStat = decStat;
	}
	
	public int alterStat(Stat stat, int value) {
		if(stat == incStat)
			return value * 11 / 10;
		if(stat == decStat)
			return value * 9 / 10;
		return value;
	}
}
