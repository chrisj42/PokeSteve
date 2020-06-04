package bot.pokemon.external;

import java.util.TreeMap;

import bot.pokemon.PokemonSpecies;
import bot.util.UsageException;

public class Importer {
	
	private Importer() {}
	
	// public static final int MAX_DEX_NUMBER = 807;
	public static final int MAX_DEX_NUMBER = 151;
	
	// some methods and such to import pokemon data from PokeAPI
	// if I don't end up actually importing them, then this will instead be an API from which I get the data.
	
	private static final TreeMap<String, PokemonSpecies> nameToSpeciesMap = new TreeMap<>();
	private static final PokemonSpecies[] dexOrderedSpecies = new PokemonSpecies[MAX_DEX_NUMBER+1]; // index 0 won't be used
	
	static {
		// initialization
		// List<NamedApiResource> speciesList = api.getPokemonSpeciesList(0, MAX_DEX_NUMBER).getResults();
	}
	
	public static PokemonSpecies getSpecies(int dex) {
		if(dex >= MAX_DEX_NUMBER)
			throw new UsageException("that is past the max dex value available. Current max is "+MAX_DEX_NUMBER+".");
		if(dex <= 0)
			throw new UsageException("Well at least you're being a good tester saul, but no, there are no pokemon with negative dex values.");
		// if(dexOrderedSpecies[dex] == null)
		// 	dexOrderedSpecies[dex] = //api.getPokemonSpecies(dex);
		return dexOrderedSpecies[dex];
	}
	
	public static PokemonSpecies getSpecies(String name) {
		throw new UsageException("not quite implemented yet, oops");
		// return nameToSpeciesMap.computeIfAbsent(name, pname -> api.);
		// return nameToSpeciesMap.get(name);
	}
	
	/*
		when you talk in a server that the bot is also in, there's a chance a pokemon could spawn (will be shown in your DMs)
			- only if you've told the bot that you're actively searching for pokemon
		
		possible features:
			- berry farming
			- breeding
			- trading
			- user battles
			- pokemart
		
		common move effects:
			- damage
			- status
			- switch
			- hits
			- cancel move
			
		common ability effects:
			- cancel move
			- change forms
			- 
	 */
}
