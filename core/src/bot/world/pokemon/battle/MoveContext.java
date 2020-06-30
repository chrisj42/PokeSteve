package bot.world.pokemon.battle;

import bot.world.pokemon.move.Move;
import bot.world.pokemon.battle.BattleInstance.Player;

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
	
	String doMove() {
		return userMove.doMove(this);
	}
}
