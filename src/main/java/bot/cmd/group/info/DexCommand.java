package bot.cmd.group.info;

import java.util.List;

import bot.cmd.ActionableCommand;
import bot.cmd.CommandContext;
import bot.cmd.Option.OptionValues;

import reactor.core.publisher.Mono;

public class DexCommand extends ActionableCommand {
	
	public DexCommand() {
		super("dex", "View an entry in the pokedex.", "<dex number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, List<String> args) {
		String msg;
		if(args.size() == 0)
			msg = "Provide a pokedex number to see information about that pokemon.";
		else
			msg = "You want to see pokemon #"+args.get(0)+". Info coming soon!";
		
		return context.channel.createMessage(msg).then();
	}
}
