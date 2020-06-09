package bot.command.group.world;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.Option;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.DataCore;
import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class SpawnCommand extends ActionableCommand {
	
	public static int DEFAULT_LEVEL = 100;
	
	private static Option LEVEL_OPT = new Option("level", 'l', "select pokemon level", "<level>");
	public SpawnCommand() {
		super("spawn", "(debug command) spawn a wild pokemon and list information about it.", new ArgumentSet("<pokemon name or dex number>"), LEVEL_OPT);
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length == 0)
			throw new ArgumentCountException(1);
		
		int level = DEFAULT_LEVEL;
		if(options.hasOption(LEVEL_OPT))
			level = options.getOptionValue(LEVEL_OPT, ArgType.INTEGER);
		
		Pokemon pokemon = ArgType.POKEMON.parseArg(args[0]).spawnPokemon(level);
		return context.channel.createMessage(pokemon.buildInfo()).then();
	}
}
