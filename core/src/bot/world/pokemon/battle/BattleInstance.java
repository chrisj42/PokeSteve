package bot.world.pokemon.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import bot.world.pokemon.move.Move;
import bot.world.pokemon.Pokemon;
import bot.world.pokemon.Stat;
import bot.util.UsageException;
import bot.util.Utils;
import bot.world.pokemon.move.Moves;

import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.EmbedFieldData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.jetbrains.annotations.Nullable;

public abstract class BattleInstance {
	
	// tracks the participating players in the battle, their pokemon, and the DM channels of the participating users
	
	private boolean running = true;
	final Player player1;
	final Player player2;
	
	public BattleInstance(Player player1, Player player2) {
		this.player1 = player1;
		this.player2 = player2;
		player1.opponent = player2;
		player2.opponent = player1;
		player1.battle = this;
		player2.battle = this;
	}
	
	public Flux<UserPlayer> userFlux() {
		return Flux.just(player1, player2)
			.filter(UserPlayer.class::isInstance)
			.map(UserPlayer.class::cast);
	}
	
	public Mono<Void> broadcastMessage(String message) { return broadcastMessage(p -> message); }
	public Mono<Void> broadcastMessage(Function<UserPlayer, String> messageFetcher) {
		return broadcastImpl(player -> player.channel.createMessage(messageFetcher.apply(player)));
	}
	public Mono<Void> broadcastMessage(Consumer<MessageCreateSpec> messageMaker) {
		return broadcastImpl(p -> p.channel.createMessage(messageMaker));
	}
	public Mono<Void> broadcastEmbed(Consumer<EmbedCreateSpec> embed) { return broadcastEmbed((p, e) -> embed.accept(e)); }
	public Mono<Void> broadcastEmbed(BiConsumer<UserPlayer, EmbedCreateSpec> embedFetcher) {
		return broadcastImpl(player -> player.channel.createEmbed(e -> embedFetcher.accept(player, e)));
	}
	private <T> Mono<Void> broadcastImpl(Function<UserPlayer, Mono<T>> messageSender) {
		return userFlux().flatMap(messageSender).then();
	}
	
	public Mono<Void> startBattle() {
		return userFlux().flatMap(player -> {
			player.data.setBattlePlayer(player);
			return player.user.getPrivateChannel();
		}).flatMap(channel -> channel.createMessage("Battle Start!"))
			.then(Mono.just(true).flatMap(e -> onRoundStart()));
	}
	
	public Mono<Void> onRoundStart() {
		// send appropriate messages to all active players
		return broadcastEmbed((player, e) -> {
			e.setTitle("Next Round");
			Player opponent = ((Player)player).opponent;
			
			e.addField("Opponent", opponent.getDescriptor()+"\n"+opponent.getHealthDisplay(), false);
			e.addField("Your Pokemon", player.getDescriptor()+"\n"+player.getHealthDisplay(), false);
			
			// here we need to check if the user is capable of choosing a move this turn; there are a number of things that could prevent the user from doing so.
			
			StringBuilder str = new StringBuilder();
			List<Integer> availableMoves = player.pokemon.getAvailableMoves();
			boolean canSelect = availableMoves.size() >= 2;
			if(!canSelect) {
				int moveid = availableMoves.size() == 1 ? availableMoves.get(0) : -1;
				Move move = moveid < 0 ? null : player.pokemon.pokemon.getMove(moveid);
				if(move != null) {
					if(player.pokemon.hasFlag(Flag.FORCED_MOVE)) // something forced this one-move-only choice
						str.append("__\"").append(move).append("\" has been auto-selected.__");
					else // having only one move was just bad luck
						canSelect = true; // let the player select, even if there's only one choice
					// 	str.append("\n__\"").append(move).append("\" is the only available move and has been selected automatically.__");
				}
				else if(player.pokemon.hasFlag(Flag.REST_MESSAGE))
					str.append("__").append(player.pokemon.getFlag(Flag.REST_MESSAGE)).append("__");
				else // if there's no move and no message, then it's the struggle message
					str.append("__You don't have any available moves. Struggle will be used.__");
			}
			
			if(canSelect) {
				str.append("Select your move with the `attack <move number>` command. Available moves:");
				for(int i = 0; i < player.pokemon.pokemon.getMoveCount(); i++) {
					boolean available = availableMoves.contains(i);
					final Move move = player.pokemon.pokemon.getMove(i);
					str.append("\n");
					if(!available) str.append("~~");
					str.append(i + 1).append(". ");
					str.append(move);
					if(!available) str.append("~~");
					str.append(" - PP: ").append(player.pokemon.getPp(i)).append("/").append(move.pp);
				}
			}
			
			e.addField("Move Selection", str.toString(), false);
			
		}).thenMany(Flux.just(player1, player2)).flatMap(player -> {
			List<Integer> availableMoves = player.pokemon.getAvailableMoves();
			if(availableMoves.size() == 0) // no available moves
				return submitAttack(player, -1);
			if(availableMoves.size() == 1 && player.pokemon.hasFlag(Flag.FORCED_MOVE)) // only one available move
				return submitAttack(player, availableMoves.get(0));
			return Mono.empty();
		}).then();
	}
	
