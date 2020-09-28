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
	
	public void addToEmbed(EmbedCreateSpec e) {
		if(flavorText != null)
			e.addField("Description", flavorText, false);
		else if(shortText != null)
			e.addField("Description", shortText, false);
		else if(longText != null)
			e.addField("Description", longText, false);
	}
}
