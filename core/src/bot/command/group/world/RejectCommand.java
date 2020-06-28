package bot.command.group.world;

import bot.UserState;
import bot.command.ActionableCommand;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;

import reactor.core.publisher.Mono;

public class RejectCommand extends ActionableCommand {
	
	public RejectCommand() {
		super("reject", "Reject a duel request.");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		UserState.cancelRequest(context.user);
		return context.channel.createMessage("request rejected.").then();
	}
}
