package bot.pokemon;

import java.util.EnumMap;

import bot.io.json.node.JsonObjectNode;

public class PokemonSpecies {
	
	public String name;
	public int dex;
	private byte catchRate;
	private EggGroup[] eggGroups;
	private int femaleRate; // in eighths
	private PokemonSpecies evolvesFrom;
	private EvolutionChain evoChain;
	private Habitat habitat;
	private GrowthRate growthRate;
	private EnumMap<Stat, Integer> baseStats;
	
	public PokemonSpecies(JsonObjectNode node) {
		
	}
	
}
