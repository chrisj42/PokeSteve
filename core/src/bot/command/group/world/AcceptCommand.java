package bot.command.group.world;

import bot.UserState;
import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.Pokemon;
import bot.pokemon.battle.PlayerBattle;
import bot.pokemon.battle.UserPlayer;

import reactor.core.publisher.Mono;

import static bot.command.group.world.SpawnCommand.LEVEL_OPT;

public class AcceptCommand extends ActionableCommand {
	
	public AcceptCommand() {
		super("accept", "Accept a duel request.", new ArgumentSet("<pokemon to use>"), LEVEL_OPT);
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length == 0)
			throw new ArgumentCountException(1);
		
		int level = SpawnCommand.DEFAULT_LEVEL;
		if(options.hasOption(LEVEL_OPT))
			level = options.getOptionValue(LEVEL_OPT, ArgType.INTEGER);
		
		Pokemon pokemon = ArgType.POKEMON.parseArg(args[0]).spawnPokemon(level);
		
		UserPlayer opponent = UserState.getRequester(context.user);
		if(opponent == null) {
			UserState.cancelRequest(context.user);
			return context.channel.createMessage("the request is no longer valid.").then();
		}
		if(UserState.getBattle(opponent.user) != null) {
			UserState.cancelRequest(context.user);
			return context.channel.createMessage("the user has already joined another battle.").then();
		}
		
		return UserState.startBattle(new PlayerBattle(opponent, new UserPlayer(context.channel, context.user, pokemon)));
	}
}
