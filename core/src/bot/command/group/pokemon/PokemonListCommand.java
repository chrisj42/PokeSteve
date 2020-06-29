package bot.command.group.pokemon;

import bot.command.ActionableCommand;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;

import reactor.core.publisher.Mono;

public class PokemonListCommand extends ActionableCommand {
	
	public PokemonListCommand() {
		super("list", "List your caught pokemon.", "[page]");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		// requireArgs(1, args);
		
		return context.channel.createMessage("not yet implemented").then();
	}
}
