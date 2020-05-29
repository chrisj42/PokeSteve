package bot.pokemon.definiton;

public enum DamageType {
	
	Physical(Stat.Attack, Stat.Defense),
	Special(Stat.SpAttack, Stat.SpDefense);
	
	public final Stat attackStat, defenseStat;
	
	DamageType(Stat attackStat, Stat defenseStat) {
		this.attackStat = attackStat;
		this.defenseStat = defenseStat;
	}
}
