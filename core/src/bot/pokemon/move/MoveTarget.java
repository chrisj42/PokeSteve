package bot.pokemon.move;

import java.util.HashMap;

public enum MoveTarget {
	
	// note that this does not cover special cases like earthquake not affecting flying pokemon or those with levitate; earthquake would have the All target.
	// note that a move with the target Ally fails with one-on-one battles
	// note that moves which don't target a specific pokemon or set of pokemon don't use this.
	
	// Self, Ally, AllySelect, Allies,
	// Enemy, Enemies, EnemySelect, EnemyRandom,
	// AllOther, All;
	Self, Enemy, All; // we won't support double-battles yet
	
	public static MoveTarget getTarget(String name) {
		switch(name) {
			case "selected-pokemon-me-first":
			case "random-opponent":
			case "all-other-pokemon":
			case "selected-pokemon":
			case "all-opponents":
				return Enemy;
			case "user-or-ally":
			case "user":
			case "user-and-allies":
				return Self;
			case "all-pokemon":
				return All;
			default:
				return null;
		}
	}
}
