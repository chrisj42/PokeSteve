package bot.command;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import bot.Core;
import bot.command.group.battle.AttackCommand;
import bot.command.group.battle.FleeCommand;
import bot.command.group.debug.SpawnCommand;
import bot.command.group.info.DexCommand;
import bot.command.group.info.HelpCommand;
import bot.command.group.info.MoveCommand;
import bot.command.group.pokemon.PokemonCommand;
import bot.command.group.pokemon.StarterCommand;
import bot.command.group.world.DuelCommand;
import bot.command.group.world.SearchCommand;
import bot.util.Utils;

import discord4j.core.object.entity.User;

public enum RootCommands {
	
	// all root commands listed here; parent commands will define their subcommands themselves.
	
	// info
	HELP(new HelpCommand()),
	DEX(new DexCommand()),
	MOVE(new MoveCommand()),
	
	// pokemon
	STARTER(new StarterCommand()),
	POKEMON(new PokemonCommand()),
	
	// world
	SEARCH(new SearchCommand()),
	DUEL(new DuelCommand()),
	
	// battle
	ATTACK(new AttackCommand()),
	FLEE(new FleeCommand()),
	
	// debug
	SPAWN(new SpawnCommand());
	
	private final String category;
	private final Command command;
	public final boolean debug;
	
	RootCommands(Command command) {
		this.category = Utils.capitalizeFirst(
			command.getClass().getPackageName()
				.replaceAll(".*\\.", "") // remove parent packages
		);
		this.command = command;
		this.debug = category.equals("Debug");
	}
	
	public Command get() { return command; }
	
	private static EnumSet<RootCommands> not(RootCommands first, RootCommands... commands) {
		return EnumSet.complementOf(EnumSet.of(first, commands));
	}
	
	public static final LinkedHashMap<String, Collection<RootCommands>> commandsByCategory = new LinkedHashMap<>();
	static {
		for(RootCommands cmd: Utils.values(RootCommands.class))
			commandsByCategory
				.computeIfAbsent(cmd.category, c -> new LinkedList<>())
				.add(cmd);
	}
	
	private static final CommandSet ALL_COMMANDS = CommandSet.from(Utils.values(RootCommands.class));
	private static final CommandSet NON_DEBUG_COMMANDS;
	static {
		EnumSet<RootCommands> nonDebug = EnumSet.allOf(RootCommands.class);
		nonDebug.removeIf(cmd -> cmd.debug);
		NON_DEBUG_COMMANDS = CommandSet.from(nonDebug);
	}
	// private static final CommandSet PRE_POKEMON_COMMANDS = CommandSet.from();
	// private static final CommandSet NON_DEBUG_PRE_POKEMON_COMMANDS;
	/*static {
		// ALL_COMMANDS = CommandSet.from(Utils.values(RootCommands.class));
		// NON_DEBUG_COMMANDS = CommandSet.from(EnumSet.complementOf(EnumSet.of(SPAWN)));
		
		// PRE_POKEMON_COMMANDS = CommandSet.from(EnumSet.of(SPAWN));
		// NON_DEBUG_PRE_POKEMON_COMMANDS = CommandSet.from(EnumSet.complementOf(EnumSet.of(SPAWN)));
	}*/
	
	public static CommandSet getCommandsFor(User user) {/* return getCommandsFor(user, false); }
	public static CommandSet getCommandsFor(User user, boolean ignoreState) {*/
		return user.getId().equals(Core.devId) ? ALL_COMMANDS : NON_DEBUG_COMMANDS;
	}
}
