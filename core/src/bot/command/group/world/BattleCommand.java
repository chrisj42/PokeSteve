package bot.command.group.world;

import bot.UserState;
import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.DataCore;
import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;
import bot.pokemon.battle.UserPlayer;
import bot.pokemon.battle.WildBattle;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class BattleCommand extends ActionableCommand {
	
	public BattleCommand() {
		super("battle", "(debug command) spawn a wild pokemon and start a battle with it.", "<your pokemon id> <wild pokemon id>");
		// super("spawn", "(debug command) spawn a wild pokemon and list all its characteristics.", "<pokemon id>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length < 2)
			throw new ArgumentCountException(2 - args.length);
		
		Pokemon userPokemon = getPokemon(args[0]);
		Pokemon wildPokemon = getPokemon(args[1]);
		
		WildBattle battle = new WildBattle(new UserPlayer(context.channel, context.user, userPokemon), wildPokemon);
		return UserState.startBattle(battle);
	}
	
	private static Pokemon getPokemon(String idString) {
		int id = ArgType.INTEGER.parseArg(idString);
		PokemonSpecies species = DataCore.POKEMON.get(id);
		if(species == null)
			throw new UsageException("no matching pokemon exists.");
		
		return species.spawnPokemon();
	}
}
