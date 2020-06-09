package bot.pokemon.external;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

public class Importer {
	
	private Importer() {}
	
	public static final int MAX_DEX_NUMBER = 807;
	// public static final int MAX_DEX_NUMBER = 151;
	
	public static final ObjectMapper jsonMapper = new ObjectMapper();
	
	// some methods and such to import pokemon data from PokeAPI
	// if I don't end up actually importing them, then this will instead be an API from which I get the data.
	
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
	
	private interface ResourceWriter<T> {
		void write(JsonGenerator g, T resource) throws IOException;
	}
	
	private static final HttpRequestFactory reqFactory = new NetHttpTransport().createRequestFactory();
	
	// download data
	public static void main(String[] args) throws IOException {
		// writeData("species", api.getPokemonSpeciesList(0, MAX_DEX_NUMBER).getResults(), api::getPokemonSpecies, JsonGenerator::writeObject);
		
		// download a list straight from PokeAPI
		
		DataImports.AllMoves.downloadData();
	}
	
	public static String readData(String urlPrefix, String urlSuffix, int maxIdx) {
		String[] data = new String[maxIdx];
		for(int i = 1; i <= maxIdx; i++) {
			System.out.println("reading data "+i);
			try {
				data[i - 1] = reqFactory.buildGetRequest(new GenericUrl("https://pokeapi.co/api/v2/" + urlPrefix + "/" + i + urlSuffix)).execute().parseAsString();
			} catch(IOException e) {
				System.out.println("error with data "+i+", skipping");
			}
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ignored) {}
		}
		return "["+String.join(",", data)+"]";
	}
	
	/*private static void writeMoves() throws IOException {
		writeData("moves", api.getMoveList(0, 165).getResults(), api::getMove, (g, move) -> {
			g.writeStartObject();
			g.writeObjectField("id", move.getId());
			g.writeObjectField("name", move.getName());
			g.writeObjectField("accuracy", move.getAccuracy());
			g.writeObjectField("effect-chance", move.getEffectChance());
			g.writeObjectField("pp", move.getPp());
			g.writeObjectField("priority", move.getPriority());
			g.writeObjectField("power", move.getPower());
			
			g.writeObjectField("damage-class", move.getDamageClass());
			g.writeObjectFieldStart("effect-description");
			g.writeObjectField("long", move.getEffectEntries().get(0).getEffect());
			g.writeObjectField("short", move.getEffectEntries().get(0).getShortEffect());
			// g.writeObjectField("flavor", move.());
			g.writeEndObject();
			g.writeObjectField("meta", move.getMeta());
			g.writeObjectField("stat-changes", move.getStatChanges());
			g.writeObjectField("target", move.getTarget());
			g.writeObjectField("type", move.getType());
			g.writeEndObject();
		});
	}*/
	
	/*private static <T> void writeData(String dataType, List<NamedApiResource> resourceList, Function<Integer, T> converter, ResourceWriter<T> writer) throws IOException {
		JsonGenerator g = jsonMapper.getFactory().createGenerator(new FileWriter(dataType+".json"));
		g.writeStartObject();
		g.writeArrayFieldStart(dataType);
		for(NamedApiResource resource: resourceList) {
			T converted = converter.apply(resource.getId());
			writer.write(g, converted);
		}
		g.writeEndArray();
		g.writeEndObject();
		g.close();
		System.out.println("file written");
	}*/
}
