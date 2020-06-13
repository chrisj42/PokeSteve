package bot.pokemon.move;

import java.util.function.Function;

import bot.pokemon.Stat;
import bot.pokemon.battle.BattleInstance.Player;
import bot.pokemon.battle.Flag;
import bot.pokemon.battle.MoveContext;
import bot.pokemon.battle.PlayerContext;
import bot.pokemon.move.PersistentEffect.TimedPersistentEffect;
import bot.pokemon.move.PokemonEffectSet.PokemonEffect;
import bot.util.Utils;

public class TrapProperty implements PokemonEffect {
	
	private final Function<Player, String> messageFetcher;
	
	public TrapProperty(Function<Player, String> messageFetcher) {
		this.messageFetcher = messageFetcher;
	}
	
	@Override
	public EffectResult doEffect(MoveContext context, boolean onEnemy) {
		Player p = onEnemy ? context.enemyPlayer : context.userPlayer;
		if(p.pokemon.hasFlag(Flag.TRAP)) {
			context.with(p).append("is already trapped by ").append(p.pokemon.getFlag(Flag.TRAP).source).append("!");
			return EffectResult.RECORDED; // this is about whether output was given
		}
		TrapEffect effect = new TrapEffect(context.userMove);
		p.pokemon.setFlag(Flag.TRAP, effect);
		p.pokemon.addEffect(effect);
		context.line(messageFetcher.apply(p));
		return EffectResult.RECORDED;
	}
	
	public static class TrapEffect extends TimedPersistentEffect {
		
		private final Move source;
		
		public TrapEffect(Move source) {
			super(Utils.randInt(4, 5));
			this.source = source;
		}
		
		@Override
		public boolean apply(PlayerContext context) {
			int damage = context.user.alterHealth(-context.userPokemon.getStat(Stat.Health) / 8);
			if(damage < 0)
				context.withUser("took ").append(-damage).append(" damage from ").append(source).append('.');
			
			return super.apply(context);
		}
		
		@Override
		protected void onEffectEnd(PlayerContext context) {
			context.user.clearFlag(Flag.TRAP);
			context.withUser("is no longer trapped by ").append(source).append('.');
		}
	}
}
