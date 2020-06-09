package bot.pokemon.move;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.DamageType;
import bot.pokemon.Move;
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
	
	public int doDamage(MoveContext context) {
		if(damageType == null || power == 0)
			return 0;
		
		final int attackStat = StageEquation.Main.modifyStat(context.userPokemon.getStat(damageType.getAttackStat()), context.user.getStage(damageType.getAttackStat()));
		final int defenseStat = StageEquation.Main.modifyStat(context.enemyPokemon.getStat(damageType.getDefenseStat()), context.enemy.getStage(damageType.getDefenseStat()));
		int damage = (2 * context.userPokemon.getLevel() / 2 + 2) * attackStat * power / defenseStat / 50 + 2;
		
		// type effectiveness
		DamagePower powerTracker = new DamagePower();
		damage = move.type.getDamageTo(context.enemySpecies.primaryType).multiplyDamage(damage, powerTracker);
		if(context.enemySpecies.secondaryType != null)
			damage = move.type.getDamageTo(context.enemySpecies.secondaryType).multiplyDamage(damage, powerTracker);
		// same type attack bonus
		if(move.type == context.userSpecies.primaryType
			|| move.type == context.userSpecies.secondaryType)
			damage = damage * 3 / 2;
		
		if(damage > 0) {
			String message = powerTracker.getEffectivenessMessage();
			if(message != null)
				context.msg.append("\n").append(message);
		}
		
		return damage;
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
