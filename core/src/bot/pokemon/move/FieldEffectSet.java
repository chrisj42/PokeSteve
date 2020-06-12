package bot.pokemon.move;

import java.util.List;

import bot.pokemon.battle.MoveContext;

public class FieldEffectSet {
	
	public static final FieldEffectSet NO_EFFECT = new FieldEffectSet();
	private static final FieldEffect[] EMPTY = new FieldEffect[0];
	
	public interface FieldEffect {
		EffectResult doEffect(MoveContext context);
	}
	
	// weather and things
	private final FieldEffect[] effects;
	
	FieldEffectSet(FieldEffect... effects) {
		this.effects = effects;
	}
	FieldEffectSet(List<FieldEffect> effects) {
		this(effects.toArray(EMPTY));
	}
	
	public EffectResult doEffects(MoveContext context) {
		EffectResult result = EffectResult.NA;
		for(FieldEffect effect: effects)
			result = result.combine(effect.doEffect(context));
		return result;
	}
}
