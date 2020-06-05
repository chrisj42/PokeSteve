package bot.pokemon.move;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.DamageType;
import bot.pokemon.Move;

import com.fasterxml.jackson.databind.JsonNode;

public class DamageEffect extends MoveEffect {
	
	private final Move move;
	private final DamageType damageType;
	private final int power;
	private final int critRateBonus;
	private final int minHits;
	private final int maxHits;
	
	public DamageEffect(Move move, JsonObjectNode node, JsonObjectNode meta) throws MissingPropertyException {
		this.move = move;
		int damageTypeId = NodeParser.getResourceId(node.getObjectNode("damage_class")) - 1;
		damageType = damageTypeId < 2 ? DamageType.values[damageTypeId] : null;
		power = node.parseValueNode("power", JsonNode::intValue);
		if((damageType == null) != (power == 0))
			System.err.println("move "+node.parseValueNode("name", JsonNode::textValue)+" has conflicting power and damage type.");
		critRateBonus = meta.parseValueNode("crit_rate", JsonNode::intValue);
		minHits = meta.parseValueNode("min_hits", JsonNode::intValue);
		maxHits = meta.parseValueNode("max_hits", JsonNode::intValue);
	}
	
}
