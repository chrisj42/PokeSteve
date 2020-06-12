package bot.pokemon.notimpl.move;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.DamageCategory;

import com.fasterxml.jackson.databind.JsonNode;

public class DamageEffect extends MoveEffect {
	
	private final Move move;
	public final DamageCategory damageType;
	public final int power;
	public final int critRateBonus;
	public final int minHits;
	public final int maxHits;
	
	public DamageEffect(Move move, JsonObjectNode node, JsonObjectNode meta) throws MissingPropertyException {
		this.move = move;
		int damageTypeId = NodeParser.getResourceId(node.getObjectNode("damage_class")) - 2;
		damageType = damageTypeId >= 0 ? DamageCategory.values[damageTypeId] : null;
		if(move.name.equals("double-slap"))
			System.out.println("move damage type: "+damageType);
		power = node.parseValueNode("power", JsonNode::intValue);
		if((damageType == null) != (power == 0))
			System.err.println("move "+node.parseValueNode("name", JsonNode::textValue)+" has conflicting power and damage type.");
		critRateBonus = meta.parseValueNode("crit_rate", JsonNode::intValue);
		minHits = meta.parseValueNode("min_hits", JsonNode::intValue);
		maxHits = meta.parseValueNode("max_hits", JsonNode::intValue);
	}
}
