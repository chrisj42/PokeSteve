package bot.pokemon;

public enum DamageType {
	// null damage type means the move type is "status"
	Physical(Stat.Attack, Stat.Defense),
	Special(Stat.SpAttack, Stat.SpDefense);
	
	private final Stat attackStat;
	private final Stat defenseStat;
	
	DamageType(Stat attackStat, Stat defenseStat) {
		this.attackStat = attackStat;
		this.defenseStat = defenseStat;
	}
	
	public Stat getAttackStat() { return attackStat; }
	public Stat getDefenseStat() { return defenseStat; }
	
}
