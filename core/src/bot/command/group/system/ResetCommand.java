package bot.command.group.system;

import bot.command.ActionableCommand;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.data.UserData;

import reactor.core.publisher.Mono;

public class ResetCommand extends ActionableCommand {
	
	public ResetCommand() {
		super("reset", "Clear your user profile of all data. This is irreversible!");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		if(args.length > 0) {
			if(!args[0].equals(context.user.getId().asString()))
				return context.channel.createMessage("Argument does not match user id.\nIf you don't know what you're doing, run without arguments: `reset`.")
				.then();
			
			// reset data
			UserData.deleteData(context.user);
			return context.channel.createMessage("Your user profile has been deleted. Use the `starter` command to select a starter.")
				.then();
		}
		
		UserData data = UserData.reqData(context.user);;
		return context.channel.createMessage("Are you absolutely sure you want to reset your profile?\nThis will delete ALL your progress and saved data.\n**Once done, all your data is gone permanently and cannot be recovered.**\n\nIf you still wish to reset your profile, run the command again with your user id: `reset "+data.userId.asString()+"`.")
			.then();
	}
}
