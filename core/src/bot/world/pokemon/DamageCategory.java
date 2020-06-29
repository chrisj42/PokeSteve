package bot.world.pokemon;

public enum DamageCategory {
	// null damage type means the move type is "status"
	Physical(Stat.Attack, Stat.Defense),
	Special(Stat.SpAttack, Stat.SpDefense);
	
	public static final DamageCategory[] values = DamageCategory.values();
	
	private final Stat attackStat;
	private final Stat defenseStat;
	
	DamageCategory(Stat attackStat, Stat defenseStat) {
		this.attackStat = attackStat;
		this.defenseStat = defenseStat;
	}
	
	public Stat getAttackStat() { return attackStat; }
	public Stat getDefenseStat() { return defenseStat; }
	
}
