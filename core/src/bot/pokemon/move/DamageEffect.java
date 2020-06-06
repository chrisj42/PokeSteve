package bot.pokemon.move;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.DamageType;
import bot.pokemon.Move;

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
}
