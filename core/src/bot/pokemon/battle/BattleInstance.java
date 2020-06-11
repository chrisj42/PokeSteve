package bot.pokemon.battle;

import java.util.function.Function;

import bot.UserState;
import bot.pokemon.Move;
import bot.pokemon.Pokemon;
import bot.pokemon.Stat;
import bot.pokemon.Stat.StageEquation;
import bot.util.UsageException;
import bot.util.Utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
			str.append("Opponent: ").append(opponent).append(opponent instanceof UserPlayer ? " ("+opponent.pokemon.pokemon.species+")" : "").append(" - Health: ").append(opponent.pokemon.health).append("/").append(opponent.pokemon.pokemon.getStat(Stat.Health));
			str.append("\nYour pokemon: ").append(player.pokemon.pokemon.species);
			str.append(" - Health: ").append(player.pokemon.health).append("/").append(player.pokemon.pokemon.getStat(Stat.Health));
			str.append("\nSelect your move with the 'attack <move number>' command. Available moves:");
			Move[] moves = player.pokemon.pokemon.moveset;
			for(int i = 0; i < moves.length; i++) {
				str.append("\n").append(i+1).append(". ").append(moves[i]);
				str.append(" - PP: ").append(player.pokemon.getPp(i)).append("/").append(moves[i].pp);
			}
			return str.toString();
		});
	}
	
	public Mono<Void> submitAttack(Player player, int moveIdx) {
		moveIdx--;
		if(player.moveIdx >= 0)
			throw new UsageException(player+", you've already selected your move.");
		
		if(player.pokemon.getPp(moveIdx) <= 0)
			throw new UsageException("move "+player.pokemon.pokemon.moveset[moveIdx]+" is out of PP.");
		
		player.moveIdx = moveIdx;
		
		return broadcast(player+" has selected their move.").then(
			Mono.fromCallable(() -> player.opponent.moveIdx >= 0)
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
	
	public static class Player {
		public final String name;
		public final BattlePokemon pokemon;
		private BattleInstance battle;
		private int moveIdx = -1;
		private Player opponent;
		
		public Player(String name, Pokemon pokemon) {
			this.name = name;
			this.pokemon = new BattlePokemon(pokemon);
		}
		
		public Player getOpponent() {
			return opponent;
		}
		
		Move getMove() {
			// if(moveIdx < 0) return null;
			return pokemon.pokemon.moveset[moveIdx];
		}
		
		int getMoveIdx() {
			return moveIdx;
		}
		
		/*private void resetMove() {
			// Move move = pokemon.pokemon.moveset[moveIdx];
			pokemon.subtractPp(moveIdx);
			moveIdx = -1;
			// return move;
		}*/
		
		// public Player getOpponent() { return opponent; }
		
		public BattleInstance getBattle() { return battle; }
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	// both pokemon have selected their moves
	// TODO later this will return a RichEmbed
	private String doRound() {
		// final Move move1 = player1.flushMove();
		// final Move move2 = player2.flushMove();
		
		int speed1 = player1.pokemon.getSpeed();
		int speed2 = player2.pokemon.getSpeed();
		
		final Player first = speed1 >= speed2 ? player1 : player2;
		
		StringBuilder msg = new StringBuilder();
		
		Player winner = doMove(first, true, msg);
		if(winner == null)
			winner = doMove(first.opponent, false, msg);
		
		if(winner != null) {
			msg.append("\n").append(winner).append(" wins!");
			winner.pokemon.pokemon.onDefeat(winner.opponent.pokemon.pokemon);
			if(player1 instanceof UserPlayer)
				UserState.leaveBattle(((UserPlayer)player1).user, false);
			if(player2 instanceof UserPlayer)
				UserState.leaveBattle(((UserPlayer)player2).user, false);
			running = false;
		}
		
		player1.moveIdx = -1;
		player2.moveIdx = -1;
		
		return msg.toString();
	}
	
	// returns player if opponent faints, null otherwise
	private Player doMove(Player player, boolean isFirst, StringBuilder msg) {
		final Move move = player.getMove();
		final MoveContext context = new MoveContext(player, player.opponent, isFirst, msg);
		
		player.pokemon.subtractPp(context.userMoveIdx);
		msg.append("\n").append(player).append(" used ").append(move).append('!');
		
		// calc if hit
		int accuracy = move.accuracy;
		boolean hit = accuracy == 0;
		if(!hit) {
			int accuracyStage = Utils.clamp(context.user.getStage(Stat.Accuracy) - context.enemy.getStage(Stat.Evasion), BattlePokemon.MIN_STAGE, BattlePokemon.MAX_STAGE);
			accuracy = StageEquation.Accuracy.modifyStat(accuracy, accuracyStage);
			// System.out.println("accuracy: "+accuracy);
			hit = Utils.randInt(0, 99) < accuracy;
		}
		if(!hit)
			msg.append("\nIt missed!");
		else {
			
			boolean change = move.stat.doStatEffect(context);
			if(change)
				context.setHadEffect();
			
			move.damage.doDamage(context);
			
			if(context.enemy.health <= 0) {
				msg.append("\n").append(player.opponent).append(" fainted!");
				return player;
			}// else msg.append("\n");
		}
		
		return null;
	}
}
