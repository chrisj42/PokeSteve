package bot.pokemon;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import static bot.pokemon.DamageRelation.*;

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
	
	public static final Type[] values = Type.values();
	
	private DamageRelation[] relations;
	
	Type() {}
	
	private void init() {
		relations = new DamageRelation[values.length];
		Arrays.fill(relations, DamageRelation.Regular);
	}
	
	private Type setDamage(DamageRelation damage, Type... defendingTypes) {
		for(Type type: defendingTypes)
			relations[type.ordinal()] = damage;
		return this;
	}
	
	public DamageRelation getDamageTo(Type defending) {
		if(defending == null)
			return Regular;
		return relations[defending.ordinal()];
	}
	
	public DamageRelation getDamageFrom(Type attacking) {
		return attacking.relations[ordinal()];
	}
	
	
	public static DamageRelation getDamageRelation(@Nullable Type attacking, @Nullable Type defending) {
		if(attacking == null || defending == null)
			return Regular;
		return attacking.getDamageTo(defending);
	}
	
	static {
		for(Type type: Type.values)
			type.init();
		
		Normal.setDamage(NoEffect, Ghost)
			.setDamage(Reduced, Rock, Steel);
		Fighting.setDamage(NoEffect, Ghost)
			.setDamage(Reduced, Flying, Poison, Bug, Psychic, Fairy)
			.setDamage(Super, Normal, Rock, Steel, Ice, Dark);
		Flying
			.setDamage(Reduced, Rock, Steel, Electric)
			.setDamage(Super, Fighting, Bug, Grass);
		Poison.setDamage(NoEffect, Steel)
			.setDamage(Reduced, Poison, Ground, Rock, Ghost)
			.setDamage(Super, Grass, Fairy);
		Ground.setDamage(NoEffect, Flying)
			.setDamage(Reduced, Bug, Grass)
			.setDamage(Super, Poison, Rock, Steel, Fire, Electric);
		Rock
			.setDamage(Reduced, Fighting, Ground, Steel)
			.setDamage(Super, Flying, Bug, Fire, Ice);
		Bug
			.setDamage(Reduced, Fighting, Flying, Poison, Ghost, Steel, Fire, Fairy)
			.setDamage(Super, Grass, Psychic, Dark);
		Ghost.setDamage(NoEffect, Normal)
			.setDamage(Reduced, Dark)
			.setDamage(Super, Ghost, Psychic);
		Steel
			.setDamage(Reduced, Steel, Fire, Water, Electric)
			.setDamage(Super, Rock, Ice, Fairy);
		Fire
			.setDamage(Reduced, Rock, Fire, Water, Dragon)
			.setDamage(Super, Bug, Steel, Grass, Ice);
		Water
			.setDamage(Reduced, Water, Grass, Dragon)
			.setDamage(Super, Ground, Rock, Fire);
		Grass
			.setDamage(Reduced, Flying, Poison, Bug, Steel, Fire, Grass, Dragon)
			.setDamage(Super, Ground, Rock, Water);
		Electric.setDamage(NoEffect, Ground)
			.setDamage(Reduced, Grass, Electric, Dragon)
			.setDamage(Super, Flying, Water);
		Psychic.setDamage(NoEffect, Dark)
			.setDamage(Reduced, Steel, Psychic)
			.setDamage(Super, Fighting, Poison);
		Ice
			.setDamage(Reduced, Steel, Fire, Water, Ice)
			.setDamage(Super, Flying, Ground, Grass, Dragon);
		Dragon.setDamage(NoEffect, Fairy)
			.setDamage(Reduced, Steel)
			.setDamage(Super, Dragon);
		Dark
			.setDamage(Reduced, Fighting, Dark, Fairy)
			.setDamage(Super, Ghost, Psychic);
		Fairy
			.setDamage(Reduced, Poison, Steel, Fire)
			.setDamage(Super, Fighting, Dragon, Dark);
	}
}
