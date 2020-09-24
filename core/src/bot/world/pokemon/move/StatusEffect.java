package bot.world.pokemon.move;

import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.battle.PlayerContext;
import bot.world.pokemon.battle.status.StatusAilment;

public class StatusEffect implements MoveEffect {
	
	private static final StatusEffect[] user = new StatusEffect[StatusAilment.values.length];
	private static final StatusEffect[] enemy = new StatusEffect[StatusAilment.values.length];
	static {
		for(StatusAilment ailment: StatusAilment.values) {
			final int ord = ailment.ordinal();
			user[ord] = new StatusEffect(false, ailment);
			enemy[ord] = new StatusEffect(true, ailment);
		}
	}
	
	public static StatusEffect get(StatusAilment ailment) { return get(true, ailment); }
	public static StatusEffect get(boolean onEnemy, StatusAilment ailment) {
		return (onEnemy ? enemy : user)[ailment.ordinal()];
	}
	
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
