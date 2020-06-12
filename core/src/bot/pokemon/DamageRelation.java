package bot.pokemon;

public enum DamageRelation {
	NoEffect(0),
	Reduced(.5f),
	Regular(1),
	Super(2);
	
	private final float damageMult;
	
	DamageRelation(float damageMult) {
		this.damageMult = damageMult;
	}
	
	public static int multiplyDamage(int damage, DamageRelation... relations) {
		float damCache = damage;
		for(DamageRelation relation: relations)
			damCache *= relation.damageMult;
		return (int) Math.ceil(damCache);
	}
}
