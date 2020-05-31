package bot.command.group.system;

import bot.Core;
import bot.command.ActionableCommand;
import bot.command.Command;
import bot.command.CommandContext;
import bot.command.CommandParent;
import bot.command.CommandSet;
import bot.command.OptionSet.OptionValues;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class HelpCommand extends ActionableCommand {
	
	public HelpCommand() {
		super("help", "View available commands, what they do, and how to use them.", "command name");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		if(args.length == 0)
			return context.channel.createMessage(
				"Available commands: `"+String.join("`, `", Core.rootCommands.getCommandNames())+"`"
			).then();
		
		CommandSet cmdSet = Core.rootCommands;
		Command cmd = null;
		for(String arg: args) {
			cmd = cmdSet.fetch(arg);
			if(cmd == null)
				throw new UsageException("that command doesn't seem to exist.");
			if(cmd instanceof CommandParent)
				cmdSet = ((CommandParent)cmd).getSubCommands();
			else
				break;
		}
		
		return context.channel.createMessage(
			"`"+cmd.getName()+"` Command Help:\n"+cmd.getHelp()
		).then();
	}
}
