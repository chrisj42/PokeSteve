package bot.command;

import bot.util.UsageException;

import reactor.core.publisher.Mono;

// a command that has no effect on its own, but holds child commands
public class CommandParent extends Command {
	
	private final CommandSet subCommands;
	private final Command defaultCommand;
	private final String subCommandListing;
	
	protected CommandParent(String name, Command... subCommands) { this(name, false, subCommands); }
	protected CommandParent(String name, boolean firstIsDefault, Command... subCommands) {
		super(name);
		this.subCommands = new CommandSet(subCommands);
		
		if(firstIsDefault)
			defaultCommand = subCommands[0];
		else
			defaultCommand = null;
		
		subCommandListing = "`"+String.join("`, `", this.subCommands.getCommandNames());
	}
	
	@Override
	public Mono<Void> execute(CommandContext context) {
		Command next = tryParseSubCommand(subCommands, context);
		if(next != null)
			return next.execute(context);
		
		if(defaultCommand != null)
			return defaultCommand.execute(context);
		
		throw new UsageException("I need more information; add one of these: "+subCommandListing);
	}
	
	public CommandSet getSubCommands() {
		return subCommands;
	}
	
	@Override
	public String getHelp() {
		String help = "Available sub-commands: "+subCommandListing;
		if(defaultCommand != null)
			help += "\nIf none are given, `"+defaultCommand.getName()+"` is assumed.";
		
		return help;
	}
}
