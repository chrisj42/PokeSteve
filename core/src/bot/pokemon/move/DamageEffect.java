package bot.pokemon.move;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.DamageRelation;
import bot.pokemon.DamageType;
import bot.pokemon.Move;
import bot.pokemon.Stat;
import bot.pokemon.Stat.StageEquation;
import bot.pokemon.battle.MoveContext;
import bot.util.Utils;

import com.fasterxml.jackson.databind.JsonNode;

public class DamageEffect extends MoveEffect {
	
	private final Move move;
	public final DamageType damageType;
	public final int power;
	public final int critRateBonus;
	public final int minHits;
	public final int maxHits;
	
	public DamageEffect(Move move, JsonObjectNode node, JsonObjectNode meta) throws MissingPropertyException {
		this.move = move;
		int damageTypeId = NodeParser.getResourceId(node.getObjectNode("damage_class")) - 2;
		damageType = damageTypeId >= 0 ? DamageType.values[damageTypeId] : null;
		power = node.parseValueNode("power", JsonNode::intValue);
		if((damageType == null) != (power == 0))
			System.err.println("move "+node.parseValueNode("name", JsonNode::textValue)+" has conflicting power and damage type.");
		critRateBonus = meta.parseValueNode("crit_rate", JsonNode::intValue);
		minHits = meta.parseValueNode("min_hits", JsonNode::intValue);
		maxHits = meta.parseValueNode("max_hits", JsonNode::intValue);
	}
	
	public enum DamageMode {
		Default, Percentage, Fixed; // what power refers to
		// TODO list moves that use percentage or fixed damage modes, along with their values
	}
	
	public void doDamage(MoveContext context) {
		final DamageRelation relation1, relation2;
		if(move.type == null) {
			relation1 = DamageRelation.Regular;
			relation2 = DamageRelation.Regular;
		} else {
			relation1 = move.type.getDamageTo(context.enemySpecies.primaryType);
			relation2 = move.type.getDamageTo(context.enemySpecies.secondaryType);
			// check for type immunity
			if(relation1 == DamageRelation.NoEffect || relation2 == DamageRelation.NoEffect) {
				context.finish();
				context.withUser("is unaffected...");
				return;
			}
		}
		
		final boolean singleHit = minHits == 0 && maxHits == 0;
		final int hits = singleHit ? 1 : Utils.randInt(minHits, maxHits);
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
		if(!singleHit)
			context.line("Hit ").append(hits).append(hits == 1 ? " time!":" times!");
	}
	
	protected int getDamage(MoveContext context) {
		// check critical
		int critStage = critRateBonus;
		
		final boolean critical = isCritical(critStage);
		
		if(critical)
			context.line("A critical hit!");
		
		final Stat attackStat = damageType.getAttackStat();
		final Stat defenseStat = damageType.getDefenseStat();
		
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
	
	public static class DamagePower {
		public int effectiveness = 2;
		
		public String getEffectivenessMessage() {
			effectiveness = Utils.clamp(effectiveness, 1, 3);
			if(effectiveness == 1)
				return "It's not very effective...";
			if(effectiveness == 3)
				return "It's super effective!";
			return null;
		}
	}
}
