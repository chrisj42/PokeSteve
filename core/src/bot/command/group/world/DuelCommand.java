package bot.command.group.world;

import bot.Core;
import bot.UserState;
import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.DataCore;
import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;
import bot.pokemon.battle.PlayerBattle;
import bot.pokemon.battle.UserPlayer;
import bot.pokemon.battle.WildBattle;
import bot.util.UsageException;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.entity.RestUser;
import reactor.core.publisher.Mono;

import static bot.command.group.world.BattleCommand.LEVELS_OPT;
import static bot.command.group.world.SpawnCommand.LEVEL_OPT;

public class DuelCommand extends ActionableCommand {
	
	public DuelCommand() {
		super("duel", "Request a battle with another player.", new ArgumentSet("<opponent user id>", "<your pokemon>"), LEVEL_OPT);
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length < 3)
			throw new ArgumentCountException(3 - args.length);
		
		Snowflake userId = Snowflake.of(args[0]);
		if(!Core.MEMBERS.contains(userId))
			throw new UsageException("To prevent accidents, only members of the Friend Lounge can be dueled.");
		
		RestUser user = Core.client.getUserById(userId);
		return user.getData().flatMap(
			uData -> {
				int yourLevel = SpawnCommand.DEFAULT_LEVEL;
				// int enemyLevel = SpawnCommand.DEFAULT_LEVEL;
				if(options.hasOption(LEVEL_OPT)) {
					yourLevel = options.getOptionValue(LEVEL_OPT, 0, ArgType.INTEGER);
					// enemyLevel = options.getOptionValue(LEVEL_OPT, 1, ArgType.INTEGER);
				}
				
				Pokemon userPokemon = ArgType.POKEMON.parseArg(args[1]).spawnPokemon(yourLevel);
				// Pokemon enemyPokemon = ArgType.POKEMON.parseArg(args[2]).spawnPokemon(enemyLevel);
				User opponent = new User(Core.gateway, uData);
				
				if(UserState.getBattle(opponent) != null)
					throw new UsageException("user is already in a battle.");
				
				UserState.requestBattle(opponent, new UserPlayer(context.channel, context.user, userPokemon));
				
				return user.getPrivateChannel().flatMap(cData -> {
					PrivateChannel channel = new PrivateChannel(Core.gateway, cData);
					return channel.createMessage(context.user.getUsername()+" wants to battle with you! Use the `accept` or `reject` commands to respond to the request.")
						.flatMap(e -> context.channel.createMessage("requested battle with "+opponent.getUsername()).then());
					// PlayerBattle battle = new PlayerBattle(new UserPlayer(context.channel, context.user, userPokemon), new UserPlayer(channel, opponent, enemyPokemon));
					// return UserState.startBattle(battle);
				});
			}
		);
	}
}
