package bot.pokemon.battle;

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
		return super.onRoundStart().then(Mono.just(true)
			.flatMap(e -> {
				// determine ai move
				if(opponent.getMoveIdx() >= 0)
					return Mono.empty(); // already chosen
				int move = Utils.randInt(0, opponent.pokemon.pokemon.moveset.length-1);
				Integer disabled = opponent.pokemon.getFlag(Flag.DISABLED_MOVE);
				if(disabled != null && disabled == move)
					move = (move+1) % opponent.pokemon.pokemon.moveset.length;
				return submitAttack(opponent, move);
			})
		).then();
	}
}
