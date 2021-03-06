package bot.world.pokemon.move;

import bot.world.pokemon.DamageCategory;
import bot.world.pokemon.Stat;
import bot.world.pokemon.Stat.StageEquation;
import bot.world.pokemon.battle.MoveContext;
import bot.util.Utils;

public interface DamageCalculator {
	
	int getDamage(MoveContext context, DamageCategory damageType);
	
	default int getDisplayPower() {
		if(this instanceof ClassicDamage)
			return ((ClassicDamage)this).power;
		return -1;
	}
	
	// a fixed damage effect is easy enough to do with a lambda so it won't have a class
	
	class PercentageDamage implements DamageCalculator {
		
		// takes a percent of the target's remaining health, or total health
		private final int percent;
		private final boolean useTotalHealth;
		
		PercentageDamage(int percent, boolean useTotalHealth) {
			this.percent = percent;
			this.useTotalHealth = useTotalHealth;
		}
		
		@Override
		public int getDamage(MoveContext context, DamageCategory damageType) {
			int healthValue = useTotalHealth ? context.enemyPokemon.getStat(Stat.Health) : context.enemy.getHealth();
			return healthValue * percent / 100;
		}
	}
	
	class ClassicDamage implements DamageCalculator {
		
		private final int power;
		private final int critBonus;
		private PowerModifier powerMod;
		
		@FunctionalInterface
		interface PowerModifier {
			int getPower(MoveContext context, int basePower);
		}
		
		ClassicDamage(int power, int critBonus) {
			this.power = power;
			this.critBonus = critBonus;
		}
		
		void modifyPower(PowerModifier modifier) {
			powerMod = modifier;
		}
		
		protected int getPower(MoveContext context) {
			if(powerMod != null)
				return powerMod.getPower(context, power);
			return power;
		}
		
		@Override
		public int getDamage(MoveContext context, DamageCategory damageType) {
			// check critical
			final boolean critical = isCritical(critBonus);
			if(critical)
				context.line("A critical hit!");
			
			// System.out.println("doing damage with move "+move+", damage type "+damageType);
			
			final Stat attackStat = damageType.getAttackStat();
			final Stat defenseStat = damageType.getDefenseStat();
			
			int attackStage = context.user.getStage(attackStat);
			if(critical) attackStage = Math.max(0, attackStage);
			int defenseStage = context.enemy.getStage(defenseStat);
			if(critical) defenseStage = Math.max(0, defenseStage);
			
			final int attack = StageEquation.Main.modifyStat(context.userPokemon.getStat(attackStat), attackStage);
			final int defense = StageEquation.Main.modifyStat(context.enemyPokemon.getStat(defenseStat), defenseStage);
			
			int damage = (2 * context.userPokemon.getLevel() / 2 + 2) * attack * getPower(context) / defense / 50 + 2;
			if(critical)
				damage = damage * 3 / 2;
			
			return damage;
		}
		
		// to get value do 1/x
		private static final int[] critChances = {24, 8, 2, 1};
		public static boolean isCritical(int critStage) {
			critStage = Utils.clamp(critStage, 0, critChances.length-1);
			return Utils.randInt(1, critChances[critStage]) == 1;
		}
	}
}
