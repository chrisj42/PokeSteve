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
	
	private static final DecimalFormat format = new DecimalFormat("000");
	
	public DexCommand() {
		super("dex", "View an entry in the pokedex.", "<pokemon name or dex number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length < 1)
			throw new ArgumentCountException(1);
		
		PokemonSpecies species = ArgType.POKEMON.parseArg(args[0]);
		
		return context.channel.createEmbed(em -> em
			.setTitle("#"+species.dex+" - "+species)
			.setFooter("#"+species.dex+" - "+species, null)
			.addField("Typing", species.primaryType+(species.secondaryType == null ? "" : " and "+species.secondaryType), false)
			.addField("Base Stats", ""+
				   "Health      - "+species.getBaseStat(Stat.Health)
				+"\nAttack      - "+species.getBaseStat(Stat.Attack)
				+"\nDefense     - "+species.getBaseStat(Stat.Defense)
				+"\nSp. Attack  - "+species.getBaseStat(Stat.SpAttack)
				+"\nSp. Defense - "+species.getBaseStat(Stat.SpDefense)
				+"\nSpeed       - "+species.getBaseStat(Stat.Speed)
				, false)
			.setImage("https://raw.githubusercontent.com/chrisj42/PokeSteve/master/resources/sprites/"+format.format(species.dex)+".png")
		).then();
	}
}
