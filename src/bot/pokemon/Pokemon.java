package bot.pokemon;

import java.util.EnumMap;

import bot.io.json.node.JsonObjectNode;

import org.jetbrains.annotations.NotNull;

public class Pokemon {
	
	@NotNull
	private final PokemonSpecies species;
	private final Nature nature;
	private final Gender gender;
	private final EnumMap<Stat, Integer> stats;
	private final Move[] moveset;
	
	public Pokemon(JsonObjectNode node) {
		
	}
	
}
