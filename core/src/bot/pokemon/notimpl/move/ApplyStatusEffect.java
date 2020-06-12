package bot.pokemon.notimpl.move;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.battle.status.StatusEffect;

import com.fasterxml.jackson.databind.JsonNode;

public class ApplyStatusEffect extends MoveEffect {
	
	// TODO impl status effects next
	
	public final StatusEffect statusEffect;
	public final int chance;
	
	public ApplyStatusEffect(JsonObjectNode node, JsonObjectNode meta) throws MissingPropertyException {
		int statusId = NodeParser.getResourceId(meta.getObjectNode("ailment")) - 1;
		statusEffect = statusId < 0 || statusId >= StatusEffect.values.length ? null : StatusEffect.values[statusId];
		chance = meta.parseValueNode("ailment_chance", JsonNode::intValue);
		// if(chance != 0 && move.effectChance != chance)
		// 	System.err.println("move "+move.name+" has inconsistent effect chance and ailment chance.");
	}
	
}