	public Mono<Void> submitAttack(Player player, int moveIdx) {/* return submitAttack(player, moveIdx, true); }
	Mono<Void> submitAttack(Player player, int moveIdx, boolean validate) {*/
		if(player.moveIdx >= 0)
			throw new UsageException(player+", you've already selected your move.");
		
		if(moveIdx >= 0) {
			if(player.pokemon.getPp(moveIdx) <= 0)
				throw new UsageException("move " + player.pokemon.pokemon.getMove(moveIdx) + " is out of PP.");
			
			// check disabled
			if(player.pokemon.hasFlag(Flag.DISABLED_MOVE)) {
				int moveid = player.pokemon.getFlag(Flag.DISABLED_MOVE);
				if(moveid == moveIdx)
					throw new UsageException("move " + player.pokemon.pokemon.getMove(moveIdx) + " is disabled.");
			}
		}
		
		player.moveIdx = moveIdx;
		player.ready = true;
		
		return broadcastMessage(player+" is ready.").then(
			Mono.defer(() -> {
				if(player.opponent.ready)
					return doRound()
						.filter(Boolean::booleanValue)
						.flatMap(e -> onRoundStart());
				else {
					if(player instanceof UserPlayer)
						return ((UserPlayer)player).channel.createMessage("Waiting on "+player.opponent).then();
					return Mono.empty();
				}
			})
		);
	}
	
	// both pokemon have selected their moves
	// TODO later this will return a RichEmbed
	private Mono<Boolean> doRound() {
		@Nullable final Move move1 = player1.getMove();
		@Nullable final Move move2 = player2.getMove();
		final int priority1 = move1 != null ? move1.priority : 0;
		final int priority2 = move2 != null ? move2.priority : 0;
		final int speed1 = player1.pokemon.getSpeed();
		final int speed2 = player2.pokemon.getSpeed();
		
		final Player first;
		if(priority1 != priority2) {
			if(priority1 > priority2)
				first = player1;
			else
				first = player2;
		} else if(speed1 != speed2) {
			if(speed1 > speed2)
				first = player1;
			else
				first = player2;
		} else
			first = Utils.randInt(0, 1) == 0 ? player1 : player2;
		
		ArrayList<String> fieldTitles = new ArrayList<>(3);
		ArrayList<String> fieldValues = new ArrayList<>(3);
		// TODO add some message embeds to the move logs; each move will either have its own embed or its own
		Player winner = doMove(first, true, fieldTitles, fieldValues);
		if(winner == null)
			winner = doMove(first.opponent, false, fieldTitles, fieldValues);
		
		StringBuilder msg = new StringBuilder(); // post move effects only
		first.pokemon.processEffects(new PlayerContext(first, first.opponent, msg));
		first.opponent.pokemon.processEffects(new PlayerContext(first.opponent, first, msg));
		if(msg.length() > 0) {
			fieldTitles.add("Effects");
			fieldValues.add(msg.toString());
		}
		
		// another win check in case the trailing effects caused someone to feint
		if(winner == null && first.pokemon.getHealth() <= 0)
			winner = first.opponent;
		else if(winner == null && first.opponent.pokemon.getHealth() <= 0)
			winner = first;
		
		final Mono<Void> postMono;
		final String endMsg;
		if(winner != null) {
			if(winner.pokemon.getHealth() > 0) {
				endMsg = "***"+winner+" wins!***";
				winner.pokemon.pokemon.onDefeat(winner.opponent.pokemon.pokemon);
				postMono = winner.onFinish(BattleResult.WIN).then(
					winner.opponent.onFinish(BattleResult.LOSE)
				);
			} else {
				endMsg = "***Both pokemon have fainted...***";
				postMono = winner.onFinish(BattleResult.TIE).then(
					winner.opponent.onFinish(BattleResult.TIE)
				);
			}
			running = false;
		} else {
			endMsg = null;
			postMono = Mono.empty();
		}
		
		player1.resetMove();
		player2.resetMove();
		
		return broadcastEmbed(spec -> {
			spec.setTitle("Turn Results");
			for(int i = 0; i < fieldTitles.size(); i++)
				spec.addField(fieldTitles.get(i), fieldValues.get(i), false);
		}).then(Mono.defer(() -> {
			if(endMsg != null)
				return broadcastMessage(endMsg).then(postMono);
			return postMono;
		})).then(Mono.fromCallable(() -> running));
	}
	
