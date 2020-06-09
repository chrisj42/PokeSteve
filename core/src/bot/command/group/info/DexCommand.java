package bot.command.group.info;

import java.text.DecimalFormat;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.DataCore;
import bot.pokemon.PokemonSpecies;
import bot.pokemon.Stat;
import bot.pokemon.external.Importer;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class DexCommand extends ActionableCommand {
	
	public DexCommand() {
		super("dex", "View an entry in the pokedex.", "<pokemon dex number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length < 1)
			throw new ArgumentCountException(1);
		
		PokemonSpecies tempSpecies;
		
		// String msg = "";
		try {
			int dexNumber = Integer.parseInt(args[0]);
			tempSpecies = DataCore.POKEMON.get(dexNumber);
			if(tempSpecies == null)
				throw new UsageException("Could not find a pokemon with that pokedex number. Note that this bot only knows of pokemon up to generation 7, where the national dex ends on #"+Importer.MAX_DEX_NUMBER+".");
		} catch(NumberFormatException e) {
			// species = Importer.getSpecies(args[0]);
			// if(species == null)
			// 	msg = ";
			// throw new UsageException("dex numbers only for now, sorry");
			// species = null;
			tempSpecies = DataCore.POKEMON.get(args[0]);
			if(tempSpecies == null)
				throw new UsageException("Could not find a pokemon with that name. Note that this bot only knows of pokemon up to generation 7, gen 8+ are not yet supported.");
		}
		
		final PokemonSpecies species = tempSpecies;
		DecimalFormat format = new DecimalFormat("000");
		
		return context.channel.createEmbed(em -> em
			.setTitle("#"+species.dex+" - "+species.name)
			.addField("Typing", species.primaryType+(species.secondaryType == null ? "" : " and "+species.secondaryType), false)
			.addField("Base Stats", ""+
				   "Health      - "+species.getBaseStat(Stat.Health)
				+"\nAttack      - "+species.getBaseStat(Stat.Attack)
				+"\nDefense     - "+species.getBaseStat(Stat.Defense)
				+"\nSp. Attack  - "+species.getBaseStat(Stat.SpAttack)
				+"\nSp. Defense - "+species.getBaseStat(Stat.SpDefense)
				+"\nSpeed       - "+species.getBaseStat(Stat.Speed)
				, false)
			.setImage("https://raw.githubusercontent.com/fanzeyi/pokemon.json/master/images/"+format.format(species.dex)+".png")
		).then();
	}
}
