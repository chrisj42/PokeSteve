package bot.command.group.world;

import bot.Core;
import bot.UserState;
import bot.command.ActionableCommand;
import bot.command.ArgType;
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

public class DuelCommand extends ActionableCommand {
	
	public DuelCommand() {
		super("duel", "start a battle with another player.", "<opponent user id>", "<your pokemon>", "<opponent pokemon>");
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
				Pokemon userPokemon = ArgType.POKEMON.parseArg(args[1]).spawnPokemon();
				Pokemon enemyPokemon = ArgType.POKEMON.parseArg(args[2]).spawnPokemon();
				User opponent = new User(Core.gateway, uData);
				if(UserState.getBattle(opponent) != null)
					throw new UsageException("user is already in a battle.");
				return user.getPrivateChannel().flatMap(cData -> {
					PrivateChannel channel = new PrivateChannel(Core.gateway, cData);
					PlayerBattle battle = new PlayerBattle(new UserPlayer(context.channel, context.user, userPokemon), new UserPlayer(channel, opponent, enemyPokemon));
					return UserState.startBattle(battle);
				});
			}
		);
	}
}
