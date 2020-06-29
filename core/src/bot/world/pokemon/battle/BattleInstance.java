package bot.world.pokemon.battle;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import bot.data.UserData;
import bot.world.pokemon.move.Move;
import bot.world.pokemon.Pokemon;
import bot.world.pokemon.Stat;
import bot.util.UsageException;
import bot.util.Utils;
import bot.world.pokemon.move.Moves;

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
	
	public Mono<Void> broadcast(String message) {
		return broadcast(p -> message);
	}
	public Mono<Void> broadcast(Function<UserPlayer, String> messageFetcher) {
		return userFlux()
			// .map(player -> player.channel)
			.flatMap(player -> {
				String message = messageFetcher.apply(player);
				return player.channel.createMessage(message);
			})
			.then();
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
		return broadcast(player -> {
			StringBuilder str = new StringBuilder("**__Next Round__**\n");
			Player opponent = ((Player)player).opponent;
			str.append("Opponent: ").append(opponent.getDescriptor()).append(" - Health: ").append(opponent.pokemon.getHealth()).append("/").append(opponent.pokemon.pokemon.getStat(Stat.Health));
			str.append("\nYour pokemon: Lv. ").append(player.pokemon.pokemon.getLevel()).append(" ").append(player.pokemon.pokemon.species);
			str.append(" - Health: ").append(player.pokemon.getHealth()).append("/").append(player.pokemon.pokemon.getStat(Stat.Health));
			
			// here we need to check if the user is capable of choosing a move this turn; there are a number of things that could prevent the user from doing so.
			
			List<Integer> availableMoves = player.pokemon.getAvailableMoves();
			boolean canSelect = availableMoves.size() >= 2;
			if(!canSelect) {
				int moveid = availableMoves.size() == 1 ? availableMoves.get(0) : -1;
				Move move = moveid < 0 ? null : player.pokemon.pokemon.getMove(moveid);
				if(move != null) {
					if(player.pokemon.hasFlag(Flag.FORCED_MOVE)) // something forced this one-move-only choice
						str.append("\n__\"").append(move).append("\" has been auto-selected.__");
					else // having only one move was just bad luck
						canSelect = true; // let the player select, even if there's only one choice
					// 	str.append("\n__\"").append(move).append("\" is the only available move and has been selected automatically.__");
				}
				else if(player.pokemon.hasFlag(Flag.REST_MESSAGE))
					str.append("\n__").append(player.pokemon.getFlag(Flag.REST_MESSAGE)).append("__");
				else // if there's no move and no message, then it's the struggle message
					str.append("\n__You don't have any available moves. Struggle will be used.__");
			}
			
			if(canSelect) {
				str.append("\nSelect your move with the `attack <move number>` command. Available moves:");
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
			return str.toString();
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
		
		return broadcast(player+" is ready.").then(
			Mono.fromCallable(() -> player.opponent.ready)
			.flatMap(ready -> {
				if(ready) return doRound().then(
					Mono.fromCallable(() -> running)
					.flatMap(hasBattle -> {
						if(hasBattle)
							return onRoundStart();
						return Mono.empty();
					})
				);
				if(player instanceof UserPlayer)
					return ((UserPlayer)player).channel.createMessage("Waiting on "+player.opponent).then();
				return Mono.empty();
			})
		);
	}
	
	// both pokemon have selected their moves
	// TODO later this will return a RichEmbed
	private Mono<Void> doRound() {
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
		
		StringBuilder msg = new StringBuilder();
		
		Player winner = doMove(first, true, msg);
		if(winner == null)
			winner = doMove(first.opponent, false, msg);
		
		first.pokemon.processEffects(new PlayerContext(first, first.opponent, msg));
		first.opponent.pokemon.processEffects(new PlayerContext(first.opponent, first, msg));
		// another win check in case the trailing effects caused someone to feint
		if(winner == null && first.pokemon.getHealth() <= 0)
			winner = first.opponent;
		else if(winner == null && first.opponent.pokemon.getHealth() <= 0)
			winner = first;
		
		Mono<Void> postMono = null;
		if(winner != null) {
			if(winner.pokemon.getHealth() > 0) {
				msg.append("\n***").append(winner).append(" wins!***");
				winner.pokemon.pokemon.onDefeat(winner.opponent.pokemon.pokemon);
				postMono = winner.onFinish(BattleResult.WIN).then(
					winner.opponent.onFinish(BattleResult.LOSE)
				);
			} else {
				msg.append("\n***Both pokemon have fainted...***");
				postMono = winner.onFinish(BattleResult.TIE).then(
					winner.opponent.onFinish(BattleResult.TIE)
				);
			}
			running = false;
		}
		
		player1.resetMove();
		player2.resetMove();
		
		return broadcast(msg.toString()).then(postMono == null ? Mono.empty() : postMono);
	}
	
	private Player doMove(Player player, boolean isFirst, StringBuilder msg) {
		if(player.moveIdx >= 0) // update last move used
			player.lastMoveIdx = player.moveIdx;
		final Move move = player.getMove();
		// System.out.println("doing player move: "+player+" with move "+move+", idx "+player.moveIdx);
		if(move != null) {
			MoveContext context = new MoveContext(move, player, player.opponent, isFirst, msg);
			context.doMove();
			if(context.enemy.getHealth() <= 0)
				return player;
			else if(context.user.getHealth() <= 0)
				return player.opponent; // recoil
		} else {
			msg.append('\n').append(player).append(" is recharging.");
			player.pokemon.clearFlag(Flag.FORCED_MOVE);
		}
		
		return null;
	}
	
	public Mono<Void> forfeit(UserPlayer player) {
		return broadcast(player.name+" has fled the battle.").then(Flux.merge(
			player.onFinish(BattleResult.LOSE), 
			player.getOpponent().onFinish(BattleResult.WIN)
		).then());
	}
	
	public static abstract class Player {
		public final String name;
		public final BattlePokemon pokemon;
		private BattleInstance battle;
		private boolean ready = false;
		private int moveIdx = -1;
		private int lastMoveIdx = -1;
		private Player opponent;
		
		public Player(String name, Pokemon pokemon) {
			this.name = name;
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
		
		private void resetMove() {
			moveIdx = -1;
			ready = false;
			pokemon.clearFlag(Flag.FLINCH);
		}
		
		public BattleInstance getBattle() { return battle; }
		
		abstract Mono<Void> onFinish(BattleResult result);
		
		public String getDescriptor() {
			return "Lv. "+pokemon.pokemon.getLevel()+" "+pokemon.pokemon.species.name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
