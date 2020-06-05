package bot.pokemon.battle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

import bot.pokemon.Move;
import bot.pokemon.Pokemon;
import bot.pokemon.Stat;
import bot.util.UsageException;
import bot.util.Utils;

import discord4j.core.object.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class BattleInstance {
	
	// tracks the participating players in the battle, their pokemon, and the DM channels of the participating users
	
	final Player player1;
	final Player player2;
	
	private final HashMap<User, Player> userMap = new HashMap<>(2);
	
	public BattleInstance(Player player1, Player player2) {
		this.player1 = player1;
		this.player2 = player2;
		player1.opponent = player2;
		player2.opponent = player1;
		player1.battle = this;
		player2.battle = this;
		if(player1 instanceof UserPlayer)
			userMap.put(((UserPlayer)player1).user, player1);
		if(player2 instanceof UserPlayer)
			userMap.put(((UserPlayer)player2).user, player2);
	}
	
	public Mono<Void> broadcast(String message) {
		return broadcast(p -> message);
	}
	public Mono<Void> broadcast(Function<UserPlayer, String> messageFetcher) {
		return Flux.just(player1, player2)
			.filter(UserPlayer.class::isInstance)
			.map(UserPlayer.class::cast)
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
			StringBuilder str = new StringBuilder("Next Round\n");
			str.append("Your pokemon: ").append(player.pokemon.pokemon.species.name);
			str.append(" - Health: ").append(player.pokemon.getHealth()).append("/").append(player.pokemon.pokemon.getStat(Stat.Health));
			str.append("\nSelect your move with the 'attack' command. Available moves:");
			Move[] moves = player.pokemon.pokemon.moveset;
			for(int i = 0; i < moves.length; i++) {
				str.append("\n").append(moves[i].name).append(" - PP: ");
				str.append(player.pokemon.getPp(i)).append("/").append(moves[i].pp);
			}
			return str.toString();
		});
	}
	
	public Mono<Void> submitAttack(User player, int moveIdx) {
		return submitAttack(userMap.get(player), moveIdx);
	}
	
	public Mono<Void> submitAttack(Player player, int moveIdx) {
		if(player.moveIdx >= 0)
			throw new UsageException(player.name+", you've already selected your move.");
		player.moveIdx = moveIdx;
		
		return broadcast(player.name+" has selected their move.").then(
			Mono.fromCallable(() -> player.opponent.moveIdx >= 0)
			.flatMap(ready -> {
				if(ready) return broadcast(doRound());
				if(player instanceof UserPlayer)
					return ((UserPlayer)player).channel.createMessage("Waiting on "+player.opponent.name).then();
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
		
		public BattleInstance getBattle() { return battle; }
	}
	
	// both pokemon have selected their moves
	// TODO later this will return a RichEmbed
	private String doRound() {
		player1.moveIdx = -1;
		player2.moveIdx = -1;
		
		return "this is when the moves would actually play out, and damage would be a thing.\nBut it isn't yet.\n\nrip.";
	}
}
