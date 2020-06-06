package bot.command.group.world;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.DataCore;
import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class SpawnCommand extends ActionableCommand {
	
	public SpawnCommand() {
		super("spawn", "(debug command) spawn a wild pokemon and list information about it.", "<pokemon id>");
		// super("spawn", "(debug command) spawn a wild pokemon and list all its characteristics.", "<pokemon id>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length == 0)
			throw new ArgumentCountException(1);
		
		Pokemon pokemon = getPokemon(args[0]);
		return context.channel.createMessage(pokemon.buildInfo()).then();
	}
	
	private static Pokemon getPokemon(String idString) {
		int id = ArgType.INTEGER.parseArg(idString);
		PokemonSpecies species = DataCore.POKEMON.get(id);
		if(species == null)
			throw new UsageException("no matching pokemon exists.");
		
		return species.spawnPokemon();
	}
}
