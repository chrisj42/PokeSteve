package bot.pokemon.move.format;

import bot.pokemon.DamageRelation;
import bot.pokemon.Stat;
import bot.pokemon.Stat.StageEquation;
import bot.pokemon.Type;
import bot.pokemon.battle.MoveContext;
import bot.pokemon.move.DamageEffect.DamagePower;
import bot.pokemon.move.format.PokeUtil.RangeValue;
import bot.util.Utils;

public class DamageEffect extends MoveEffect {
	
	// classic effect that damages the enemy
	
	// private DamageTarget target;
	private Type damageType;
	private DamageCategory damageCategory;
	private int power;
	private int critRateBonus; // usually 0 or 1
	private RangeValue hitCount;
	
	public DamageEffect() {}
	
	@Override
	public void apply(MoveContext context) {
		// if(!PokeUtil.chance(getEffectChance()))
		// 	return;
		
		final DamageRelation relation1, relation2;
		if(damageType == null) {
			relation1 = DamageRelation.Regular;
			relation2 = DamageRelation.Regular;
		} else {
			relation1 = damageType.getDamageTo(context.enemySpecies.primaryType);
			relation2 = damageType.getDamageTo(context.enemySpecies.secondaryType);
			// check for type immunity
			if(relation1 == DamageRelation.NoEffect || relation2 == DamageRelation.NoEffect) {
				context.setHadEffect();
				context.withUser("is unaffected...");
				return;
			}
		}
		
		final int hits = hitCount == null ? 1 : hitCount.getValue();
		int damage = 0;
		for(int i = 0; i < hits; i++) {
			if(hits > 1)
				context.line("hitting ").append(i+1).append(" time").append(i+1==1?"":"s").append("...");
			damage += getDamage(context);
		}
		
		// type effectiveness
		DamagePower powerTracker = new DamagePower();
		damage = relation1.multiplyDamage(damage, powerTracker);
		damage = relation2.multiplyDamage(damage, powerTracker);
		
		String message = powerTracker.getEffectivenessMessage();
		if(message != null)
			context.line(message);
		
		// same type attack bonus
		if(context.userMove.getType() == context.userSpecies.primaryType
			|| context.userMove.getType() == context.userSpecies.secondaryType)
			damage = damage * 3 / 2;
		
		context.enemy.health -= damage;
		context.withEnemy("took ").append(damage).append(" damage!");
		if(hitCount != null)
			context.line("Hit ").append(hits).append(hits == 1 ? "time!":"times!");
	}
	
	protected int getDamage(MoveContext context) {
		// check critical
		int critStage = critRateBonus;
		
		final boolean critical = isCritical(critStage);
		
		if(critical)
			context.line("A critical hit!");
		
		final Stat attackStat = damageCategory.getAttackStat();
		final Stat defenseStat = damageCategory.getDefenseStat();
		
		int attackStage = context.user.getStage(attackStat);
		if(critical) attackStage = Math.max(0, attackStage);
		int defenseStage = context.enemy.getStage(defenseStat);
		if(critical) defenseStage = Math.max(0, defenseStage);
		
		final int attack = StageEquation.Main.modifyStat(context.userPokemon.getStat(attackStat), attackStage);
		final int defense = StageEquation.Main.modifyStat(context.enemyPokemon.getStat(defenseStat), defenseStage);
		
		return (2 * context.userPokemon.getLevel() / 2 + 2) * attack * power / defense / 50 + 2;
	}
	
	/*public enum DamageTarget {
		Self, Enemy, All;
	}*/
	
	// to get value do 1/x
	private static final int[] critChances = {24, 8, 2, 1};
	public static boolean isCritical(int critStage) {
		critStage = Utils.clamp(critStage, 0, critChances.length-1);
		return Utils.randInt(1, critChances[critStage]) == 1;
	}
	
	public enum DamageCategory {
		// null damage type means the move type is "status"
		Physical(Stat.Attack, Stat.Defense),
		Special(Stat.SpAttack, Stat.SpDefense);
		
		private final Stat attackStat;
		private final Stat defenseStat;
		
		DamageCategory(Stat attackStat, Stat defenseStat) {
			this.attackStat = attackStat;
			this.defenseStat = defenseStat;
		}
		
		public Stat getAttackStat() { return attackStat; }
		public Stat getDefenseStat() { return defenseStat; }
		
	}
	
	// public DamageTarget getTarget() {
	// 	return target;
	// }
	
	public Type getDamageType() {
		return damageType;
	}
	
	public DamageCategory getDamageCategory() {
		return damageCategory;
	}
	
	public int getPower() {
		return power;
	}
	
	public int getCritRateBonus() {
		return critRateBonus;
	}
	
	public RangeValue getHitCount() {
		return hitCount;
	}
}
