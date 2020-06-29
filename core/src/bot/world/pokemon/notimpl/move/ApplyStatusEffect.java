package bot.world.pokemon.notimpl.move;

import bot.data.json.MissingPropertyException;
import bot.data.json.NodeParser;
import bot.data.json.node.JsonObjectNode;
import bot.world.pokemon.battle.status.StatusEffect;

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
