package bot.data.json.node;

import bot.data.json.MissingPropertyException;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class JsonParentNode extends TypedJsonNode {
	
	JsonParentNode(JsonParentNode parent, JsonNode node, String nameInParent) throws MissingPropertyException {
		super(parent, node, nameInParent);
	}
	
}
