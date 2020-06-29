package bot.command.group.pokemon;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.data.DataCore;
import bot.data.UserData;
import bot.util.UsageException;
import bot.util.Utils;
import bot.world.pokemon.PokemonSpecies;

import reactor.core.publisher.Mono;

public class StarterCommand extends ActionableCommand {
	
	private static final int[] GEN_OFFSETS = {1, 152, 252, 387, 495, 650, 722};
	private final static TreeMap<Integer, PokemonSpecies> STARTER_POKEMON = new TreeMap<>();
	static {
		for(int off: GEN_OFFSETS) {
			STARTER_POKEMON.put(off, DataCore.POKEMON.get(off));
			STARTER_POKEMON.put(off+3, DataCore.POKEMON.get(off+3));
			STARTER_POKEMON.put(off+6, DataCore.POKEMON.get(off+6));
		}
	}
	
	public StarterCommand() {
		super("starter", "Pick your starter. Give no arguments to see all available starters.", "<pokemon name or dex number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length == 0)
			throw new UsageException("Available starters: "+String.join(", ", Utils.map(STARTER_POKEMON.values(), PokemonSpecies::toString)));
		
		UserData data = UserData.getData(context.user);
		if(data != null)
			throw new UsageException("You've already selected your starter pokemon.");
		
		PokemonSpecies starter = ArgType.POKEMON.parseArg(args[0]);
		
		if(!STARTER_POKEMON.containsKey(starter.dex))
			throw new UsageException("That is not a valid starter pokemon.");
		
		UserData.createData(context.user, starter.spawnPokemon(1));
		
		return context.channel.createMessage("Congrats, you now have a level 1 "+starter+"!").then();
	}
}
