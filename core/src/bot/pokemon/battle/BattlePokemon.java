package bot.pokemon.battle;

import java.util.EnumMap;

import bot.pokemon.Move;
import bot.pokemon.Pokemon;
import bot.pokemon.Stat;
import bot.pokemon.battle.ShiftableStat.StatStageManager;

public class BattlePokemon {
	
	// this class is used when battling; holds things like stat stage changes and other temporary effects.
	
	public final Pokemon pokemon;
	private final EnumMap<ShiftableStat, StatStageManager> statStages;
	
	private final int health;
	private final int[] movePp;
	
	public BattlePokemon(Pokemon pokemon) {
		this.pokemon = pokemon;
		health = pokemon.getStat(Stat.Health);
		
		statStages = new EnumMap<>(ShiftableStat.class);
		for(ShiftableStat stat: ShiftableStat.values)
			statStages.put(stat, stat.getStatManager());
		
		movePp = new int[pokemon.moveset.length];
		for(int i = 0; i < movePp.length; i++)
			movePp[i] = pokemon.moveset[i].pp;
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getPp(int move) {
		return movePp[move];
	}
}
