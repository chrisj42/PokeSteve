package bot;

import java.util.HashMap;

import bot.command.Command;
import bot.command.CommandSet;
import bot.command.Commands;
import bot.pokemon.battle.BattleInstance;
import bot.pokemon.battle.BattleInstance.Player;
import bot.pokemon.battle.UserPlayer;
import bot.pokemon.battle.WildBattle;

import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

// possible states a user could be in. Usually has metadata associated with it. Determines what commands are available.
public enum UserState {
	Idle(Commands.HELP, Commands.DEX, Commands.MOVE, Commands.SPAWN, Commands.BATTLE, Commands.DUEL),
	Travel(Commands.HELP),
	Search(Commands.HELP),
	Battle(Commands.HELP, Commands.ATTACK, Commands.FLEE),
	Trade(Commands.HELP);
	
	public static final UserState[] values = UserState.values();
	
	public final CommandSet commands;
	
	UserState(Command... rootCommands) {
		commands = new CommandSet(rootCommands);
	}
	
	
	private static final HashMap<User, UserState> userStates = new HashMap<>();
	private static final HashMap<User, UserPlayer> userBattles = new HashMap<>();
	
	public static UserState getState(User user) {
		return userStates.computeIfAbsent(user, u -> UserState.Idle);
	}
	/*public static void setState(User user, UserState state) {
		userStates.put(user, state);
	}*/
	
	public static Mono<Void> startBattle(BattleInstance battle) {
		return battle.userFlux().flatMap(player -> {
			userBattles.put(player.user, player);
			userStates.put(player.user, UserState.Battle);
			return player.user.getPrivateChannel();
		}).flatMap(channel -> channel.createMessage("Battle Start!"))
			.then(Mono.just(true).flatMap(e -> battle.onRoundStart()));
	}
	
	public static Player getBattle(User user) {
		return userBattles.get(user);
	}
	
	public static UserPlayer leaveBattle(User user) { return leaveBattle(user, true); }
	public static UserPlayer leaveBattle(User user, boolean checkOther) {
		UserPlayer player = userBattles.remove(user);
		if(checkOther && player != null && player.getOpponent() instanceof UserPlayer)
			leaveBattle(((UserPlayer)player.getOpponent()).user, false);
		userStates.put(user, UserState.Idle);
		return player;
	}
}
