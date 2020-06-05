package bot.command;

import java.util.TreeMap;

import bot.command.ArgumentSet.ArgumentCountException;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class OptionSet {
	
	public static final OptionSet NO_OPTS = new OptionSet();
	
	private final Option[] options;
	
	// private final TreeMap<Character, Option> shortOptionMap = new TreeMap<>();
	// private final TreeMap<String, Option> longOptionMap = new TreeMap<>();
	private final TreeMap<String, Option> optionMap = new TreeMap<>();
	
	private final String help;
	
	public OptionSet(Option... options) {
		this.options = options;
		if(options.length == 0) {
			this.help = "";
			return;
		}
		
		StringBuilder help = new StringBuilder("Options:");
		for(Option o: options) {
			// shortOptionMap.put(o.getAbbrev(), o);
			optionMap.put(o.getName(), o);
			help.append("\n").append(o.getHelp());
		}
		this.help = help.toString();
	}
	
	public boolean hasOptions() {
		return options.length > 0;
	}
	
	public String getHelp() {
		return help;
	}
	
	public OptionValues parseOptions(CommandContext context) {
		return new OptionValues(context);
	}
	
	public class OptionValues {
		
		private TreeMap<Option, String[]> values;
		
		private OptionValues(CommandContext context) {
			this.values = new TreeMap<>();
			
			String optionId;
			while((optionId = context.nextArgument()) != null) {
				if(!optionId.startsWith("--")) {
					// no more options
					context.rewindArgument();
					break;
				}
				
				if(optionId.equals("--"))
					break; // use the next arguments without refunding this one, as it's just a marker
				
				/*if(optionId.startsWith("--")) // a single option given by the full name
					parseOption(optionMap.get(optionId.substring(2)), optionId, context);
				else {
					// a number of 
					for(char c: optionId.substring(1).toCharArray()) {
						parseOption(shortOptionMap.get(c), "-"+c, context);
					}
				}*/
				
				// is option
				Option curOption = optionMap.get(optionId.substring(2));
				if(curOption == null)
					throw new UsageException("unrecognized option `"+optionId+"`.");
				
				values.put(curOption, curOption.parseOptionArgs(context, optionId));
			}
		}
		
		/*private void parseOption(Option curOption, String optionString, CommandContext context) {
			if(curOption == null)
				throw new UsageException("unrecognized option `"+optionString+"`.");
			
			values.put(curOption, curOption.parseOptionArgs(context, optionString));
		}*/
		
		public boolean hasOption(Option o) {
			return values.containsKey(o);
		}
		
		public <T> T getOptionValue(Option o, ArgType<T> type) {
			return getOptionValue(o, 0, type);
		}
		public <T> T getOptionValue(Option o, int argIdx, ArgType<T> type) {
			return type.parseArg(values.get(o)[argIdx]);
		}
	}
}
