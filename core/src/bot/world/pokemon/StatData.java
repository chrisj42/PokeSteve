package bot.world.pokemon;

import bot.world.pokemon.Stat.StatEquation;
import bot.util.POJO;
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
	
	private StatData(Pokemon pokemon, Stat stat, int iv, int ev) {
		this.pokemon = pokemon;
		this.stat = stat;
		if(stat == Stat.Health) statEquation = StatEquation.HP;
		else statEquation = StatEquation.Main;
		
		this.base = pokemon.species.getBaseStat(stat);
		this.iv = Utils.randInt(0, 31);
		this.ev = 0;
		recalcStat();
	}
	StatData(Pokemon pokemon, Stat stat) {
		this(pokemon, stat, Utils.randInt(0, 31), 0);
	}
	StatData(Pokemon pokemon, Stat stat, StatData model) {
		this(pokemon, stat, model.iv, model.ev);
	}
	StatData(Pokemon pokemon, Stat stat, SerialStatData data) {
		this(pokemon, stat, data.iv, data.ev);
	}
	
	public Stat getStatType() { return stat; }
	
	public void recalcStat() {
		value = statEquation.calcStat(base, ev, iv, pokemon.getLevel(), pokemon.nature.getNatureMod(stat));
	}
	
	public void addEV(int amount) {
		ev = Math.min(MAX_EV, ev + amount);
		recalcStat();
	}
	
	int getIV() { return iv; }
	int getEV() { return ev; }
	
	public int getStatValue() { return value; }
	
	@POJO
	public static class SerialStatData {
		public int ev;
		public int iv;
		
		public SerialStatData() {}
		public SerialStatData(StatData data) {
			iv = data.iv;
			ev = data.ev;
		}
	}
}
