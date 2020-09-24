package bot.world.pokemon.move;

import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.battle.status.StatusAilment;

public class StatusEffect implements MoveEffect {
	
	private final StatusAilment status;
	private final boolean onEnemy;
	
	public StatusEffect(StatusAilment status) { this(true, status); }
	public StatusEffect(boolean onEnemy, StatusAilment status) {
		this.onEnemy = onEnemy;
		this.status = status;
	}
	
	@Override
	public EffectResult doEffect(MoveContext context) {
		context.withPlayer(!onEnemy, " has been afflicted with ").append(status).append("! ...except not really because those aren't implemented just yet...");
		return EffectResult.NA;
	}
	
}
