package bot.cmd;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

public class CommandSet {
	
	private LinkedHashMap<String, Command> commandMap;
	// private final Command[] commands;
	
	public CommandSet(Command... commands) {
		// this.commands = commands;
		commandMap = new LinkedHashMap<>(commands.length < 2 ? 1 : Math.max(4, commands.length * 7 / 4));
		for(Command c: commands)
			commandMap.put(c.getName().toLowerCase(), c);
	}
	
	@Nullable
	public Command fetch(String name) {
		return commandMap.get(name.toLowerCase());
	}
	
	public Collection<Command> getCommands() {
		return commandMap.values();
	}
	
	public Set<String> getCommandNames() {
		return commandMap.keySet();
	}
}
