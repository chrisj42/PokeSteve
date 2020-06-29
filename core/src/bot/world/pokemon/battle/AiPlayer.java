package bot.world.pokemon.battle;

import bot.util.Utils;
import bot.world.pokemon.Pokemon;
import bot.world.pokemon.battle.BattleInstance.Player;

import reactor.core.publisher.Mono;

public class AiPlayer extends Player {
	
	public AiPlayer(Pokemon pokemon) {
		super("Wild "+pokemon.species, pokemon);
	}
	
	int selectMove() {
		/*int move = Utils.randInt(0, getOpponent().pokemon.pokemon.getMoveCount()-1);
		Integer disabled = getOpponent().pokemon.getFlag(Flag.DISABLED_MOVE);
		if(disabled != null && disabled == move)
			move = (move+1) % getOpponent().pokemon.pokemon.getMoveCount();
		return move;*/
		Integer rand = Utils.pickRandom(pokemon.getAvailableMoves());
		return rand == null ? -1 : rand;
	}
	
	@Override
	Mono<Void> onFinish(BattleResult result) { return Mono.empty(); }
	
	@Override
	public String getDescriptor() {
		return "Wild "+super.getDescriptor();
	}
}
