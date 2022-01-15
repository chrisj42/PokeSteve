package bot.command.group.pokemon;

import java.util.Map.Entry;
import java.util.NavigableMap;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.data.UserData;
import bot.util.UsageException;
import bot.world.pokemon.Pokemon.CaughtPokemon;

import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

public class PokemonListCommand extends ActionableCommand {
	
	private static final int PAGE_SIZE = 10;
	
	public PokemonListCommand() {
		super("list", "List your caught pokemon.", "[page]");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		UserData data = UserData.reqData(context.user);
		
		int page = 0;
		if(args.length > 0) {
			page = ArgType.INTEGER.parseArg(args[0]);
			if(page <= 0)
				throw new UsageException("page must be greater than zero.");
			page--;
		}
		
		final int offset = page * PAGE_SIZE;
		
		NavigableMap<Integer, CaughtPokemon> tailMap = data.getPokemonFrom(offset);
		if(tailMap.size() == 0)
			throw new UsageException("There are no pokemon on this page! Choose a lower page number.");
		
		StringBuilder str = new StringBuilder();
		Entry<Integer, CaughtPokemon> entry = tailMap.firstEntry();
		for(int i = 0; i < PAGE_SIZE && entry != null; i++, entry = tailMap.higherEntry(entry.getKey())) {
			CaughtPokemon pokemon = entry.getValue();
			if(i > 0) str.append("\n");
			pokemon.buildListEntry(str);
		}
		
		final String list = str.toString();
		return context.channel.createMessage(EmbedCreateSpec.builder()
			.title("Your pokemon")
			.description(list)
			.build()
		).then();
	}
}
