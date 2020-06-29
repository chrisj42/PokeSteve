package bot.command.group.debug;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.Option;
import bot.command.OptionSet.OptionValues;
import bot.world.pokemon.Pokemon;

import reactor.core.publisher.Mono;

public class SpawnCommand extends ActionableCommand {
	
	public static int DEFAULT_LEVEL = 100;
	
	static final Option LEVEL_OPT = new Option("level", 'l', "select pokemon level", "<level>");
	public SpawnCommand() {
		super("spawn", "Spawn a wild pokemon and list information about it.", ArgumentSet.get("<pokemon name or dex number>"), LEVEL_OPT);
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		requireArgs(1, args);
		
		int level = DEFAULT_LEVEL;
		if(options.hasOption(LEVEL_OPT))
			level = options.getOptionValue(LEVEL_OPT, ArgType.INTEGER);
		
		Pokemon pokemon = ArgType.POKEMON.parseArg(args[0]).spawnPokemon(level);
		return context.channel.createEmbed(pokemon::buildEmbed).then();
	}
}
