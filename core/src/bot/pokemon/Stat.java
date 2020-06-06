package bot.pokemon;

import java.util.function.Function;

public enum Stat {
	
	// all stats
	
	Health,
	Attack,
	Defense,
	SpAttack("Special Attack"),
	SpDefense("Special Defense"),
	Speed,
	Accuracy,
	Evasion;
	
	public static final Stat[] persistStats = new Stat[] {
		Health, Attack, Defense, SpAttack, SpDefense, Speed
	};
	public static final Stat[] stageStats = new Stat[] {
		Attack, Defense, SpAttack, SpDefense, Speed, Accuracy, Evasion
	};
	public static final Stat[] values = Stat.values();
	
	private String altName;
	
	Stat() { this(null); }
	Stat(String altName) {
		this.altName = altName;
	}
	
	@Override
	public String toString() {
		return altName == null ? super.toString() : altName;
	}
	
	
	public enum StatEquation {
		
		Main() {
			@Override
			public int calcStat(int base, int ev, int iv, int level, int natureMod) {
				return Nature.applyNatureMod(
					(2 * base + iv + ev) * level / 100 + 5, natureMod);
			}
		},
		
		HP() {
			@Override
			public int calcStat(int base, int ev, int iv, int level, int natureMod) {
				return (2 * base + iv + ev) * level / 100 + level + 10;
			}
		};
		
		StatEquation() {}
		
		public abstract int calcStat(int base, int ev, int iv, int level, int natureMod);
	}
	
	public enum StageEquation {
		
		Main(s -> Math.max(2f, 2 + s) / Math.max(2, 2 - s)),
		
		Accuracy(s -> Math.max(3f, 3 + s) / Math.max(3, 3 - s));
		
		private final Function<Integer, Float> statModifer;
		
		StageEquation(Function<Integer, Float> statModifer) {
			this.statModifer = statModifer;
		}
		
		// statVal is the current stat for a pokemon, as opposed to species base stats
		public int modifyStat(int statVal, int stage) {
			return Math.round(statVal * statModifer.apply(stage));
		}
	}
}
