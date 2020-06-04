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
	
	public int multiplyDamage(int damage) {
		return (int) Math.ceil(damage * damageMult);
	}
}
