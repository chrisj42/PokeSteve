package bot.command.group.battle;

import bot.UserState;
import bot.command.ActionableCommand;
import bot.command.ArgType;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.battle.BattleInstance;
import bot.pokemon.battle.BattleInstance.Player;
import bot.util.UsageException;

import reactor.core.publisher.Mono;

public class AttackCommand extends ActionableCommand {
	
	public AttackCommand() {
		super("attack", "use a move on the opposing pokemon.", "<move number>");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) throws ArgumentCountException {
		if(args.length == 0)
			throw new ArgumentCountException(1);
		
		Player player = UserState.getBattle(context.user);
		
		int id = ArgType.INTEGER.parseArg(args[0]) - 1;
		if(id < 0 || id >= player.pokemon.pokemon.moveset.length)
			throw new UsageException("move id does not exist.");
		
		return player.getBattle().submitAttack(player, id);
	}
}
