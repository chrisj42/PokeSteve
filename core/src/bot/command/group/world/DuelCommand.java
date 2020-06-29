package bot.command.group.world;

import bot.Core;
import bot.command.CommandParent;
import bot.data.UserData;
import bot.command.ActionableCommand;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.util.UsageException;
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
			new DuelRejectCommand(),
			new DuelCancelCommand()
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
			if(args.length == 0)
				throw new ArgumentCountException(1);
			
			UserData selfData = UserData.reqData(context.user);
			
			Snowflake opponentId = Snowflake.of(args[0]);
			RestUser restOpponent = Core.client.getUserById(opponentId);
			return restOpponent.getData().flatMap(uData -> {
				User opponent = new User(Core.gateway, uData);
				selfData.requestDuel(opponent, context);
				
				return opponent.getPrivateChannel()
					.flatMap(channel -> channel.createMessage(context.user.getUsername()+" wants to battle with you! Type `duel accept` to start the battle or `duel reject` to refuse the request.")
					)
					.flatMap(msg -> context.channel.createMessage("Sent "+opponent.getUsername()+" a duel request."))
					.then();
			});
		}
	}
	
	public static class DuelAcceptCommand extends ActionableCommand {
		
		public DuelAcceptCommand() {
			super("accept", "Accept a duel request.");
		}
		
		@Override
		protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
			UserData data = UserData.reqData(context.user);
			if(data.getBattlePlayer() != null)
				throw new UsageException("Cannot accept duel requests during a battle.");
			
			UserPlayer opponent = data.clearIncomingRequest();
			return context.channel.createMessage("Accepted duel request from "+opponent.user.getUsername()+".")
				.flatMap(msg -> opponent.user.getPrivateChannel())
				.flatMap(channel -> channel.createMessage(context.user.getUsername()+" accepted your duel request."))
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
			UserPlayer opponent = data.clearIncomingRequest();
			return context.channel.createMessage("Rejected duel request from "+opponent.user.getUsername()+".")
				.flatMap(msg -> opponent.user.getPrivateChannel())
				.flatMap(channel -> channel.createMessage(context.user.getUsername()+" rejected your duel request."))
				.then();
		}
	}
	
	public static class DuelCancelCommand extends ActionableCommand {
		
		public DuelCancelCommand() {
			super("cancel", "Cancel a duel request you recently made.");
		}
		
		@Override
		protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
			UserData data = UserData.reqData(context.user);
			UserData opponentData = data.clearOutgoingRequest();
			return context.channel.createMessage("Cancelled duel request to "+opponentData.getUser().getUsername()+".")
				.flatMap(msg -> opponentData.getUser().getPrivateChannel())
				.flatMap(channel -> channel.createMessage(context.user.getUsername()+" cancelled their duel request."))
				.then();
		}
	}
}
