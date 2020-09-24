package bot.command.group.battle;

import bot.command.ActionableCommand;
import bot.command.ArgumentSet.ArgumentCountException;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.data.UserData;
import bot.util.UsageException;
import bot.util.Utils;
import bot.world.pokemon.Stat;
import bot.world.pokemon.battle.BattleInstance.Player;
import bot.world.pokemon.battle.Flag;
import bot.world.pokemon.battle.PlayerBattle;
import bot.world.pokemon.battle.WildBattle;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CatchCommand extends ActionableCommand {
	
	private static final float BASE_CHANCE = 0.05f;
	private static final float CHANCE_PER_HEALTH = (1-BASE_CHANCE);
	
	public CatchCommand() {
		super("catch", "Attempt to catch a wild pokemon.");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		Player player = UserData.reqData(context.user).getBattlePlayer();
		if(player == null)
			throw new UsageException("You are not in a battle.");
		if(player.getBattle() instanceof PlayerBattle)
			throw new UsageException("You can't catch another trainer's pokemon!");
		
		// attempt to catch
		float healthPercent = player.getOpponent().pokemon.getHealth() / (float) player.getOpponent().pokemon.pokemon.getStat(Stat.Health);
		
		float chance = ((1 - healthPercent) * CHANCE_PER_HEALTH) + BASE_CHANCE;
		// System.out.println("chance of catching with "+healthPercent+" health: "+chance);
		if(Math.random() < chance) {
			// caught
			UserData.reqData(context.user).addPokemon(player.getOpponent().pokemon.pokemon);
			
			return context.channel.createEmbed(e -> e
				.setTitle("You threw a pokeball!")
				.setDescription("You caught a "+player.getOpponent().pokemon.pokemon.getName()+"!")
			).then(
				((WildBattle)player.getBattle()).onPokemonCaught()
			);
		} else {
			// forfeit turn
			return context.channel.createEmbed(e -> e
				.setTitle("You threw a pokeball!")
				.setDescription("But you couldn't catch it...")
			).then(
				Mono.defer(() -> {
					player.pokemon.setFlag(Flag.FAILED_CATCH);
					return player.getBattle().submitAttack(player, -1);
				})
			);
		}
	}
}
