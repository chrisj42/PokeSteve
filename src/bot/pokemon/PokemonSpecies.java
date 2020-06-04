package bot.pokemon;

import java.util.EnumMap;

import bot.io.json.node.JsonObjectNode;

import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies;

public class PokemonSpecies {
	
	private final String name;
	private final int dex;
	private final byte catchRate;
	private final EggGroup[] eggGroups;
	private final int femaleRate; // in eighths
	private final PokemonSpecies evolvesFrom;
	private final EvolutionChain evoChain;
	private final Habitat habitat;
	private final GrowthRate growthRate;
	private final EnumMap<Stat, Integer> baseStats;
	
	public PokemonSpecies(JsonObjectNode node) {
		
	}
	
}
