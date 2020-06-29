package bot.command.group.info;

import java.text.DecimalFormat;

import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.world.pokemon.PokemonSpecies;
import bot.world.pokemon.Stat;

import reactor.core.publisher.Mono;

public class DexCommand extends ActionableCommand {
	
	public DexCommand() {
		super("dex", "View an entry in the pokedex.", "<pokemon name or dex number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		requireArgs(1, args);
		
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
			.setImage(species.getSpritePath())
		).then();
	}
}
