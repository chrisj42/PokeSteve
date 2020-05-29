package bot.pokemon.definiton;

import java.util.EnumMap;

public enum Type {
	
	Normal,
	Fighting,
	Flying,
	Poison,
	Ground,
	Rock,
	Bug,
	Ghost,
	Steel,
	Fire,
	Water,
	Grass,
	Electric,
	Psychic,
	Ice,
	Dragon,
	Dark,
	Fairy;
	
	
	Type() {}
	Type(DamageType damageType, int[] superEffective, int[] notEffective, int[] noEffect) {}
	
	// holds damage multiplier when a move of this type is against any other type
	private final EnumMap<Type, Float> typeEffectiveness = new EnumMap<>(Type.class);
}
