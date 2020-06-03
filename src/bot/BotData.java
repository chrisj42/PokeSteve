package bot;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;

import bot.io.DataFile;
import bot.io.json.MissingPropertyException;
import bot.io.json.node.JsonObjectNode;
import bot.util.POJO;
import bot.util.ThrowFunction;

import com.fasterxml.jackson.databind.JsonNode;

public class BotData {
	
	// private final WritableJsonTraversal dataWriter; // use to update and save the editable bot data.
	
	// final DynamicProperty<Presence> defaultStatus;
	
	static BotData load() throws IOException, MissingPropertyException {
		return new BotData();
	}
	
	private BotData() throws IOException, MissingPropertyException {
		JsonObjectNode config = DataFile.CONFIG.readJson();
		// ReadOnlyJsonTraversal config = new ReadOnlyJsonTraversal(DataFile.CONFIG);
		// prefix = config.getProperty("prefix", resolver(JsonNode::textValue));
		// owner = config.getProperty("owner", resolver(MapFunction.attach(JsonNode::textValue, Snowflake::of)));
		// isBlobbo = config.getProperty("bot", resolver(JsonNode::booleanValue));
		// greetingOrder = config.getProperty("greetingOrder", resolver(JsonNode::intValue));
		
		// dataWriter = new WritableJsonTraversal(DataFile.DATA);
		
		// status = new DynamicProperty<>("default_status", dataWriter.getProperty("default_status", JsonTraversal::getNode), PresenceData.class);
		
		// Iterable<ObjectNode> replyData = Utils.map(config.getArrayProperty("replies", node -> (ObjectNode)node), )
	}
	
	public static class Property<T> {
		//private final String name;
		
		private T value;
		
		private Property(String name, T value) {
			this.value = value;
		}
		private Property(String name, JsonNode root, Function<JsonNode, T> reader) {
			//this.name = name;
			value = reader.apply(root.get(name));
		}
		
		public T get() { return value; }
	}
	
	public static class DynamicProperty<T> extends Property<T> {
		private final ThrowFunction<T, JsonNode, Throwable> writer;
		
		@SuppressWarnings("unchecked")
		private DynamicProperty(String name, JsonNode node, Class<? extends POJO<T>> pojoClass) throws IOException {
			this(name, node, nodeValue -> ((POJO<T>)Core.jsonMapper.readerFor(pojoClass).readValue(nodeValue)).create(), getWriter(pojoClass));
		}
		private DynamicProperty(String name, JsonNode node, ThrowFunction<JsonNode, T, IOException> reader, ThrowFunction<T, JsonNode, Throwable> writer) throws IOException {
			super(name, reader.apply(node));
			this.writer = writer;
		}
		
		public void set(T value) {
			super.value = value;
		}
	}
	
	private static <T> ThrowFunction<T, JsonNode, Throwable> getWriter(Class<? extends POJO<T>> pojoClass) {
		return instance -> {
			StringWriter cache = new StringWriter();
			
			Core.jsonMapper.writerFor(instance.getClass()).writeValue(cache, pojoClass.getDeclaredConstructor(instance.getClass()).newInstance(instance));
			cache.flush();
			String content = cache.getBuffer().toString();
			
			return Core.jsonMapper.readTree(content);
		};
	}
}
