package bot.pokemon.battle;

import java.util.function.Supplier;

import bot.pokemon.Pokemon;
import bot.util.Utils;

import reactor.core.publisher.Mono;

public class WildBattle extends BattleInstance {
	
	public final UserPlayer player;
	public final Player opponent;
	
	public WildBattle(UserPlayer player, Pokemon opponent) {
		super(player, new AiPlayer(opponent));
		this.player = player;
		this.opponent = super.player2;
	}
	
	@Override
	public Mono<Void> onRoundStart() {
		return super.onRoundStart().then(Mono.fromCallable(() -> {
			// determine ai move
			return Utils.randInt(0, opponent.pokemon.pokemon.moveset.length - 1);
		}).flatMap(
			move -> submitAttack(opponent, move)
		)).then();
	}
}
