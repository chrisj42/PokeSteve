package bot.pokemon;

import bot.util.Utils;

public class StatData {
	
	public static final int MAX_EV = 252;
	
	private final Pokemon pokemon;
	private final Stat stat;
	
	private final int base;
	private final int iv;
	private int ev;
	private int value; // cache
	
	StatData(Pokemon pokemon, Stat stat) {
		this.pokemon = pokemon;
		this.stat = stat;
		this.base = pokemon.species.getBaseStat(stat);
		this.iv = Utils.randInt(0, 31);
		this.ev = 0;
		recalcStat();
	}
	
	public Stat getStatType() { return stat; }
	
	public void recalcStat() {
		value = stat.equation.calcStat(base, ev, iv, pokemon.getLevel(), pokemon.nature.getNatureMod(stat));
	}
	
	public void addEV(int amount) {
		ev = Math.min(MAX_EV, ev + amount);
		recalcStat();
	}
	
	public int getStatValue() { return value; }
}
