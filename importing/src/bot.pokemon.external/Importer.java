package bot.pokemon.external;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient;
import me.sargunvohra.lib.pokekotlin.model.NamedApiResource;
import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies;

public class Importer {
	
	private Importer() {}
	
	// public static final int MAX_DEX_NUMBER = 807;
	public static final int MAX_DEX_NUMBER = 151;
	
	public static final ObjectMapper jsonMapper = new ObjectMapper();
	
	// some methods and such to import pokemon data from PokeAPI
	// if I don't end up actually importing them, then this will instead be an API from which I get the data.
	
	private static final PokeApiClient api = new PokeApiClient();
	
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
	
	// download data
	public static void main(String[] args) throws IOException {
		List<NamedApiResource> speciesList = api.getPokemonSpeciesList(0, MAX_DEX_NUMBER).getResults();
		
		JsonGenerator g = jsonMapper.getFactory().createGenerator(new FileWriter("species.json"));
		g.writeStartObject();
		g.writeArrayFieldStart("species");
		for(NamedApiResource resource: speciesList) {
			PokemonSpecies species = api.getPokemonSpecies(resource.getId());
			
		}
		g.writeEndArray();
		g.writeEndObject();
		g.close();
		System.out.println("file written");
		// Core.jsonMapper.writeValue(new File("src/main/resources/species.json"), );
	}
}
