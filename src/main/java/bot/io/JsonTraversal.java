package bot.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import bot.Core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public abstract class JsonTraversal<JT extends JsonTraversal<JT>> {
	
	final File location;
	final String depthString;
	final JT parent;
	private JsonNode node;
	
	JsonTraversal(DataFile fileType) throws IOException, MissingPropertyException {
		String filepath = fileType.path;
		this.location = new File(filepath);
		
		JT fileRoot;
		try {
			fileRoot = intoObjectNode("", Core.jsonMapper.readTree(location));
		} catch(IOException e) {
			throw new IOException("Error reading "+ fileType.path, e);
		}
		
		this.node = fileRoot.getNode();
		depthString = fileRoot.depthString;
		parent = null;
	}
	JsonTraversal(JT parent, String depthString, JsonNode node) {
		this.parent = parent;
		this.location = parent.location;
		this.depthString = depthString;
		this.node = node;
	}
	
	abstract JT intoObjectNode(String fieldName, JsonNode node);
	abstract JT intoArrayNode(int index, JsonNode node);
	
	public JsonNode getNode() {
		return node;
	}
	
	public JsonNode getRoot() {
		if(parent != null) return parent.getRoot();
		return node;
	}
	
	public <T> T resolve(PropertyFunction<JsonNode, T> valueFetcher) throws MissingPropertyException {
		T value = valueFetcher.apply(node);
		if(value == null)
			throw new MissingPropertyException(location, String.format("node '%s' has an unexpected type: %s", this, node.getNodeType()));
		return value;
	}
	
	public static <T, JT extends JsonTraversal<JT>> PropertyFunction<JT, T> resolver(PropertyFunction<JsonNode, T> valueFetcher) {
		return linkNode -> linkNode.resolve(valueFetcher);
	}
	
	public JT getProperty(String name) throws MissingPropertyException {
		return getProperty(name, node -> node);
	}
	public <T> T getProperty(String name, PropertyFunction<JT, T> mapFunction) throws MissingPropertyException {
		try {
			return mapFunction.apply(intoObjectNode(name, resolve(node -> node.get(name))));
		} catch(MissingPropertyException e) {
			String errorMsg;
			JsonNodeType type = node.getNodeType();
			if(type != JsonNodeType.OBJECT) // not an object node
				errorMsg = String.format("node '%s' is not an object type, has no property '%s'; actual type: %s", this, name, type);
			else // actually is an object node
				errorMsg = String.format("object node '%s' does not have property '%s'", this, name);
			
			throw new MissingPropertyException(location, errorMsg);
		}
	}
	
	public <T> ArrayList<T> getArray(String name, PropertyFunction<JT, T> elementMapper) throws MissingPropertyException {
		JT arrayNodeTraversal = getProperty(name);
		JsonNode arrayNode = arrayNodeTraversal.getNode();
		if(!arrayNode.isArray()) {
			throw new MissingPropertyException(location, String.format(
				"object node '%s' expected to have array at '%s'; instead found %s",
				this, name, arrayNode.getNodeType())
			);
		}
		
		ArrayList<T> list = new ArrayList<>(arrayNode.size());
		int i = 0;
		for(JsonNode elementNode: arrayNode) {
			list.add(elementMapper.apply(arrayNodeTraversal.intoArrayNode(i++, elementNode)));
		}
		return list;
	}
	
	@Override
	public String toString() {
		return (parent == null ? "" : parent.toString()+".") + depthString;
	}
}
