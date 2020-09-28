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
	private int evStore; // stored until level up
	private int value; // cache
	private final StatEquation statEquation;
	
	private StatData(Pokemon pokemon, Stat stat, int iv, int ev, int evStore) {
		this.pokemon = pokemon;
		this.stat = stat;
		if(stat == Stat.Health) statEquation = StatEquation.HP;
		else statEquation = StatEquation.Main;
		
		this.base = pokemon.species.getBaseStat(stat);
		this.iv = iv;
		this.ev = ev;
		this.evStore = evStore;
		recalcStat();
	}
	StatData(Pokemon pokemon, Stat stat) {
		this(pokemon, stat, Utils.randInt(0, 31), 0, 0);
	}
	StatData(Pokemon pokemon, Stat stat, StatData model) {
		this(pokemon, stat, model.iv, model.ev, model.evStore);
	}
	StatData(Pokemon pokemon, Stat stat, SerialStatData data) {
		this(pokemon, stat, data.iv, data.ev, data.evStore);
	}
	
	public Stat getStatType() { return stat; }
	
	public void recalcStat() {
		value = statEquation.calcStat(base, ev, iv, pokemon.getLevel(), pokemon.nature.getNatureMod(stat));
	}
	
	public void addEV(int amount) {
		evStore += amount;
	}
	
	void onLevelUp() {
		ev = Math.min(MAX_EV, ev + evStore);
		evStore = 0;
		recalcStat();
	}
	
	int getIV() { return iv; }
	int getEV() { return ev; }
	int getEVStore() { return evStore; }
	
	public int getStatValue() { return value; }
	
	@POJO
	public static class SerialStatData {
		public int ev;
		public int iv;
		public int evStore;
		
		public SerialStatData() {}
		public SerialStatData(StatData data) {
			iv = data.iv;
			ev = data.ev;
			evStore = data.evStore;
		}
	}
}
