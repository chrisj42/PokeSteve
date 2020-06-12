package bot.pokemon.move;

import java.util.EnumMap;

import bot.pokemon.Stat;
import bot.pokemon.battle.BattleInstance.Player;
import bot.pokemon.battle.BattlePokemon;
import bot.pokemon.battle.MoveContext;
import bot.pokemon.move.PokemonEffectSet.PokemonEffect;

public class StatProperty implements PokemonEffect {
	
	public static final StatProperty NO_EFFECT = new StatProperty(new EnumMap<>(Stat.class));
	
	private final EnumMap<Stat, Integer> statStageChanges;
	
	public StatProperty(EnumMap<Stat, Integer> statStageChanges) {
		this.statStageChanges = statStageChanges;
	}
	
	@Override
	public EffectResult doEffect(MoveContext context, boolean onEnemy) {
		if(statStageChanges.size() == 0)
			return EffectResult.NA;
		
		final BattlePokemon pokemon;
		final Player player;
		if(onEnemy) {
			pokemon = context.enemy;
			player = context.enemyPlayer;
		} else {
			pokemon = context.user;
			player = context.userPlayer;
		}
		
		statStageChanges.forEach((stat, amt) -> {
			boolean change = pokemon.alterStatStage(stat, amt);
			String message = getStatChangeMessage(amt, change);
			if(message != null)
				context.line(player).append("'s ").append(stat).append(message);
		});
		
		return EffectResult.AFFECTED;
	}
	
	private static String getStatChangeMessage(int amt, boolean change) {
		if(!change && amt < 0) return " can't go any lower!";
		if(!change && amt > 0) return " can't go any higher!";
		if(amt > 2) return " rose drastically!";
		if(amt == 2) return " sharply rose!";
		if(amt == 1) return " rose!";
		if(amt == -1) return " fell!";
		if(amt == -2) return " harshly fell!";
		if(amt < -2) return " severely fell!";
		return null;
	}
	
	static class StatBuilder {
		
		private final EnumMap<Stat, Integer> statStageChanges = new EnumMap<>(Stat.class);
		
		StatBuilder() {}
		
		StatBuilder set(Stat stat, int stageDelta) {
			statStageChanges.put(stat, stageDelta);
			return this;
		}
		
		StatProperty create() {
			return new StatProperty(statStageChanges);
		}
	}
	
}
