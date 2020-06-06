package bot.pokemon.move;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.Move;
import bot.pokemon.StatusEffect;

import com.fasterxml.jackson.databind.JsonNode;

public class ApplyStatusEffect extends MoveEffect {
	
	private final Move move;
	public final StatusEffect statusEffect;
	public final int chance;
	
	public ApplyStatusEffect(Move move, JsonObjectNode node, JsonObjectNode meta) throws MissingPropertyException {
		this.move = move;
		int statusId = NodeParser.getResourceId(meta.getObjectNode("ailment")) - 1;
		statusEffect = statusId < 0 ? null : StatusEffect.values[statusId];
		chance = meta.parseValueNode("ailment_chance", JsonNode::intValue);
		if(chance != 0 && move.effectChance != chance)
			System.err.println("move "+move.name+" has inconsistent effect chance and ailment chance.");
	}
	
}
