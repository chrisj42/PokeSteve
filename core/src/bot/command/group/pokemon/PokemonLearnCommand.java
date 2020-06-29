package bot.command.group.pokemon;

import bot.command.ActionableCommand;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;

import reactor.core.publisher.Mono;

public class PokemonLearnCommand extends ActionableCommand {
	
	public PokemonLearnCommand() {
		super("learn", "Add a move to a pokemon's moveset.", "<move name>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length == 0)
			throw new ArgumentCountException(1);
		
		return context.channel.createMessage("not yet implemented").then();
	}
}
