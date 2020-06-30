package bot.command.group.pokemon;

import bot.command.*;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.OptionSet.OptionValues;
import bot.data.UserData;
import bot.util.UsageException;
import bot.world.pokemon.Pokemon.CaughtPokemon;

import reactor.core.publisher.Mono;

import org.jetbrains.annotations.NotNull;

public class PokemonCommand extends CommandParent {
	
	public PokemonCommand() {
		super("pokemon", "Manage your caught pokemon.",
			new PokemonListCommand(),
			new PokemonSelectCommand(),
			new PokemonInfoCommand(),
			new PokemonNicknameCommand(),
			new PokemonLearnCommand()
		);
	}
	
	static final Option SELECT_OPT = new Option("for", 'f', "Specify for which pokemon this command will apply. Defaults to your selected pokemon.", "<catch id>");
	
	@NotNull
	static CaughtPokemon getPokemon(CommandContext context, OptionValues options) {
		UserData data = UserData.reqData(context.user);
		if(!options.hasOption(SELECT_OPT))
			return data.getSelectedPokemon();
		
		int catchId = options.getOptionValue(SELECT_OPT, ArgType.INTEGER);
		CaughtPokemon pokemon = data.getPokemon(catchId);
		if(pokemon == null)
			throw new UsageException("Could not find pokemon with id "+catchId);
		return pokemon;
	}
	
	public static class PokemonInfoCommand extends ActionableCommand {
		
		public PokemonInfoCommand() {
			super("info", "View information about a caught pokemon.", ArgumentSet.NO_ARGS, SELECT_OPT);
		}
		
		@Override
		protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
			final CaughtPokemon pokemon = getPokemon(context, options);
			return context.channel.createEmbed(pokemon::buildEmbed).then();
		}
	}
	
	public static class PokemonSelectCommand extends ActionableCommand {
		
		public PokemonSelectCommand() {
			super("select", "Select a pokemon to use in battles.", "<catch id>");
		}
		
		@Override
		protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
			requireArgs(1, args);
			
			final int catchId = ArgType.INTEGER.parseArg(args[0]);
			
			UserData data = UserData.reqData(context.user);
			if(data.getBattlePlayer() != null)
				throw new UsageException("Cannot select pokemon during a battle.");
			
			CaughtPokemon pokemon = data.selectPokemon(catchId);
			
			return context.channel.createMessage("You selected your level "+pokemon.getLevel()+" "+pokemon.species.name+(pokemon.hasNickname() ? ", "+pokemon.getName() : "")+".").then();
		}
	}
}
