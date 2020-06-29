package bot.command.group.pokemon;

import bot.command.ActionableCommand;
import bot.command.ArgumentSet;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.data.UserData;
import bot.world.pokemon.Pokemon.CaughtPokemon;

import reactor.core.publisher.Mono;

public class PokemonNicknameCommand extends ActionableCommand {
	
	public PokemonNicknameCommand() {
		super("nickname", "Give your pokemon a nickname. Use \"\" to reset the nickname.", ArgumentSet.get("\"nickname\""), PokemonCommand.SELECT_OPT);
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		requireArgs(1, args);
		
		CaughtPokemon pokemon = PokemonCommand.getPokemon(context, options);
		
		final String prevName = pokemon.getName();
		boolean remove = args[0].length() == 0;
		if(remove)
			pokemon.setNickname(null);
		else
			pokemon.setNickname(args[0]);
		
		UserData.reqData(context.user).save();
		return context.channel.createMessage(prevName+" will now be known as "+pokemon.getName()+".")
			.then();
	}
}
