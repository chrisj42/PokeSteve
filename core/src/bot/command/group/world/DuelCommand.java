package bot.command.group.world;

import bot.Core;
import bot.command.CommandParent;
import bot.data.UserData;
import bot.command.ActionableCommand;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.world.pokemon.battle.PlayerBattle;
import bot.world.pokemon.battle.UserPlayer;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.entity.RestUser;
import reactor.core.publisher.Mono;

public class DuelCommand extends CommandParent {
	
	public DuelCommand() {
		super("duel", "Make and manage duel requests.",
			new DuelRequestCommand(),
			new DuelAcceptCommand(),
			new DuelRejectCommand()
		);
		// super("duel", "Request a battle with another player.", new ArgumentSet("<opponent user id>", "<your pokemon>"), LEVEL_OPT);
	}
	
	public static class DuelRequestCommand extends ActionableCommand {
		
		public DuelRequestCommand() {
			super("request", "Request a duel with another player.", "<opponent user id>");
			// super("duel", "Request a battle with another player.", new ArgumentSet("<opponent user id>", "<your pokemon>"), LEVEL_OPT);
		}
		
		@Override
		protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
			if(args.length < 3)
				throw new ArgumentCountException(3 - args.length);
			
			UserData selfData = UserData.reqData(context.user);
			
			Snowflake opponentId = Snowflake.of(args[0]);
			RestUser opponentUser = Core.client.getUserById(opponentId);
			return opponentUser.getData().flatMap(
				uData -> {
					// int yourLevel = SpawnCommand.DEFAULT_LEVEL;
					// int enemyLevel = SpawnCommand.DEFAULT_LEVEL;
				/*if(options.hasOption(LEVEL_OPT)) {
					yourLevel = options.getOptionValue(LEVEL_OPT, 0, ArgType.INTEGER);
					// enemyLevel = options.getOptionValue(LEVEL_OPT, 1, ArgType.INTEGER);
				}*/
					
					// Pokemon userPokemon = ArgType.POKEMON.parseArg(args[1]).spawnPokemon(yourLevel);
					// Pokemon enemyPokemon = ArgType.POKEMON.parseArg(args[2]).spawnPokemon(enemyLevel);
					User opponent = new User(Core.gateway, uData);
					selfData.requestDuel(opponent, context);
					
					return opponentUser.getPrivateChannel().flatMap(cData -> {
						PrivateChannel channel = new PrivateChannel(Core.gateway, cData);
						return channel.createMessage(context.user.getUsername()+" wants to battle with you! Type `duel accept` to start the battle or `duel reject` to refuse the request.")
							.flatMap(e -> context.channel.createMessage("Requested duel with "+opponent.getUsername()).then());
						// PlayerBattle battle = new PlayerBattle(new UserPlayer(context.channel, context.user, userPokemon), new UserPlayer(channel, opponent, enemyPokemon));
						// return UserState.startBattle(battle);
					});
				}
			);
		}
	}
	
	/*public static class DuelStatusCommand extends ActionableCommand {
		
		public DuelStatusCommand() {
			super("status", "Check if you have a duel pending.");
		}
		
		@Override
		protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
			return null;
		}
	}*/
	
	public static class DuelAcceptCommand extends ActionableCommand {
		
		public DuelAcceptCommand() {
			super("accept", "Accept a duel request.");
		}
		
		@Override
		protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
			UserData data = UserData.reqData(context.user);
			UserPlayer opponent = data.flushDuelRequest();
			
			return context.channel.createMessage("Accepted duel request from "+opponent.user.getUsername()+".")
				.flatMap(msg -> new PlayerBattle(opponent,
					new UserPlayer(context.channel, data, data.getSelectedPokemon())
				).startBattle());
		}
	}
	
	public static class DuelRejectCommand extends ActionableCommand {
		
		public DuelRejectCommand() {
			super("reject", "Reject a duel request.");
		}
		
		@Override
		protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
			UserData data = UserData.reqData(context.user);
			UserPlayer opp = data.flushDuelRequest();
			return context.channel.createMessage("Rejected duel request from "+opp.user.getUsername()+".")
				.flatMap(msg -> opp.user.getPrivateChannel())
				.flatMap(channel -> channel.createMessage(context.user.getUsername()+" rejected your duel request."))
				.then();
		}
	}
}
