package bot.data.json.node;

import bot.data.json.MissingPropertyException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public abstract class TypedJsonNode {
	
	final JsonParentNode parent;
	final JsonNode node;
	
	TypedJsonNode(JsonParentNode parent, JsonNode node, String nameInParent) throws MissingPropertyException {
		this.parent = parent;
		this.node = node;
		if(node == null)
			throw new MissingPropertyException(String.format("%s has no child node %s", parent, nameInParent));
	}
	
	@Override
	public String toString() {
		if(node == null)
			return getClass().getSimpleName()+"(null node)";
		
		return getClass().getSimpleName()+"(actual:"+node.getNodeType()+")";
	}
	
	public JsonNode getNode() { return node; }
	
	public boolean hasValue() {
		return node.getNodeType() != JsonNodeType.NULL &&
			node.getNodeType() != JsonNodeType.MISSING;
	}
}
