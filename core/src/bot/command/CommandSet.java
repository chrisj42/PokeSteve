package bot.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import bot.util.UsageException;

import org.jetbrains.annotations.Nullable;

public class CommandSet {
	
	private LinkedHashMap<String, Command> commandMap;
	// private final Command[] commands;
	
	private CommandSet(int count) {
		commandMap = new LinkedHashMap<>(count < 2 ? 1 : Math.max(4, count * 7 / 4));
	}
	
	public static CommandSet of(Command... commands) { return of(Arrays.asList(commands)); }
	public static CommandSet of(Collection<Command> commands) {
		CommandSet set = new CommandSet(commands.size());
		for(Command c: commands)
			set.commandMap.put(c.getName().toLowerCase(), c);
		return set;
	}
	public static CommandSet from(RootCommands... commands) { return from(Arrays.asList(commands)); }
	public static CommandSet from(Collection<RootCommands> commands) {
		CommandSet set = new CommandSet(commands.size());
		for(RootCommands c: commands)
			set.commandMap.put(c.get().getName().toLowerCase(), c.get());
		return set;
	}
	
	@Nullable
	public Command fetch(String name) {
		return commandMap.get(name.toLowerCase());
	}
	
	/*public Collection<Command> getCommands() {
		return commandMap.values();
	}*/
	
	public Set<String> getCommandNames() {
		return commandMap.keySet();
	}
}
