package bot.command.group.info;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import bot.Core;
import bot.command.*;
import bot.command.OptionSet.OptionValues;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class HelpCommand extends ActionableCommand {
	
	// private static final Option STATE_OPT = new Option("state", 's', "show help for commands usable in other bot states, like during battle or trade.", "Idle|Battle"/*String.join("|", Utils.map(Arrays.asList(UserState.values), Enum::name))*/);
	
	private String rootHelp;
	private String rootHelpDebug;
	
	public HelpCommand() {
		super("help", "Show a list of available commands, or provide help on a single command.",
			ArgumentSet.get("[command name]")/*, STATE_OPT*/
		);
	}
	
	private static void append(StringBuilder main, StringBuilder debug, boolean debugOnly, Object... values) {
		for(Object o: values) {
			if(!debugOnly) main.append(o);
			debug.append(o);
		}
	}
	
	private String getRootHelp(boolean debug) {
		if(rootHelp == null) {
			StringBuilder mainStr = new StringBuilder();
			StringBuilder debugStr = new StringBuilder();
			for(Entry<String, Collection<RootCommands>> entry: RootCommands.commandsByCategory.entrySet()) {
				final boolean isDebug = entry.getKey().equals("Debug");
				append(mainStr, debugStr, isDebug, "\n***", entry.getKey(), " Commands***");
				for(RootCommands cmd: entry.getValue()) {
					append(mainStr, debugStr, isDebug, "\n\t`", cmd.get().getUsage());
					append(mainStr, debugStr, isDebug, "`\n\t\t", cmd.get().getDescription());
				}
				append(mainStr, debugStr, isDebug, "\n");
			}
			
			append(mainStr, debugStr, false, "\nType `help ` followed by the name of a command for more information about it.");
			
			rootHelp = mainStr.toString();
			rootHelpDebug = debugStr.toString();
		}
		
		return debug ? rootHelpDebug : rootHelp;
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		final CommandSet rootCommands = RootCommands.getCommandsFor(context.user);
		
		if(args.length == 0)
			return context.channel.createEmbed(e -> e
				.setTitle("Available Commands")
				.setDescription(getRootHelp(context.user.getId().equals(Core.devId)))
			).then();
		
		CommandSet cmdSet = rootCommands;
		Command cmd = null;
		Command last = null;
		// String cmdString = null;
		System.out.println(Arrays.toString(args));
		for(String arg: args) {
			// cmdString = cmdString == null ? cmd == null ? null : cmd.getName() : cmdString + (cmd == null ? "" : " "+cmd.getName());
			last = cmd;
			cmd = cmdSet.fetch(arg);
			if(cmd instanceof CommandParent)
				cmdSet = ((CommandParent)cmd).getSubCommands();
			else
				break;
		}
		
		if(cmd == null) {
			if(last == null)
				throw new UsageException("That command doesn't seem to exist."/*+(cmdString == null ? "" : " Did you mean `"+cmdString+"`?")*/);
			cmd = last;
		}
		
		final Command command = cmd;
		return context.channel.createEmbed(e -> e
			.setTitle("`"+command.getName()+"` Command Help:")
			.setDescription(command.getHelp())
		).then();
	}
}
