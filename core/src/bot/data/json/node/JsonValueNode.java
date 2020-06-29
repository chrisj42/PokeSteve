package bot.data.json.node;

import bot.data.json.MissingPropertyException;
import bot.data.json.NodeParser;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonValueNode extends TypedJsonNode {
	
	public JsonValueNode(JsonNode root) throws MissingPropertyException {
		this(null, root, null);
	}
	JsonValueNode(JsonParentNode parent, JsonNode node, String nameInParent) throws MissingPropertyException {
		super(parent, node, nameInParent);
	}
	
	public <T> T parseValue(NodeParser<T> parseFunction) {
		return parseFunction.parseNode(node);
	}
	
}
