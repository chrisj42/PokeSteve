package bot.command;

public class ArgumentSet {
	
	public static final ArgumentSet NO_ARGS = new ArgumentSet();
	
	public static ArgumentSet get(String... args) {
		return args.length == 0 ? NO_ARGS : new ArgumentSet(args);
	}
	
	private final String[] args;
	
	private final String usage;
	
	private ArgumentSet(String... args) {
		this.args = args;
		
		this.usage = String.join(" ", args);
	}
	
	// public int argCount() { return args.length; }
	public boolean hasArgs() {
		return args.length > 0;
	}
	
	public String getUsage() { return usage; }
	
	public String[] parseArguments(CommandContext context, boolean validateArgs) throws ArgumentCountException {
		if(validateArgs) {
			String[] argValues = new String[args.length];
			for(int i = 0; i < args.length; i++) {
				argValues[i] = context.nextArgument();
				if(argValues[i] == null)
					throw new ArgumentCountException(args.length - i);
			}
			return argValues;
		}
		return context.getRemainingArgs().toArray(new String[0]);
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
