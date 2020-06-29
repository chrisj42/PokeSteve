package bot.command;

import bot.util.UsageException;

import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

// a command that has no effect on its own, but holds child commands
public class CommandParent extends Command {
	
	private final CommandSet subCommands;
	private final Command defaultCommand;
	
	private final String subCommandListing;
	private final String description;
	private final String usage;
	private final String help;
	
	protected CommandParent(String name, String description, Command... subCommands) { this(name, description, false, subCommands); }
	protected CommandParent(String name, String description, boolean firstIsDefault, Command... subCommands) {
		super(name);
		this.description = description;
		this.subCommands = CommandSet.of(subCommands);
		
		if(firstIsDefault)
			defaultCommand = subCommands[0];
		else
			defaultCommand = null;
		
		subCommandListing = "`"+String.join("`, `", this.subCommands.getCommandNames())+"`";
		
		final boolean req = defaultCommand == null;
		usage = getName() + " " + (req?"<":"[") + String.join("|", this.subCommands.getCommandNames()) + (req?">":"]");
		
		StringBuilder help = new StringBuilder(description).append("\n\n*Available sub-commands:*");
		if(defaultCommand != null)
			help.append(" (default is `").append(defaultCommand.getName()).append("`)");
			// help.append("\nIf none are given, `").append(defaultCommand.getName()).append("` is assumed.");
		for(Command sub: subCommands)
			help.append("\n`").append(sub.getUsage()).append("`\n\t").append(sub.getDescription());
		
		this.help = help.toString();
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
	public String getDescription() { return description; }
	
	@Override
	public String getUsage() { return usage; }
	
	@Override
	public String getHelp() { return help; }
}
