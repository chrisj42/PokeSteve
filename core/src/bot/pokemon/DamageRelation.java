package bot.pokemon;

import bot.pokemon.move.DamageEffect.DamagePower;

public enum DamageRelation {
	NoEffect(0, 0),
	Reduced(.5f, -1),
	Regular(1, 0),
	Super(2, 1);
	
	private final float damageMult;
	private final int effectivenessDelta;
	
	DamageRelation(float damageMult, int effectivenessDelta) {
		this.damageMult = damageMult;
		this.effectivenessDelta = effectivenessDelta;
	}
	
	public int multiplyDamage(int damage, DamagePower powerTracker) {
		powerTracker.effectiveness += effectivenessDelta;
		return (int) Math.ceil(damage * damageMult);
	}
}
