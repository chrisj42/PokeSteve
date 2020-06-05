package bot.command;

import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.group.system.HelpCommand;

import reactor.core.publisher.Mono;

public abstract class Command {
	
	private final String name;
	
	protected Command(String name) {
		this.name = name;
	}
	
	public String getName() { return name; }
	
	public abstract Mono<Void> execute(CommandContext context);
	
	public abstract String getHelp();
	
	// attempts to match the next argument in the context with a command from the given list
	public static Command tryParseSubCommand(CommandSet commands, CommandContext context) {
		String commandName = context.nextArgument();
		if(commandName == null)
			return null;
		Command command = commands.fetch(commandName);
		if(command == null)
			context.rewindArgument();
		return command;
	}
	
	@Override
	public String toString() {
		return name+"-command";
	}
}
