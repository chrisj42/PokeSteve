package bot.cmd;

import reactor.core.publisher.Mono;

// a command that has no effect on its own, but holds child commands
public class CommandParent extends Command {
	
	public final CommandSet subCommands;
	public final Command defaultCommand;
	public final String subCommandListing;
	
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
		
		return context.channel.createMessage("I need more information; add one of these: "+subCommandListing).then();
	}
	
}
