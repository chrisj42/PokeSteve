package bot.pokemon.instance;

import java.util.EnumMap;

import bot.pokemon.definiton.*;

public class Pokemon {
	
	private final PokemonSpecies species;
	private Form form;
	private int level;
	
	private int health;
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	public Pokemon(PokemonSpecies species) {
		this.species = species;
	}
}
