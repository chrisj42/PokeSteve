package bot.cmd;

// represents an optional parameter to a command
// todo add generics and pass typed objects instead of Strings
public class Option {
	
	public final String name;
	public final char abbrev;
	
	public final Argument<?>[] args;
	
	public final String description;
	public final String usage;
	
	private int pos;
	
	// to-do-maybe allow each argument to be a list of args; or, give them generic types which may include array types...
	// for now, what happens to the content of an option argument is entirely up to the code for the command using the option.
	public Option(String name, char abbrev, String description, Argument<?>... args) {
		this.name = name;
		this.abbrev = abbrev;
		this.args = args;
		this.description = description;
		
		StringBuilder use = new StringBuilder("--").append(name).append("|-").append(abbrev);
		int i = 0;
		for(Argument<?> arg: args) {
			arg.setPos(i++);
			use.append(" ").append(arg.argName);
		}
		this.usage = use.toString();
	}
	
	void setPos(int pos) { this.pos = pos; }
	
	/*public String getName() { return name; }
	public char getAbbrev() { return abbrev; }
	
	public String getDescription() { return description; }
	public String getUsage() { return usage; }*/
	
	// abstracts actual type so that each type can have a more user-friendly description
	/*public enum OptionArgType {
		Text(String.class),
		Integer(int.class),
		Decimal(Float.class),
		Id(Snowflake.class);
		
		public final Class<?> clazz;
		private final String typeName;
		
		OptionArgType(Class<?> clazz, String typeName) {
			this.clazz = clazz;
			this.typeName = typeName;
		}
	}*/
	
	public static class OptionValues {
		
		private Object[][] values;
		
		public OptionValues(Option[] options) {
			values = new Object[options.length][];
		}
		
		public void readOptionValues(Option o, CommandContext context) throws ArgumentCountException {
			Object[] args = new Object[o.args.length];
			for(Argument<?> arg: o.args) {
				final int i = arg.getPos();
				String argString = context.nextArgument();
				if(argString == null)
					throw new ArgumentCountException("insufficient arguments for option "+o.name+"; expected "+o.args.length+", given "+i+".");
				args[i] = o.args[i].type.parseArg(argString);
			}
			values[o.pos] = args;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getOptionArg(Option o, Argument<T> arg) {
			return (T) values[o.pos][arg.getPos()];
		}
		
		public static class ArgumentCountException extends Exception {
			public ArgumentCountException(String message) {
				super(message);
			}
		}
	}
}
