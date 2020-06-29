package bot.command;

import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.OptionSet.OptionValues;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

// a command that can be executed without further subcommands
public abstract class ActionableCommand extends Command {
	
	private final OptionSet options;
	
	private final ArgumentSet arguments;
	
	private final String description;
	private final String usage;
	
	private final String helpString;
	
	protected ActionableCommand(String name, String description, String... arguments) {
		this(name, description, ArgumentSet.get(arguments));
	}
	protected ActionableCommand(String name, String description, ArgumentSet arguments, Option... options) {
		this(name, description, arguments, options.length == 0 ? OptionSet.NO_OPTS : new OptionSet(options));
	}
	protected ActionableCommand(String name, String description, ArgumentSet arguments, OptionSet options) {
		super(name);
		this.description = description;
		this.arguments = arguments;
		this.options = options;
		
		String usage = name;
		if(options.hasOptions())
			usage += " [options...]";
		if(arguments.hasArgs())
			usage += " " + arguments.getUsage();
		
		this.usage = usage;
		
		this.helpString = "Usage: `"+usage+"`\n" + description + (options.hasOptions() ? "\n\n" : "")+options.getHelp();
	}
	
	@Override
	public final Mono<Void> execute(CommandContext context) {
		OptionValues optionValues = options.parseOptions(context);
		
		try {
			String[] argValues = arguments.parseArguments(context, false);
			return execute(context, optionValues, argValues);
		} catch(ArgumentCountException e) {
			throw new UsageException(e.missingArgs + " argument" + (e.missingArgs == 1 ? " is" : "s are")+" missing.");
			// throw new UsageException(e.insertArgCountString("is", "are")+" missing.");
		}
	}
	
	protected abstract Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException;
	
	@Override
	public String getHelp() {
		return helpString;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public String getUsage() {
		return usage;
	}
}
