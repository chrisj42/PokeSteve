package bot.world.pokemon.move;

import bot.world.pokemon.Stat;
import bot.world.pokemon.battle.Flag;
import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.battle.PlayerContext;
import bot.world.pokemon.move.PersistentEffect.TimedPersistentEffect;
import bot.util.Utils;

public class TrapEffect implements MoveEffect {
	
	private final String messagePhrase;
	
	public TrapEffect(String messagePhrase) {
		this.messagePhrase = messagePhrase;
	}
	
	@Override
	public EffectResult doEffect(MoveContext context) {
		if(context.enemy.hasFlag(Flag.TRAP)) {
			context.withEnemy(" is already trapped by ").append(context.enemy.getFlag(Flag.TRAP).source).append("!");
			return EffectResult.RECORDED; // this is about whether output was given
		}
		PersistentTrapEffect effect = new PersistentTrapEffect(context.userMove);
		context.enemy.setFlag(Flag.TRAP, effect);
		context.enemy.addEffect(effect);
		context.withEnemy(messagePhrase).append(context.userPlayer).append('!');
		return EffectResult.RECORDED;
	}
	
	public static class PersistentTrapEffect extends TimedPersistentEffect {
		
		private final Move source;
		
		public PersistentTrapEffect(Move source) {
			super(Utils.randInt(4, 5));
			this.source = source;
		}
		
		@Override
		public boolean apply(PlayerContext context) {
			int damage = context.user.alterHealth(-context.userPokemon.getStat(Stat.Health) / 8);
			if(damage < 0)
				context.withUser(" took ").append(-damage).append(" damage from ").append(source).append('.');
			
			return super.apply(context);
		}
		
		@Override
		public void onEffectEnd(PlayerContext context) {
			context.user.clearFlag(Flag.TRAP);
			context.withUser(" is no longer trapped by ").append(source).append('.');
		}
	}
}
