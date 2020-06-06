package bot.command.group.info;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.DataCore;
import bot.pokemon.PokemonSpecies;
import bot.pokemon.external.Importer;

import reactor.core.publisher.Mono;

public class DexCommand extends ActionableCommand {
	
	public DexCommand() {
		super("dex", "View an entry in the pokedex.", "<pokemon dex number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length < 1)
			throw new ArgumentCountException(1);
		
		PokemonSpecies species;
		
		String msg = "";
		try {
			int dexNumber = ArgType.INTEGER.parseArg(args[0]);
			species = DataCore.POKEMON.get(dexNumber);
			if(species == null)
				msg = "Could not find a pokemon with that pokedex number. Note that this bot only knows of pokemon up to generation 7, where the national dex ends on #"+Importer.MAX_DEX_NUMBER+".";
		} catch(NumberFormatException e) {
			// species = Importer.getSpecies(args[0]);
			// if(species == null)
			// 	msg = "Could not find a pokemon with that name. Note that this bot only knows of pokemon up to generation 7, gen 8+ are not yet supported.";
			msg = "dex numbers only for now, sorry";
			species = null;
		}
		
		if(species != null)
			msg = species.name+", #"+species.dex;
		
		return context.channel.createMessage(msg).then();
	}
}
