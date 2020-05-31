package bot.io.json.node;

import bot.io.json.MissingPropertyException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonObjectNode extends JsonParentNode {
	
	public JsonObjectNode(JsonNode root) throws MissingPropertyException {
		this(null, root, null);
	}
	JsonObjectNode(JsonParentNode parent, JsonNode node, String nameInParent) throws MissingPropertyException {
		super(parent, node, nameInParent);
	}
	
	public JsonObjectNode getObjectNode(String name) throws MissingPropertyException {
		return new JsonObjectNode(this, node.get(name), name);
	}
	
	public JsonArrayNode getArrayNode(String name) throws MissingPropertyException {
		return new JsonArrayNode(this, node.get(name), name);
	}
	
	public JsonValueNode getValueNode(String name) throws MissingPropertyException {
		return new JsonValueNode(this, node.get(name), name);
	}
	
	public JsonNode setChild(String name, JsonNode newChild) {
		return ((ObjectNode)node).replace(name, newChild);
	}
}
