package bot.command.group.info;

import bot.command.ActionableCommand;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.DataCore;
import bot.pokemon.move.Move;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class MoveCommand extends ActionableCommand {
	
	public MoveCommand() {
		super("move", "View information about a move.", "<move name>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length < 1)
			throw new ArgumentCountException(1);
		
		Move move = DataCore.MOVES.get(args[0]);
		if(move == null)
			throw new UsageException("No move with that name could be found.");
		
		return context.channel.createMessage("Move "+move.id+", "+move).then();
	}
}
