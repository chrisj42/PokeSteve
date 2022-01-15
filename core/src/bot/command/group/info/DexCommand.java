package bot.command.group.info;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.world.pokemon.PokemonSpecies;

import reactor.core.publisher.Mono;

public class DexCommand extends ActionableCommand {
	
	public DexCommand() {
		super("dex", "View an entry in the pokedex.", "<pokemon name or dex number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		requireArgs(1, args);
		
		PokemonSpecies species = ArgType.POKEMON.parseArg(args[0]);
		
		return context.channel.createMessage(species.buildDexEntry().build()).then();
	}
}
