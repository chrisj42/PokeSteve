package bot.command.group.system;

import java.util.Arrays;

import bot.Core;
import bot.UserState;
import bot.command.*;
import bot.command.OptionSet.OptionValues;
import bot.util.UsageException;
import bot.util.Utils;

import reactor.core.publisher.Mono;

public class HelpCommand extends ActionableCommand {
	
	private static final Option STATE_OPT = new Option("state", 's', "show help for commands usable in other bot states, like during battle or trade.", "Idle|Battle"/*String.join("|", Utils.map(Arrays.asList(UserState.values), Enum::name))*/);
	
	public HelpCommand() {
		super("help", "Show a list of available commands, or provide help on a single command.",
			new ArgumentSet("[command name]"), STATE_OPT
		);
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		final CommandSet rootCommands;
		
		if(options.hasOption(STATE_OPT))
			rootCommands = options.getOptionValue(STATE_OPT, new ArgType<>(str -> UserState.valueOf(str).commands));
		else
			rootCommands = Core.getRootCommands(context.user);
		
		if(args.length == 0) {
			return context.channel.createMessage(
				"Available commands: `"+String.join("`, `", rootCommands.getCommandNames())+"`\nType `help ` followed by the name of a command for more information about it."
			).then();
		}
		
		CommandSet cmdSet = rootCommands;
		Command cmd = null;
		System.out.println(Arrays.toString(args));
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
