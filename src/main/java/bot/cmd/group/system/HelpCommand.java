package bot.cmd.group.system;

import java.util.List;

import bot.Core;
import bot.cmd.ActionableCommand;
import bot.cmd.Command;
import bot.cmd.CommandContext;
import bot.cmd.CommandParent;
import bot.cmd.CommandSet;
import bot.cmd.Option.OptionValues;

import reactor.core.publisher.Mono;

public class HelpCommand extends ActionableCommand {
	
	public HelpCommand() {
		super("help", "View available commands, what they do, and how to use them.", "<command>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, List<String> args) {
		return context.channel.createMessage(getCommandHelp(args)).then();
	}
	
	private String getCommandHelp(List<String> args) {
		CommandSet cmdSet = Core.rootCommands;
		Command cmd = null;
		for(String arg: args) {
			cmd = cmdSet.fetch(arg);
			if(cmd == null)
				return "that command doesn't seem to exist.";
			if(cmd instanceof CommandParent)
				cmdSet = ((CommandParent)cmd).subCommands;
			else
				break;
		}
		
		if(cmd == null)
			return "Available commands: `"+String.join("`, `", cmdSet.getCommandNames())+"`";
		
		if(cmd instanceof CommandParent) {
			CommandParent cp = (CommandParent) cmd;
			String info = "Available subcommands: "+cp.subCommandListing;
			if(cp.defaultCommand != null)
				info += " (default: `"+cp.defaultCommand.getName()+"`)";
			
			return info;
		}
		
		ActionableCommand ac = (ActionableCommand) cmd;
		
		return "`"+ac.getName()+"`: "+ac.description+"\nusage: `"+ac.usage+"`"+ac.optionHelp;
	}
}
