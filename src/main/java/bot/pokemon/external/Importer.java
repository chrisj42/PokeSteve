package bot.pokemon.external;

import me.sargunvohra.lib.pokekotlin.client.PokeApiClient;

public class Importer {
	
	// some methods and such to import pokemon data from PokeAPI
	// if I don't end up actually importing them, then this will instead be an API from which I get the data.
	
	// TODO next key thing to do: determination of pokemon stats and moveset when they spawn. From there I can work on battle instances.
	
	public Importer() {
		PokeApiClient api = new PokeApiClient();
		
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
