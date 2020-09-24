package bot.world.pokemon.move;

import java.util.LinkedList;

import bot.world.pokemon.move.FieldEffectSet.FieldEffect;
import bot.world.pokemon.move.Moves.MoveBuilder;
import bot.world.pokemon.move.PokemonEffectSet.PokemonEffect;

public class EffectGroup {
	
	public static final EffectGroup NO_EFFECTS = new EffectGroup(PokemonEffectSet.NO_EFFECT, PokemonEffectSet.NO_EFFECT, FieldEffectSet.NO_EFFECT);
	
	public final PokemonEffectSet self;
	public final PokemonEffectSet enemy;
	public final FieldEffectSet field;
	
	public EffectGroup(PokemonEffectSet self, PokemonEffectSet enemy, FieldEffectSet field) {
		this.self = self;
		this.enemy = enemy;
		this.field = field;
	}
	
	static class EffectGroupBuilder {
		
		private final MoveBuilder move;
		private final boolean primary;
		private final LinkedList<PokemonEffect> self;
		private final LinkedList<PokemonEffect> enemy;
		private final LinkedList<FieldEffect> field;
		
		EffectGroupBuilder(MoveBuilder move, boolean primary) {
			this.move = move;
			this.primary = primary;
			self = new LinkedList<>();
			enemy = new LinkedList<>();
			field = new LinkedList<>();
		}
		
		EffectGroupBuilder affectAll(PokemonEffect effect) {
			self.add(effect);
			enemy.add(effect);
			return this;
		}
		EffectGroupBuilder affectSelf(PokemonEffect effect) {
			self.add(effect);
			return this;
		}
		EffectGroupBuilder affectEnemy(PokemonEffect effect) {
			enemy.add(effect);
			return this;
		}
		EffectGroupBuilder affectField(FieldEffect effect) {
			field.add(effect);
			return this;
		}
		
		EffectGroup create() {
			return new EffectGroup(
				getPSet(self), getPSet(enemy), getFSet(field)
			);
		}
		
		MoveBuilder add() {
			return primary ? move.primary(create()) : move.secondary(create());
		}
		
		private PokemonEffectSet getPSet(LinkedList<PokemonEffect> effects) {
			if(effects.size() == 0)
				return PokemonEffectSet.NO_EFFECT;
			return new PokemonEffectSet(effects);
		}
		private FieldEffectSet getFSet(LinkedList<FieldEffect> effects) {
			if(effects.size() == 0)
				return FieldEffectSet.NO_EFFECT;
			return new FieldEffectSet(effects);
		}
	}
}
