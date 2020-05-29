package bot.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import bot.cmd.Option.OptionValues;
import bot.cmd.Option.OptionValues.ArgumentCountException;

import reactor.core.publisher.Mono;

import org.jetbrains.annotations.Nullable;

// a command that can be executed without further subcommands
public abstract class ActionableCommand extends Command {
	
	private final Option[] options;
	private final TreeMap<Character, Option> shortOptionMap = new TreeMap<>();
	private final TreeMap<String, Option> longOptionMap = new TreeMap<>();
	
	public final String description;
	public final String usage;
	public final String optionHelp;
	
	protected ActionableCommand(String name, String description, Option... options) { this(name, description, null, options); }
	protected ActionableCommand(String name, String description, @Nullable String extraArgsUsage, Option... options) {
		super(name);
		this.options = options;
		
		this.description = description;
		
		StringBuilder use = new StringBuilder(name);
		
		if(options.length > 0)
			use.append(" [options...]");
		
		if(extraArgsUsage != null)
			use.append(" ").append(extraArgsUsage);
		
		this.usage = use.toString();
		
		StringBuilder optionHelp = new StringBuilder(options.length > 0 ? "\nOptions:" : "");
		int i = 0;
		for(Option o: options) {
			o.setPos(i++);
			shortOptionMap.put(o.abbrev, o);
			longOptionMap.put(o.name, o);
			optionHelp.append("\n").append(o.usage).append("\n\t").append(o.description);
		}
		this.optionHelp = optionHelp.toString();
	}
	
	@Override
	public Mono<Void> execute(CommandContext context) {
		OptionValues optionValues = new OptionValues(options);
		
		String optionId;
		while((optionId = context.nextArgument()) != null) {
			if(!optionId.startsWith("-")) {
				// no more options
				context.rewindArgument();
				break;
			}
			
			if(optionId.equals("--"))
				break; // use the next arguments without refunding this one, as it's just a marker
			
			// Option curOption;
			if(optionId.startsWith("--")) {
				Mono<Void> error = parseOption(longOptionMap.get(optionId.substring(2)), optionId, optionValues, context);
				if(error != null)
					return error;
			}
			else {
				for(char c: optionId.substring(1).toCharArray()) {
					Mono<Void> error = parseOption(shortOptionMap.get(c), "-"+c, optionValues, context);
					if(error != null)
						return error;
				}
			}
		}
		
		return execute(context, optionValues, context.getRemainingArgs());
	}
	
	// return value signifies an error
	private Mono<Void> parseOption(Option curOption, String optionString, OptionValues optionValues, CommandContext context) {
		if(curOption == null) {
			// context.rewindArgument();
			// break;
			return context.channel.createMessage("unrecognized option `"+optionString+"`.").then();
		}
		try {
			optionValues.readOptionValues(curOption, context);
		} catch(ArgumentCountException e) {
			return context.channel.createMessage("option "+optionString+" is missing at least one argument.").then();
		}
		
		return null;
	}
	
	protected abstract Mono<Void> execute(CommandContext context, OptionValues options, List<String> args);
	
}
