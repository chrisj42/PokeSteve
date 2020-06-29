package bot.data.json;

import bot.data.json.node.JsonArrayNode;
import bot.data.json.node.JsonObjectNode;

import com.fasterxml.jackson.databind.JsonNode;

public interface NodeParser<T> {
	
	T parseNode(JsonNode node);
	
	static int getResourceId(JsonObjectNode resourceNode) throws MissingPropertyException {
		String url = resourceNode.parseValueNode("url", JsonNode::textValue);
		url = url.substring(0, url.length()-1); // cut off trailing "/"
		String idString = url.substring(url.lastIndexOf("/")+1);
		return Integer.parseInt(idString);
	}
	
	static JsonObjectNode getEnglishNode(JsonArrayNode arrayNode, boolean startFront) throws MissingPropertyException {
		JsonObjectNode node = null;
		for(
			int i = startFront ? 0 : arrayNode.getLength() - 1;
			startFront ? i < arrayNode.getLength() : i >= 0;
			i += startFront ? 1 : -1) {
			node = arrayNode.getObjectNode(i);
			if(node.getObjectNode("language").parseValueNode("name", JsonNode::textValue).equals("en"))
				break;
		}
		return node;
	}
}
