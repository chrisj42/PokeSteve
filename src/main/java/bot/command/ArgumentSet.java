package bot.command;

public class ArgumentSet {
	
	public static final ArgumentSet NO_ARGS = new ArgumentSet();
	
	private final String[] args;
	
	private final String usage;
	
	public ArgumentSet(String... args) {
		this.args = args;
		
		StringBuilder use = new StringBuilder();
		for(String arg: args)
			use.append(" <").append(arg).append(">");
		this.usage = use.toString();
	}
	
	// public int argCount() { return args.length; }
	public boolean hasArgs() {
		return args.length > 0;
	}
	
	public String getUsage() { return usage; }
	
	public String[] parseArguments(CommandContext context) throws ArgumentCountException {
		String[] argValues = new String[args.length];
		for(int i = 0; i < args.length; i++) {
			args[i] = context.nextArgument();
			if(args[i] == null)
				throw new ArgumentCountException(args.length - i);
		}
		
		return argValues;
	}
	
	public static class ArgumentCountException extends Exception {
		public final int missingArgs;
		public final String argString;
		
		public ArgumentCountException(int missingArgs) {
			super(/*missingArgs + " argument"+(missingArgs == 1 ? "" : "s")*/);
			this.missingArgs = missingArgs;
			this.argString = getMessage();
		}
		
		/*public String insertArgCountString() {
			return missingArgs + " argument" + (missingArgs == 1 ? "" : "s");
		}
		public String insertArgCountString(String singularSuffix, String pluralSuffix) {
			return insertArgCountString() + " " + (missingArgs == 1 ? singularSuffix : pluralSuffix);
		}*/
	}
}
