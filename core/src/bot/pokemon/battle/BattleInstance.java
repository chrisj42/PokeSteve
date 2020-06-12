package bot.pokemon.battle;

import java.util.function.Function;

import bot.UserState;
import bot.pokemon.move.Move;
import bot.pokemon.Pokemon;
import bot.pokemon.Stat;
import bot.pokemon.Stat.StageEquation;
import bot.util.UsageException;
import bot.util.Utils;

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
	
	public Mono<Void> onRoundStart() {
		// send appropriate messages to all active players
		return broadcast(player -> {
			StringBuilder str = new StringBuilder("**__Next Round__**\n");
			Player opponent = ((Player)player).opponent;
			str.append("Opponent: ").append(opponent).append(opponent instanceof UserPlayer ? " ("+opponent.pokemon.pokemon.species+")" : "").append(" - Health: ").append(opponent.pokemon.getHealth()).append("/").append(opponent.pokemon.pokemon.getStat(Stat.Health));
			str.append("\nYour pokemon: ").append(player.pokemon.pokemon.species);
			str.append(" - Health: ").append(player.pokemon.getHealth()).append("/").append(player.pokemon.pokemon.getStat(Stat.Health));
			
			// here we need to check if the user is capable of choosing a move this turn; there are a number of things that could prevent the user from doing so.
			
			if(player.pokemon.hasFlag(Flag.CHARGING_MOVE))
				str.append("\n__You are currently charging \"").append(player.pokemon.pokemon.moveset[player.pokemon.getFlag(Flag.CHARGING_MOVE)]).append("\" and cannot select another move.__");
			else if(player.pokemon.hasFlag(Flag.RECHARGING))
				str.append("\n__You are recharging from a previous move and cannot select a move this turn.__");
			else {
				str.append("\nSelect your move with the 'attack <move number>' command. Available moves:");
				Move[] moves = player.pokemon.pokemon.moveset;
				for(int i = 0; i < moves.length; i++) {
					str.append("\n").append(i + 1).append(". ").append(moves[i]);
					str.append(" - PP: ").append(player.pokemon.getPp(i)).append("/").append(moves[i].pp);
				}
			}
			return str.toString();
		}).thenMany(userFlux()).flatMap(player -> {
			if(player.pokemon.hasFlag(Flag.CHARGING_MOVE))
				return submitAttack(player, player.pokemon.getFlag(Flag.CHARGING_MOVE)+1);
			else if(player.pokemon.hasFlag(Flag.RECHARGING))
				return submitAttack(player, 0);
			return Mono.empty();
		}).then();
	}
	
	public Mono<Void> submitAttack(Player player, int moveIdx) {
		moveIdx--;
		if(player.moveIdx >= 0)
			throw new UsageException(player+", you've already selected your move.");
		
		if(moveIdx >= 0 && player.pokemon.getPp(moveIdx) <= 0)
			throw new UsageException("move "+player.pokemon.pokemon.moveset[moveIdx]+" is out of PP.");
		
		player.moveIdx = moveIdx;
		player.ready = true;
		
		return broadcast(player+" is ready.").then(
			Mono.fromCallable(() -> player.opponent.ready)
			.flatMap(ready -> {
				if(ready) return broadcast(doRound()).then(
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
	private String doRound() {
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
		
		if(winner != null) {
			if(winner.pokemon.getHealth() > 0) {
				msg.append("\n***").append(winner).append(" wins!***");
				winner.pokemon.pokemon.onDefeat(winner.opponent.pokemon.pokemon);
			} else
				msg.append("\n***Both pokemon have fainted...***");
			if(player1 instanceof UserPlayer)
				UserState.leaveBattle(((UserPlayer)player1).user, false);
			if(player2 instanceof UserPlayer)
				UserState.leaveBattle(((UserPlayer)player2).user, false);
			running = false;
		}
		
		player1.resetMove();
		player2.resetMove();
		
		return msg.toString();
	}
	
	private Player doMove(Player player, boolean isFirst, StringBuilder msg) {
		final Move move = player.getMove();
		System.out.println("doing player move: "+player+" with move "+move+", idx "+player.moveIdx);
		if(move != null) {
			MoveContext context = new MoveContext(move, player, player.opponent, isFirst, msg);
			context.doMove();
			if(context.enemy.getHealth() <= 0)
				return player;
			else if(context.user.getHealth() <= 0)
				return player.opponent; // recoil
		} else {
			msg.append('\n').append(player).append(" is recharging.");
			player.pokemon.clearFlag(Flag.RECHARGING);
		}
		
		return null;
	}
	
	public static class Player {
		public final String name;
		public final BattlePokemon pokemon;
		private BattleInstance battle;
		private boolean ready = false;
		private int moveIdx = -1;
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
			if(moveIdx < 0) return null;
			return pokemon.pokemon.moveset[moveIdx];
		}
		
		int getMoveIdx() {
			return moveIdx;
		}
		
		private void resetMove() {
			moveIdx = -1;
			ready = false;
		}
		
		public BattleInstance getBattle() { return battle; }
		
		@Override
		public String toString() {
			return name;
		}
	}
}
