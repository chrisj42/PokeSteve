package bot.pokemon.notimpl.move;

import java.util.LinkedList;

import bot.io.json.MissingPropertyException;
import bot.io.json.node.JsonObjectNode;

import com.fasterxml.jackson.databind.JsonNode;

public class MoveEffect {
	
	// an effect of a move. can be damage, status, or a number of things.
	
	/*public static MoveEffect[] parseEffects(JsonObjectNode moveNode) throws MissingPropertyException {
		JsonObjectNode metaNode = moveNode.getObjectNode("meta");
		
		LinkedList<MoveEffect> effects = new LinkedList<>();
		int power = moveNode.parseValueNode("power", JsonNode::intValue);
		if(power > 0)
			effects.add(new DamageEffect(moveNode, metaNode));
		
		return effects.toArray(new MoveEffect[0]);
	}*/
}
