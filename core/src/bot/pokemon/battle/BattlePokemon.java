package bot.pokemon.battle;

import java.util.EnumMap;

import bot.pokemon.Move;
import bot.pokemon.Pokemon;
import bot.pokemon.Stat;
import bot.util.Utils;

public class BattlePokemon {
	
	// this class is used when battling; holds things like stat stage changes and other temporary effects.
	
	public static final int MAX_STAGE = 6;
	public static final int MIN_STAGE = -6;
	
	public final Pokemon pokemon;
	private final EnumMap<Stat, Integer> statStages;
	
	public int health;
	private final int[] movePp;
	
	public BattlePokemon(Pokemon pokemon) {
		this.pokemon = pokemon;
		health = pokemon.getStat(Stat.Health);
		
		statStages = new EnumMap<>(Stat.class);
		for(Stat stat: Stat.stageStats)
			statStages.put(stat, 0);
		
		movePp = new int[pokemon.moveset.length];
		for(int i = 0; i < movePp.length; i++)
			movePp[i] = pokemon.moveset[i].pp;
	}
	
	public int getStage(Stat stat) {
		return statStages.get(stat);
	}
	
	// returns whether there was a change
	public boolean alterStatStage(Stat stat, int amount) {
		final int cur = statStages.get(stat);
		final int next = Utils.clamp(cur + amount, MIN_STAGE, MAX_STAGE);
		statStages.put(stat, next);
		return cur != next;
	}
	
	public int getPp(int move) {
		return movePp[move];
	}
	
	public void subtractPp(int move) {
		movePp[move]--;
	}
}
