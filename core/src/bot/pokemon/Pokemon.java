package bot.pokemon;

import java.util.EnumMap;

import bot.io.json.node.JsonObjectNode;

import org.jetbrains.annotations.NotNull;

public class Pokemon {
	
	@NotNull
	private PokemonSpecies species;
	private Nature nature;
	private Gender gender;
	private EnumMap<Stat, Integer> stats;
	private Move[] moveset;
	
	public Pokemon(JsonObjectNode node) {
		
	}
	
}
