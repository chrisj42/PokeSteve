package bot.pokemon;

import bot.pokemon.Stat.StatEquation;
import bot.util.Utils;

public class StatData {
	
	public static final int MAX_EV = 252;
	
	private final Pokemon pokemon;
	private final Stat stat;
	
	private final int base;
	private final int iv;
	private int ev;
	private int value; // cache
	private final StatEquation statEquation;
	
	StatData(Pokemon pokemon, Stat stat) {
		this.pokemon = pokemon;
		this.stat = stat;
		if(stat == Stat.Health) statEquation = StatEquation.HP;
		else statEquation = StatEquation.Main;
		
		this.base = pokemon.species.getBaseStat(stat);
		this.iv = Utils.randInt(0, 31);
		this.ev = 0;
		recalcStat();
	}
	
	public Stat getStatType() { return stat; }
	
	public void recalcStat() {
		value = statEquation.calcStat(base, ev, iv, pokemon.getLevel(), pokemon.nature.getNatureMod(stat));
	}
	
	public void addEV(int amount) {
		ev = Math.min(MAX_EV, ev + amount);
		recalcStat();
	}
	
	public int getStatValue() { return value; }
}
