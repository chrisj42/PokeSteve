package bot.world.pokemon.move;

import bot.world.pokemon.Stat;
import bot.world.pokemon.battle.MoveContext;

public class StatEffect implements MoveEffect {
	
	private final Stat stat;
	private final int stageDelta;
	private final boolean onEnemy;
	
	public StatEffect(Stat stat, int stageDelta, boolean onEnemy) {
		this.stat = stat;
		this.stageDelta = stageDelta;
		this.onEnemy = onEnemy;
	}
	
	@Override
	public EffectResult doEffect(MoveContext context) {
		boolean change = (onEnemy ? context.enemy : context.user).alterStatStage(stat, stageDelta);
		
		String message = getStatChangeMessage(stageDelta, change);
		context.withPlayer(!onEnemy, "'s ").append(stat).append(message);
		
		return EffectResult.RECORDED;
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
		return " didn't change."; // should never actually reach here because amt will never be 0, but I'll just put this in anyway.
	}
}
