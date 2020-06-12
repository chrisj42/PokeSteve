package bot.pokemon.battle;

import bot.pokemon.move.Move;
import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;
import bot.pokemon.battle.BattleInstance.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoveContext extends PlayerContext {
	
	@NotNull public final Move userMove;
	public final int userMoveIdx;
	@Nullable public final Move enemyMove;
	public final int enemyMoveIdx;
	
	public final boolean isFirst;
	
	// private boolean hadEffect = false;
	
	public MoveContext(@NotNull Move move, Player user, Player enemy, boolean isFirst, StringBuilder msg) {
		super(user, enemy, msg);
		this.userMove = move;
		this.userMoveIdx = user.getMoveIdx();
		
		this.enemyMove = enemy.getMove();
		this.enemyMoveIdx = enemy.getMoveIdx();
		
		this.isFirst = isFirst;
	}
	
	// public void setHadEffect() {
	// 	hadEffect = true;
	// }
	
	// public boolean hadEffect() { return hadEffect; }
	
	void doMove() {
		userMove.doMove(this);
	}
}
