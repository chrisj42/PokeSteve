package bot.command;

import java.util.function.Function;

import bot.data.DataCore;
import bot.world.pokemon.PokemonSpecies;
import bot.world.pokemon.external.Importer;
import bot.util.UsageException;
import bot.world.pokemon.move.Move;

public class ArgType<T> {
	
	public static final ArgType<String> TEXT = new ArgType<>(String::toString);
	public static final ArgType<Integer> INTEGER = new ArgType<>(Integer::parseInt);
	public static final ArgType<Float> DECIMAL = new ArgType<>(Float::parseFloat);
	
	public static final ArgType<PokemonSpecies> POKEMON = new ArgType<>(val -> {
		PokemonSpecies species;
		try {
			int dexNumber = Integer.parseInt(val);
			species = DataCore.POKEMON.get(dexNumber);
			if(species == null)
				throw new UsageException("Could not find a pokemon with the pokedex number "+dexNumber+". Note that this bot only knows of pokemon up to generation 7, where the national dex ends on #"+Importer.MAX_DEX_NUMBER+".");
		} catch(NumberFormatException e) {
			species = DataCore.POKEMON.get(val);
			if(species == null)
				throw new UsageException("Could not find a pokemon with the name \""+val+"\". Note that this bot only knows of pokemon up to generation 7, gen 8+ are not yet supported.");
		}
		
		return species;
	});
	
	public static final ArgType<Move> MOVE = new ArgType<>(val -> {
		Move move = DataCore.MOVES.get(val);
		if(move == null)
			throw new UsageException("Could not find move with name \""+val+"\".");
		return move;
	});
	
	private final Function<String, T> argParser;
	
	public ArgType(Function<String, T> argParser) {
		this.argParser = argParser;
	}
	
	public T parseArg(String arg) {
		try {
			return argParser.apply(arg);
		} catch(Exception e) {
			if(e instanceof UsageException) throw e;
			throw new UsageException("Argument \""+arg+"\" has invalid format", e);
		}
	}
}
