package bot.command.group.world;

import bot.command.ActionableCommand;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;

import reactor.core.publisher.Mono;

public class SpawnCommand extends ActionableCommand {
	
	public SpawnCommand() {
		super("spawn", "(debug command) spawn a wild pokemon and list all its characteristics.", "<pokemon>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		return context.channel.createMessage("not yet implemented").then();
	}
}
