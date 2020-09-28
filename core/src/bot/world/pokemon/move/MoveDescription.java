package bot.world.pokemon.move;

import bot.data.json.MissingPropertyException;
import bot.data.json.NodeParser;
import bot.data.json.node.JsonObjectNode;

import discord4j.core.spec.EmbedCreateSpec;

import com.fasterxml.jackson.databind.JsonNode;

public class MoveDescription {
	
	public final String shortText;
	public final String longText;
	public final String flavorText;
	
	public MoveDescription(JsonObjectNode moveNode) throws MissingPropertyException {
		JsonObjectNode effectNode = NodeParser.getEnglishNode(moveNode.getArrayNode("effect_entries"), true);
		shortText = effectNode.parseValueNode("short_effect", JsonNode::asText).replaceAll("[\\n\\r]", " ");
		longText = effectNode.parseValueNode("effect", JsonNode::asText).replaceAll("[\\n\\r]", " ");
		
		JsonObjectNode flavorNode = NodeParser.getEnglishNode(moveNode.getArrayNode("flavor_text_entries"), false);
		flavorText = flavorNode.parseValueNode("flavor_text", JsonNode::asText).replaceAll("[\\n\\r]", " ");
		
		// running this found no missing text in any of the cases...
		/*String name = moveNode.parseValueNode("name", JsonNode::textValue);
		if(shortText == null)
			System.out.println(name+" has no short text.");
		if(longText == null)
			System.out.println(name+" has no long text.");
		if(flavorText == null)
			System.out.println(name+" has no flavor text.");*/
	}
}
