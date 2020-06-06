package bot;

import java.util.HashMap;

import bot.command.Command;
import bot.command.CommandSet;
import bot.command.Commands;
import bot.pokemon.battle.BattleInstance;
import bot.pokemon.battle.BattleInstance.Player;
import bot.pokemon.battle.WildBattle;

import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

// possible states a user could be in. Usually has metadata associated with it. Determines what commands are available.
public enum UserState {
	Idle(Commands.HELP, Commands.DEX, Commands.SPAWN, Commands.BATTLE),
	Travel(Commands.HELP),
	Search(Commands.HELP),
	Battle(Commands.HELP, Commands.ATTACK),
	Trade(Commands.HELP);
	
	public static final UserState[] values = UserState.values();
	
	public final CommandSet commands;
	
	UserState(Command... rootCommands) {
		commands = new CommandSet(rootCommands);
	}
	
	
	private static final HashMap<User, UserState> userStates = new HashMap<>();
	private static final HashMap<User, Player> userBattles = new HashMap<>();
	
	public static UserState getState(User user) {
		return userStates.computeIfAbsent(user, u -> UserState.Idle);
	}
	/*public static void setState(User user, UserState state) {
		userStates.put(user, state);
	}*/
	
	public static Mono<Void> startBattle(WildBattle battle) {
		userBattles.put(battle.player.user, battle.player);
		userStates.put(battle.player.user, UserState.Battle);
		return battle.player.channel.createMessage("Battle Start!")
			.flatMap(e -> battle.onRoundStart());
	}
	
	public static Player getBattle(User user) {
		return userBattles.get(user);
	}
}
