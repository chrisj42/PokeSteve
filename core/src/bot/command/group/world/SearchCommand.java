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
	public SearchCommand() {
		super("search", "Search around the area for a wild pokemon.", "<wild pokemon>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		requireArgs(1, args);
		
		UserData data = UserData.reqData(context.user);
		
		PokemonSpecies species = ArgType.POKEMON.parseArg(args[0]);
		
		// if(Utils.randInt(0, 1) != 0)
		// 	return context.channel.createMessage("You couldn't find anything.").then();
		
		Pokemon wildPokemon = species.spawnPokemon(data.getSelectedPokemon().getLevel());
		
		return context.channel.createMessage("A wild "+wildPokemon.species+" appeared!").flatMap(
			e -> new WildBattle(new UserPlayer(context.channel, data, data.getSelectedPokemon()), wildPokemon)
				.startBattle()
		);
	}
}
