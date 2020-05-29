package bot.io;

import java.io.IOException;
import java.util.function.Function;

import bot.Core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WritableJsonTraversal extends JsonTraversal<WritableJsonTraversal> {
	
	public interface NodeCreator<T> {
		JsonNode make(ObjectNode parent, T value);
	}
	
	private final Function<JsonNode, JsonNode> valueSetter;
	
	public WritableJsonTraversal(DataFile fileType) throws IOException, MissingPropertyException {
		super(fileType);
		valueSetter = null;
	}
	
	private WritableJsonTraversal(WritableJsonTraversal parent, Function<JsonNode, JsonNode> valueSetter, String depthString, JsonNode node) {
		super(parent, depthString, node);
		this.valueSetter = valueSetter;
	}
	
	@Override
	WritableJsonTraversal intoObjectNode(String fieldName, JsonNode node) {
		return new WritableJsonTraversal(this, nodeValue -> ((ObjectNode)getNode()).replace(fieldName, nodeValue), fieldName, node);
	}
	
	@Override
	WritableJsonTraversal intoArrayNode(int index, JsonNode node) {
		return new WritableJsonTraversal(this, nodeValue -> ((ArrayNode)getNode()).set(index, nodeValue), "["+index+"]", node);
	}
	
	public <T> JsonNode setProperty(String name, T value, NodeCreator<T> nodeCreator) throws MissingPropertyException, IOException {
		WritableJsonTraversal setProp = getProperty(name); // this only succeeds if the current node is an object type
		JsonNode newNode = nodeCreator.make((ObjectNode)getNode(), value); // create the new node
		JsonNode oldNode = setProp.valueSetter.apply(newNode);
		Core.jsonMapper.writeValue(location, getRoot()); // write to file, from the base of the object.
		return oldNode;
	}
}
