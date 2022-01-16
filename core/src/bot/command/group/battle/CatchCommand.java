package bot.command.group.battle;

import java.time.Duration;

import bot.Core;
import bot.command.ActionableCommand;
import bot.command.CommandContext;
import bot.command.OptionSet.OptionValues;
import bot.data.UserData;
import bot.util.UsageException;
import bot.world.pokemon.Pokemon.CaughtPokemon;
import bot.world.pokemon.Stat;
import bot.world.pokemon.battle.BattleInstance.Player;
import bot.world.pokemon.battle.BattlePokemon;
import bot.world.pokemon.battle.Flag;
import bot.world.pokemon.battle.PlayerBattle;
import bot.world.pokemon.battle.WildBattle;
import bot.world.pokemon.battle.status.StatusAilment;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class CatchCommand extends ActionableCommand {
	
	// private static final float BASE_CHANCE = 0.05f;
	// private static final float CHANCE_PER_HEALTH = (1-BASE_CHANCE);
	
	public CatchCommand() {
		super("catch", "Attempt to catch a wild pokemon.");
	}
	
	@Override
	protected Mono<Void> execute(CommandContext context, OptionValues options, String[] args) {
		// attempt to catch
		final CatchEventData catchEvent = new CatchEventData(context);
		
		return catchEvent.doCatchEvent();
	}
	
	private static class CatchEventData {
		private static final Color INITIAL_CATCH_EMBED_COLOR = Color.DARK_GOLDENROD;
		private static final Color FAILED_CATCH_EMBED_COLOR = Color.CINNABAR;
		private static final Color SUCCESSFUL_CATCH_EMBED_COLOR = Color.SEA_GREEN;
		
		private static final Duration BREAKOUT_INTERVAL = Duration.ofMillis(2000);
		private static final String[] BREAKOUT_MESSAGES = {
			"Oh well...", // breakout on first shake
			"Aw man...", // breakout on second shake
			"Almost had it too!", // breakout on third shake
			"It was so close too...", // breakout right before capture
		};
		private static final String[] SHAKE_MESSAGES = {
			"It shook once...", // on first shake
			"It shook twice...", // on second shake
			"It shook a third time..!", // on third shake
			" was caught!" // on successful capture
		};
		private static final int BREAKOUT_ATTEMPTS = BREAKOUT_MESSAGES.length;
		
		private static String getMessage(Player name, int attempt, boolean breakout) {
			if(breakout)
				return "Oh no! The "+name+" broke free! " + BREAKOUT_MESSAGES[attempt];
			// no breakout
			if(attempt == SHAKE_MESSAGES.length - 1)
				// capture
				return name+SHAKE_MESSAGES[attempt];
			// shake
			return SHAKE_MESSAGES[attempt];
		}
		
		private final CommandContext context;
		private final UserData userData;
		private final WildBattle battle;
		private final BattlePokemon wildPokemon;
		private final boolean canBreakout;
		private final float breakoutChance;
		private int breakoutAttempts;
		
		private String cumulativeStatus;
		private Color embedColor = INITIAL_CATCH_EMBED_COLOR;
		
		CatchEventData(CommandContext context) {
			this.context = context;
			
			UserData data = UserData.reqData(context.user);
			Player userPlayer = data.getBattlePlayer();
			if(userPlayer == null)
				throw new UsageException("You are not in a battle.");
			if(userPlayer.getBattle() instanceof PlayerBattle)
				throw new UsageException("You can't catch another trainer's pokemon!");
			
			this.userData = data;
			this.battle = (WildBattle) userPlayer.getBattle(); 
			// this.wildPokemonPlayer = battle.wildPokemon;
			this.wildPokemon = battle.wildPokemon.pokemon;
			
			final float captureChance = getCaptureChance(wildPokemon);
			canBreakout = canBreakout(captureChance);
			breakoutChance = getBreakoutChance(captureChance);
			breakoutAttempts = 0;
		}
		
		private EmbedCreateSpec createEmbed() {
			return EmbedCreateSpec.builder()
				.title("You threw a pokeball!")
				.color(embedColor)
				.description(cumulativeStatus)
				.build();
		}
		
		Mono<Void> doCatchEvent() {
			Core.setUserWaiting(userData.userId, true);
			cumulativeStatus = battle.wildPokemon+" was sucked inside...";
			
			return context.channel.createMessage(createEmbed())
				.delayElement(BREAKOUT_INTERVAL)
				.flatMap(this::onBreakoutAttempt)
				.then();
		}
		
		private Mono<Message> onBreakoutAttempt(Message msg) {
			// check for breakout
			final boolean breakout = canBreakout && tryBreakout(breakoutChance);
			
			// fetch status message
			final String message = getMessage(battle.wildPokemon, breakoutAttempts, breakout);
			cumulativeStatus += '\n'+message;
			
			if(breakout) {
				embedColor = FAILED_CATCH_EMBED_COLOR;
				return msg
					.edit().withEmbeds(createEmbed())
					.delayElement(BREAKOUT_INTERVAL)
					.flatMap(m -> {
						battle.player.pokemon.setFlag(Flag.FAILED_CATCH);
						Core.setUserWaiting(userData.userId, false);
						return battle.submitAttack(battle.player, -1)
							.thenReturn(m);
					});
			} else {
				// no breakout
				breakoutAttempts++;
				if(breakoutAttempts >= BREAKOUT_ATTEMPTS) {
					// capture
					embedColor = SUCCESSFUL_CATCH_EMBED_COLOR;
					return msg
						.edit().withEmbeds(createEmbed())
						.flatMap(m -> {
							// register the caught pokemon
							CaughtPokemon caught = userData.addPokemon(wildPokemon.pokemon);
							Core.setUserWaiting(userData.userId, false);
							return battle.onPokemonCaught()
								.delayElement(BREAKOUT_INTERVAL.dividedBy(2))
								.then(Mono.defer(() ->
									context.channel.createMessage(caught.buildEmbed().build())
								))
								.thenReturn(m);
						});
				}
				
				// shake
				return msg
					.edit().withEmbeds(createEmbed())
					.delayElement(BREAKOUT_INTERVAL)
					.flatMap(this::onBreakoutAttempt);
			}
		}
		
		// follows 3rd/4th gen capture formula
		private static float getCaptureChance(BattlePokemon pokemon) {
			final int maxHealth = pokemon.pokemon.getStat(Stat.Health);
			final int curHealth = pokemon.getHealth();
			final int catchRate = pokemon.pokemon.species.catchRate;
			StatusAilment ailment = pokemon.getFlag(Flag.STATUS_EFFECT);
			final float statusModifier = ailment == null ? 1 : ailment.catchModifier;
			
			return (3 * maxHealth - 2 * curHealth) * catchRate * statusModifier / (3 * maxHealth) / 255;
		}
		
		private static boolean canBreakout(float captureChance) {
			return captureChance < 1;
		}
		
		private static float getBreakoutChance(float captureChance) {
			return (float) Math.pow(captureChance, .25);
		}
		
		private static boolean tryBreakout(float breakoutChance) {
			return Math.random() >= breakoutChance;
		}
	}
}
