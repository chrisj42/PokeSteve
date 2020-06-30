package bot.data.json;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import bot.data.json.node.JsonArrayNode;
import bot.data.json.node.JsonObjectNode;
import bot.util.Utils;

import com.fasterxml.jackson.databind.JsonNode;

public interface NodeParser<T> {
	
	T parseNode(JsonNode node);
	
	static int getResourceId(JsonObjectNode resourceNode) throws MissingPropertyException {
		String url = resourceNode.parseValueNode("url", JsonNode::textValue);
		url = url.substring(0, url.length()-1); // cut off trailing "/"
		String idString = url.substring(url.lastIndexOf("/")+1);
		return Integer.parseInt(idString);
	}
	
	static <T extends Enum<T>> T parseEnumResource(JsonObjectNode resourceNode, Class<T> clazz) throws MissingPropertyException {
		return Utils.values(clazz)[getResourceId(resourceNode)-1];
	}
	
	static <T> T[] parseObjectArray(JsonArrayNode node, Class<T> clazz) throws MissingPropertyException {
		@SuppressWarnings("unchecked")
		T[] ar = (T[]) Array.newInstance(clazz, node.getLength());
		try {
			Constructor<T> constructor = clazz.getConstructor(JsonObjectNode.class);
			constructor.setAccessible(true);
			for(int i = 0; i < ar.length; i++) {
				JsonObjectNode cnode = node.getObjectNode(i);
				if(cnode.getNode().isNull()) continue;
				// System.out.println("parsing node "+i+" into "+clazz);
				ar[i] = constructor.newInstance(cnode);
			}
			return ar;
		} catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			MissingPropertyException ex = new MissingPropertyException("Error parsing object array");
			ex.initCause(e);
			throw ex;
		}
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
