package bot.command.group.info;

import bot.command.ActionableCommand;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.data.UserData;

import reactor.core.publisher.Mono;

public class StatsCommand extends ActionableCommand {
	
	public StatsCommand() {
		super("stats", "View pokemon and battle statistics.");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		UserData data = UserData.reqData(context.user);
		
		return context.channel.createMessage(data.buildStatistics().build()).then();
	}
}
