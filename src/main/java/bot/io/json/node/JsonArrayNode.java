package bot.io.json.node;

import bot.io.json.MissingPropertyException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonArrayNode extends JsonParentNode {
	
	public JsonArrayNode(JsonNode root) throws MissingPropertyException {
		this(null, root, null);
	}
	JsonArrayNode(JsonParentNode parent, JsonNode node, String nameInParent) throws MissingPropertyException {
		super(parent, node, nameInParent);
	}
	
	public JsonObjectNode getObjectNode(int idx) throws MissingPropertyException {
		return new JsonObjectNode(this, node.get(idx), String.valueOf(idx));
	}
	
	public JsonArrayNode getArrayNode(int idx) throws MissingPropertyException {
		return new JsonArrayNode(this, node.get(idx), String.valueOf(idx));
	}
	
	public JsonValueNode getValueNode(int idx) throws MissingPropertyException {
		return new JsonValueNode(this, node.get(idx), String.valueOf(idx));
	}
	
	public JsonNode setChild(int idx, JsonNode newChild) {
		return ((ArrayNode)node).set(idx, newChild);
	}
}
