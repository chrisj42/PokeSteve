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
		this(name, description, arguments.length == 0 ? ArgumentSet.NO_ARGS : new ArgumentSet(arguments));
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
		
		this.helpString = "Usage: `"+usage+"`\n" + description + (options.hasOptions() ? "\n" : "")+options.getHelp();
	}
	
	@Override
	public Mono<Void> execute(CommandContext context) {
		OptionValues optionValues = options.parseOptions(context);
		
		String[] argValues;
		try {
			argValues = arguments.parseArguments(context);
		} catch(ArgumentCountException e) {
			throw new UsageException(e.missingArgs + " argument" + (e.missingArgs == 1 ? " is" : "s are")+" missing.");
			// throw new UsageException(e.insertArgCountString("is", "are")+" missing.");
		}
		
		return execute(context, optionValues, argValues);
	}
	
	protected abstract Mono<Void> execute(CommandContext context, OptionValues options, String[] args);
	
	@Override
	public String getHelp() {
		return helpString;
	}
	
	protected String getDescription() {
		return description;
	}
	
	protected String getUsage() {
		return usage;
	}
}
