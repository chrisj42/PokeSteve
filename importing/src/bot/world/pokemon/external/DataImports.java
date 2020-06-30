package bot.world.pokemon.external;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public enum DataImports {
	
	// Gen1Moves("move", "gen1", 165),
	
	AllMoves("move", null, 746),
	
	// Gen1PokemonSpecies("pokemon-species", "gen1", 151),
	
	// Gen1Pokemon("pokemon", "gen1", 151),
	
	AllPokemonSpecies("pokemon-species", null, 807),
	
	AllPokemon("pokemon", null, 807),
	
	// Gen1Encounters("encounters", "pokemon", "/encounters", "gen1", 151),
	
	// Gen1EvoChains("evolution-chain", "gen1", 78),
	
	EvoChains("evolution-chain", null, 419),
	
	Locations("location", null, 781),
	
	LocationAreas("location-area", null, 683);
	
	private final String urlPrefix;
	private final String urlSuffix;
	private final String fileSuffix;
	public final String type;
	public final int maxIdx;
	
	DataImports(String type, String fileSuffix, int maxIdx) {
		this(type, type, "", fileSuffix, maxIdx);
	}
	DataImports(String type, String urlPrefix, String urlSuffix, String fileSuffix, int maxIdx) {
		this.urlPrefix = urlPrefix;
		this.type = type;
		this.urlSuffix = urlSuffix;
		this.fileSuffix = fileSuffix;
		this.maxIdx = maxIdx;
	}
	
	public void downloadData() throws IOException {
		final String data = Importer.readData(urlPrefix, urlSuffix, maxIdx);
		String suffix = fileSuffix != null ? "-"+fileSuffix : "";
		Files.write(new File(type+suffix+".json").toPath(), Collections.singleton(data));
		System.out.println("file written");
	}
}
