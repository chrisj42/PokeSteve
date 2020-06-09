package bot.command.group.world;

import bot.UserState;
import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.Option;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.DataCore;
import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;
import bot.pokemon.battle.UserPlayer;
import bot.pokemon.battle.WildBattle;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class BattleCommand extends ActionableCommand {
	
	static final Option LEVELS_OPT = new Option("levels", 'l', "select levels of the two pokemon.", "<your pokemon level>", "<wild pokemon level>");
	public BattleCommand() {
		super("battle", "(debug command) spawn a wild pokemon and start a battle with it.", new ArgumentSet("<your pokemon>", "<wild pokemon>"), LEVELS_OPT);
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length < 2)
			throw new ArgumentCountException(2 - args.length);
		
		int yourLevel = SpawnCommand.DEFAULT_LEVEL;
		int enemyLevel = SpawnCommand.DEFAULT_LEVEL;
		if(options.hasOption(LEVELS_OPT)) {
			yourLevel = options.getOptionValue(LEVELS_OPT, 0, ArgType.INTEGER);
			enemyLevel = options.getOptionValue(LEVELS_OPT, 1, ArgType.INTEGER);
		}
		
		Pokemon userPokemon = ArgType.POKEMON.parseArg(args[0]).spawnPokemon(yourLevel);
		Pokemon wildPokemon = ArgType.POKEMON.parseArg(args[1]).spawnPokemon(enemyLevel);
		
		WildBattle battle = new WildBattle(new UserPlayer(context.channel, context.user, userPokemon), wildPokemon);
		return UserState.startBattle(battle);
	}
}
