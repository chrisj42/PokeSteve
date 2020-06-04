package bot.pokemon.move;

public enum MoveTarget {
	
	// note that this does not cover special cases like earthquake not affecting flying pokemon or those with levitate; earthquake would have the All target.
	// note that a move with the target Ally fails with one-on-one battles
	// note that moves which don't target a specific pokemon or set of pokemon don't use this.
	
	Self, Ally, AllySelect, Allies,
	Enemy, Enemies, EnemySelect, EnemyRandom,
	AllOther, All;
}
