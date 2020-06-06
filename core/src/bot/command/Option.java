package bot.command;

import bot.command.ArgumentSet.ArgumentCountException;
import bot.util.UsageException;

import org.jetbrains.annotations.NotNull;

// represents an optional parameter to a command
public class Option implements Comparable<Option> {
	
	private final String name;
	private final char abbrev;
	
	private final ArgumentSet args;
	
	private final String help;
	
	public Option(String name, char abbrev, String description, String... args) {
		this(name, abbrev, description, new ArgumentSet(args));
	}
	public Option(String name, char abbrev, String description, ArgumentSet args) {
		this.name = name;
		this.abbrev = abbrev;
		this.args = args;
		
		String usage = "--" + name + "|-" + abbrev + (args.hasArgs() ? " " : "") + args.getUsage();
		
		this.help = usage + "\n\t" + description;
	}
	
	@Override
	public int compareTo(@NotNull Option o) {
		return name.compareTo(o.name);
	}
	
	public String getName() { return name; }
	public char getAbbrev() { return abbrev; }
	
	public String getHelp() { return help; }
	
	public String[] parseOptionArgs(CommandContext context, String referenceString) {
		try {
			return args.parseArguments(context, true);
		} catch(ArgumentCountException e) {
			throw new UsageException("option "+referenceString+" is missing at least one argument.");
		}
	}
}
