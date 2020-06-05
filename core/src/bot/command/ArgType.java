package bot.command;

import java.util.function.Function;

import bot.util.UsageException;

public class ArgType<T> {
	
	public static final ArgType<String> TEXT = new ArgType<>(String::toString);
	public static final ArgType<Integer> INTEGER = new ArgType<>(Integer::parseInt);
	public static final ArgType<Float> DECIMAL = new ArgType<>(Float::parseFloat);
	
	public static final ArgType<Float> POKEMON = new ArgType<>(Float::parseFloat);
	
	private final Function<String, T> argParser;
	
	public ArgType(Function<String, T> argParser) {
		this.argParser = argParser;
	}
	
	public T parseArg(String arg) {
		try {
			return argParser.apply(arg);
		} catch(Exception e) {
			throw new UsageException("Argument \""+arg+"\" has invalid format", e);
		}
	}
}
