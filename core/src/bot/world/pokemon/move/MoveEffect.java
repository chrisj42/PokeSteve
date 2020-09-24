package bot.world.pokemon.move;

import bot.util.Utils;
import bot.world.pokemon.Stat;
import bot.world.pokemon.Type;
import bot.world.pokemon.battle.Flag;
import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.battle.PlayerContext;
import bot.world.pokemon.battle.status.StatusAilment;
import bot.world.pokemon.move.PersistentEffect.TimedPersistentEffect;

public interface MoveEffect {
	
	MoveEffect[] NONE = new MoveEffect[0];
	
	EffectResult doEffect(MoveContext context);
	
	MoveEffect FLINCH = (context) -> {
		if(context.isFirst) context.enemy.setFlag(Flag.FLINCH);
		return EffectResult.FAILURE;
	};
	
	MoveEffect THRASH = (context) -> {
		if(context.user.hasFlag(Flag.FORCED_MOVE))
			context.withUser(" is thrashing about!");
		else {
			context.withUser(" began thrashing about!");
			context.user.setFlag(Flag.FORCED_MOVE, context.userMoveIdx);
			context.user.addEffect(new TimedPersistentEffect(Utils.randInt(2, 3)) {
				@Override
				public void onEffectEnd(PlayerContext context) {
					context.withUser(" stopped thrashing about.");
					context.user.clearFlag(Flag.FORCED_MOVE);
				}
			});
		}
		return EffectResult.RECORDED;
	};
	
	MoveEffect DISABLE = (context) -> {
		if(context.enemy.hasFlag(Flag.DISABLED_MOVE))
			return EffectResult.FAILURE; // "but it failed"
		int lastMove = context.enemyPlayer.getLastMoveIdx();
		if(lastMove < 0)
			return EffectResult.FAILURE;
		
		Move move = context.enemyPokemon.getMove(lastMove);
		context.withEnemy("'s ").append(move).append(" was disabled!");
		context.enemy.setFlag(Flag.DISABLED_MOVE, lastMove);
		context.enemy.addEffect(new TimedPersistentEffect(4) {
			@Override
			public void onEffectEnd(PlayerContext context) {
				context.withUser("'s ").append(move).append(" is no longer disabled!");
				context.user.clearFlag(Flag.DISABLED_MOVE);
			}
		});
		return EffectResult.RECORDED;
	};
	
	MoveEffect LEECH_SEED = context -> {
		if(context.enemy.hasFlag(Flag.LEECH_SEED))
			return EffectResult.FAILURE;
		
		// no effect on grass pokemon
		if(context.enemySpecies.primaryType == Type.Grass || context.enemySpecies.secondaryType == Type.Grass)
			return EffectResult.FAILURE;
		
		context.withEnemy(" was seeded!");
		PersistentEffect effect = pcontext -> {
			int drain = pcontext.user.alterHealth(-pcontext.userPokemon.getStat(Stat.Health) / 8);
			int heal = pcontext.enemy.alterHealth(drain);
			pcontext.withUser(" was drained by leech seed!");
			if(heal > 0)
				pcontext.withEnemy(" regained health!");
			return true; // only stops when switched out, normally, so essentially never
		};
		context.enemy.setFlag(Flag.LEECH_SEED, effect);
		context.enemy.addEffect(effect);
		
		return EffectResult.RECORDED;
	};
}
