package bot.command.group.info;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.external.Importer;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies;

public class DexCommand extends ActionableCommand {
	
	public DexCommand() {
		super("dex", "View an entry in the pokedex.", "<pokemon name or dex number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		PokemonSpecies species;
		
		String msg = "";
		try {
			int dexNumber = ArgType.INTEGER.parseArg(args[0]);
			species = Importer.getSpecies(dexNumber);
			if(species == null)
				msg = "Could not find a pokemon with that pokedex number. Note that this bot only knows of pokemon up to generation 7, where the national dex ends on #"+Importer.MAX_DEX_NUMBER+".";
		} catch(NumberFormatException e) {
			species = Importer.getSpecies(args[0]);
			if(species == null)
				msg = "Could not find a pokemon with that name. Note that this bot only knows of pokemon up to generation 7, gen 8+ are not yet supported.";
		}
		
		if(species != null)
			msg = species.getName()+", #"+species.getId();
		
		return context.channel.createMessage(msg).then();
	}
}
