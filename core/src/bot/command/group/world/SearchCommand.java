package bot.command.group.world;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.Option;
import bot.command.OptionSet.OptionValues;
import bot.data.UserData;
import bot.util.Utils;
import bot.world.pokemon.Pokemon;
import bot.world.pokemon.PokemonSpecies;
import bot.world.pokemon.battle.BattleInstance;
import bot.world.pokemon.battle.UserPlayer;
import bot.world.pokemon.battle.WildBattle;

import reactor.core.publisher.Mono;

public class SearchCommand extends ActionableCommand {
	
	// TODO once I have location data set up, use that instead of choosing the pokemon
	
	private static final Option LEVEL_OPT = new Option("level", 'l', "select pokemon level", "<level>");
	
	public SearchCommand() {
		super("search", "Search around the area for a wild pokemon.", ArgumentSet.get("<wild pokemon>"), LEVEL_OPT);
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		requireArgs(1, args);
		
		UserData data = UserData.reqData(context.user);
		
		PokemonSpecies species = ArgType.POKEMON.parseArg(args[0]);
		
		int level;
		if(options.hasOption(LEVEL_OPT))
			level = options.getOptionValue(LEVEL_OPT, ArgType.INTEGER);
		else {
			int centralLevel = data.getSelectedPokemon().getLevel();
			int offset = centralLevel / 5;
			level = centralLevel + Utils.randInt(-offset, offset);
		}
		
		Pokemon wildPokemon = species.spawnPokemon(level);
		
		return context.channel.createMessage("A wild "+wildPokemon.species+" appeared!").flatMap(
			e -> new WildBattle(new UserPlayer(context.channel, data, data.getSelectedPokemon()), wildPokemon)
				.startBattle()
		);
	}
}
