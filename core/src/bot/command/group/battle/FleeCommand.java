package bot.command.group.battle;

import bot.UserState;
import bot.command.ActionableCommand;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.pokemon.battle.BattleInstance;
import bot.pokemon.battle.UserPlayer;

import reactor.core.publisher.Mono;

public class FleeCommand extends ActionableCommand {
	
	public FleeCommand() {
		super("flee", "leave the battle.");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		UserPlayer player = UserState.leaveBattle(context.user);
		if(player != null)
			return player.getBattle().broadcast(player+" left the battle.");
		return context.channel.createMessage("left the battle.").then();
	}
}
