package bot.world.pokemon.move;

import bot.util.Utils;
import bot.world.pokemon.battle.Flag;
import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.battle.PlayerContext;
import bot.world.pokemon.move.PersistentEffect.TimedPersistentEffect;

public interface MoveEffect {
	
	MoveEffect[] NONE = new MoveEffect[0];
	
	EffectResult doEffect(MoveContext context);
	
	MoveEffect FLINCH = (context) -> {
		if(context.isFirst) context.enemy.setFlag(Flag.FLINCH);
		return EffectResult.NO_OUTPUT;
	};
	
	MoveEffect THRASH = (context) -> {
		if(context.user.hasFlag(Flag.FORCED_MOVE))
			context.withUser(" is thrashing about!");
		else {
			context.withUser(" began thrashing about!");
			context.user.setFlag(Flag.FORCED_MOVE, context.userMoveIdx);
			context.user.addEffect(new TimedPersistentEffect(Utils.randInt(2, 3)) {
				@Override
				protected void onEffectEnd(PlayerContext context) {
					context.withUser(" stopped thrashing about.");
					context.user.clearFlag(Flag.FORCED_MOVE);
					// TODO cause confusion
				}
			});
		}
		return EffectResult.RECORDED;
	};
	
	MoveEffect DISABLE = (context) -> {
		if(context.enemy.hasFlag(Flag.DISABLED_MOVE))
			return EffectResult.NO_OUTPUT; // "but it failed"
		int lastMove = context.enemyPlayer.getLastMoveIdx();
		if(lastMove < 0)
			return EffectResult.NO_OUTPUT;
		
		Move move = context.enemyPokemon.getMove(lastMove);
		context.withEnemy("'s ").append(move).append(" was disabled!");
		context.enemy.setFlag(Flag.DISABLED_MOVE, lastMove);
		context.enemy.addEffect(new TimedPersistentEffect(4) {
			@Override
			protected void onEffectEnd(PlayerContext context) {
				context.withUser("'s ").append(move).append(" is no longer disabled!");
				context.user.clearFlag(Flag.DISABLED_MOVE);
			}
		});
		return EffectResult.RECORDED;
	};
}