	private Player doMove(Player player, boolean isFirst, List<String> fieldTitles, List<String> fieldValues) {
		if(player.moveIdx >= 0) // update last move used
			player.lastMoveIdx = player.moveIdx;
		final Move move = player.getMove();
		// System.out.println("doing player move: "+player+" with move "+move+", idx "+player.moveIdx);
		if(move != null) {
			StringBuilder msg = new StringBuilder();
			MoveContext context = new MoveContext(move, player, player.opponent, isFirst, msg);
			final String titleText = context.doMove();
			fieldTitles.add(titleText);
			fieldValues.add(msg.toString());
			if(context.enemy.getHealth() <= 0)
				return player;
			else if(context.user.getHealth() <= 0)
				return player.opponent; // recoil
		} else {
			fieldTitles.add(player.pokemon.pokemon.getName()+" is recharging.");
			fieldValues.add("zzz...");
			player.pokemon.clearFlag(Flag.FORCED_MOVE);
		}
		
		return null;
	}
	
	public Mono<Void> forfeit(UserPlayer player) {
		return broadcastMessage(player+" has fled the battle.").then(Flux.merge(
			player.onFinish(BattleResult.LOSE), 
			player.getOpponent().onFinish(BattleResult.WIN)
		).then());
	}
	
	public static abstract class Player {
		// public final String name;
		public final BattlePokemon pokemon;
		private BattleInstance battle;
		private boolean ready = false;
		private int moveIdx = -1;
		private int lastMoveIdx = -1;
		private Player opponent;
		private int lastDamageTaken;
		
		public Player(Pokemon pokemon) {
			// this.name = name;
			this.pokemon = new BattlePokemon(pokemon);
		}
		
		public Player getOpponent() {
			return opponent;
		}
		
		@Nullable
		Move getMove() {
			if(moveIdx < 0) {
				// either resting
				if(pokemon.hasFlag(Flag.REST_MESSAGE))
					return null;
				// or struggling
				return Moves.Struggle.getMove();
			}
			return pokemon.pokemon.getMove(moveIdx);
		}
		
		int getMoveIdx() {
			return moveIdx;
		}
		public int getLastMoveIdx() { return lastMoveIdx; }
		
		public void setLastDamage(int damage) {
			this.lastDamageTaken = damage;
		}
		public int getLastDamage() { return lastDamageTaken; }
		
		private void resetMove() {
			moveIdx = -1;
			ready = false;
			lastDamageTaken = -1;
			pokemon.clearFlag(Flag.FLINCH);
		}
		
		public BattleInstance getBattle() { return battle; }
		
		abstract Mono<Void> onFinish(@Nullable BattleResult result);
		
		abstract String getPlayerName();
		
		String getDescriptor() {
			return (pokemon.pokemon.hasNickname() ? pokemon.pokemon.getName()+", " : "")+"Lv. "+pokemon.pokemon.getLevel()+" "+pokemon.pokemon.species.name;
		}
		
		String getHealthDisplay() {
			return "HP: "+pokemon.getHealth()+"/"+pokemon.pokemon.getStat(Stat.Health);
		}
		
		@Override
		public String toString() { return getPlayerName(); }
	}
}
