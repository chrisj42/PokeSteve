package bot.pokemon.move;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonArrayNode;
import bot.io.json.node.JsonObjectNode;

import com.fasterxml.jackson.databind.JsonNode;

public class MoveDescription {
	
	public final String shortText;
	public final String longText;
	public final String flavorText;
	
	public MoveDescription(JsonObjectNode moveNode) throws MissingPropertyException {
		JsonObjectNode effectNode = NodeParser.getEnglishNode(moveNode.getArrayNode("effect_entries"), true);
		if(effectNode == null) {
			shortText = null;
			longText = null;
		} else {
			shortText = effectNode.parseValueNode("short_effect", JsonNode::textValue);
			longText = effectNode.parseValueNode("effect", JsonNode::textValue);
		}
		
		JsonObjectNode flavorNode = NodeParser.getEnglishNode(moveNode.getArrayNode("flavor_text_entries"), false);
		flavorText = flavorNode == null ? null : flavorNode.parseValueNode("flavor_text", JsonNode::textValue);
	}
}
