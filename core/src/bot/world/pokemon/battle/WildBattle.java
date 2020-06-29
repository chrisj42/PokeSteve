package bot.world.pokemon.battle;

import bot.util.UsageException;
import bot.world.pokemon.Pokemon;

import reactor.core.publisher.Mono;

public class WildBattle extends BattleInstance {
	
	public final UserPlayer player;
	public final AiPlayer wildPokemon;
	
	public WildBattle(UserPlayer player, Pokemon wildPokemon) {
		super(player, new AiPlayer(wildPokemon));
		this.player = player;
		this.wildPokemon = (AiPlayer) super.player2;
	}
	
	@Override
	public Mono<Void> onRoundStart() {
		return super.onRoundStart().then(Mono.just(true)
			.flatMap(e -> {
				// determine ai move
				if(wildPokemon.getMoveIdx() >= 0)
					return Mono.empty(); // already chosen
				try {
					return submitAttack(wildPokemon, wildPokemon.selectMove());
				} catch(UsageException ex) {
					System.out.println("wild pokemon failed to pick a valid move: "+ex);
					return Mono.empty();
				}
			})
		).then();
	}
}
