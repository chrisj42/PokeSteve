package bot.command.group.pokemon;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.data.DataCore;
import bot.data.UserData;
import bot.world.pokemon.Pokemon.CaughtPokemon;
import bot.world.pokemon.move.Move;

import reactor.core.publisher.Mono;

public class PokemonLearnCommand extends ActionableCommand {
	
	public PokemonLearnCommand() {
		super("learn", "Add a move to a pokemon's move set.", "<move name>", "<move to replace>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		requireArgs(2, args);
		
		UserData data = UserData.reqData(context.user);
		
		Move toLearn = ArgType.MOVE.parseArg(args[0]);
		Move toReplace = ArgType.MOVE.parseArg(args[1]);
		
		data.getSelectedPokemon().replaceMove(toLearn, toReplace);
		return context.channel.createMessage("Replaced move "+toReplace.getName()+" with "+toLearn.getName()+".").then();
	}
}
