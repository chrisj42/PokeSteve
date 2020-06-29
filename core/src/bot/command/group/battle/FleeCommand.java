package bot.command.group.battle;

import bot.data.UserData;
import bot.command.ActionableCommand;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.util.UsageException;
import bot.world.pokemon.battle.UserPlayer;

import reactor.core.publisher.Mono;

public class FleeCommand extends ActionableCommand {
	
	public FleeCommand() {
		super("flee", "leave the battle.");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		UserPlayer player = UserData.reqData(context.user).getBattlePlayer();
		if(player == null)
			throw new UsageException("You are not currently in a battle.");
		return player.getBattle().forfeit(player);
	}
}
