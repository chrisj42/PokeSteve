package bot.pokemon;

import java.util.function.Function;

public enum Stat {
	
	// these are the stats that are persistent for pokemon instances.
	
	Health(StatEquation.HP),
	Attack(StatEquation.Main),
	Defense(StatEquation.Main),
	SpAttack(StatEquation.Main),
	SpDefense(StatEquation.Main),
	Speed(StatEquation.Main);
	
	public final StatEquation equation;
	
	Stat(StatEquation equation) {
		this.equation = equation;
	}
	
	public static final Stat[] values = Stat.values();
	
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
}
